package com.example.yco_yourcityocurrences.ui.mapa

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.yco_yourcityocurrences.R
import com.example.yco_yourcityocurrences.adaptors.SpinnerTiposAdaptor
import com.example.yco_yourcityocurrences.api.classes.EndPoints
import com.example.yco_yourcityocurrences.api.classes.ServiceBuilder
import com.example.yco_yourcityocurrences.api.classes.responses.*
import com.example.yco_yourcityocurrences.ui.ocorrencia.AdicionarOcorrencia
import com.example.yco_yourcityocurrences.ui.ocorrencia.EditarRemoverOcorrencia
import com.example.yco_yourcityocurrences.ui.ocorrencia.VerificarOcorrencia
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigDecimal

class MapaFragment : Fragment(), GoogleMap.OnMarkerClickListener, AdapterView.OnItemSelectedListener,
 SensorEventListener {

    //VARIÁVEIS RELACIONADAS COM SHARED PREFERENCES
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var nomeUser: String

    //MAPA
    private lateinit var gMap: GoogleMap
    private var posicaoUserMarker: Marker? = null

    //VARIÁVIES REFERENTES AOS PEDIDOS DE LOCALIZAÇÃO
    private lateinit var lastLocation: Location
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var singleFusedLocationClient: FusedLocationProviderClient
    private lateinit var singleLocationRequest: LocationRequest
    private lateinit var singleLocationCallback: LocationCallback

    //VARIÁVEIS REFERENTES A VIEWS PARA ADICIONAR E FILTRAR OCORRENCIAS
    private lateinit var fabAdicionarOcorrencia: View
    private lateinit var layoutLabels: ConstraintLayout
    private lateinit var botaoFiltros: ImageButton

    private lateinit var spinnerTipos: Spinner
    private lateinit var tipoSelecionado: String
    private lateinit var numKm: EditText

    private lateinit var botaoPesqTitulo: ImageButton
    private lateinit var pesqTituloCont: EditText

    //VARIÁVEIS PARA RECENTRAR A CÂMERA, REALIZAR A ADIÇÃO,
    // OU EDIÇÃO OU REMOÇÃO DE UMA OCORRÊNCIA
    private var resetCamera = true
    private val reqCodeAdicionarOcorrencia = 1
    private val reqCodeEditarRemoverOcorrencia = 2

    //VARIÁVIES RELACIONADAS COM O SENSOR DE MOVIMENTO
    private lateinit var mSensorManager: SensorManager
    private lateinit var mSensorAccelerometer: Sensor
    private lateinit var mSensorMagnetometer: Sensor

    private var mAccelerometerData = FloatArray(3)
    private var mMagnetometerData = FloatArray(3)

    private var azimuth: Float = 0.0f
    private var pitch: Float = 0.0f
    private var roll: Float = 0.0f

    private val VALUE_DRIFT = 0.05f

    private val callback = OnMapReadyCallback { googleMap ->
        gMap = googleMap
        requestOcorrencias()
        googleMap.setOnMarkerClickListener(this)
        googleMap.setOnCameraMoveListener {
            resetCamera = false
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_mapa, container, false)
        sharedPreferences = this.requireActivity().getSharedPreferences(getString(R.string.user_creds_file_key), Context.MODE_PRIVATE)
        nomeUser = sharedPreferences.getString(getString(R.string.username), "").toString()

        root.findViewById<ImageButton>(R.id.reset_location).setOnClickListener { _ -> resetCamera = true
        }

        /*OBTENÇÃO DAS VIEWS DO LAYOUT E MOSTRAR OU ESCONDER O BOTÃO DE ADIÇÃO DE UMA OCORRÊNCIA*/
        fabAdicionarOcorrencia = root.findViewById(R.id.adicionarOcorrencia)
        layoutLabels = root.findViewById(R.id.constraint_layout_labels)

        if(nomeUser != "") {
            layoutLabels.visibility = View.VISIBLE
            fabAdicionarOcorrencia.visibility = View.VISIBLE
        }
        else {
            layoutLabels.visibility = View.INVISIBLE
            fabAdicionarOcorrencia.visibility = View.INVISIBLE
        }

        //FAB PARA ACICIONAR UMA OCORRENCIA, OBTENDO A LOCALIZAÇÃO ATUAL
        fabAdicionarOcorrencia.setOnClickListener { _ ->
            singleFusedLocationClient = LocationServices.getFusedLocationProviderClient(root.context)
            createSingleLocationRequest()
            startSingleLocationRequest()
        }

        //CALLBACK PARA O RECEBIMENTO DA LOCALIZAÇÃO ATUAL APENAS UMA VEZ
        singleLocationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                singleFusedLocationClient.removeLocationUpdates(singleLocationCallback)
                val intent = Intent(this@MapaFragment.context, AdicionarOcorrencia::class.java)
                intent.putExtra("LAT", p0.lastLocation.latitude)
                intent.putExtra("LNG", p0.lastLocation.longitude)
                startActivityForResult(intent, reqCodeAdicionarOcorrencia)
            }
        }

        /*----- PROCEDIMENTOS DE FILTRAGEM DE OCORRÊNCIAS ---- */
        botaoPesqTitulo = root.findViewById(R.id.btn_pesq_titulo)
        pesqTituloCont = root.findViewById(R.id.search_content)
        botaoPesqTitulo.setOnClickListener { _ ->
            if(pesqTituloCont.text.isNotEmpty()) {
                val request = ServiceBuilder.buildService(EndPoints::class.java)
                val call = request.getAllOcorrenciasTitulo(pesqTituloCont.text.toString())
                call.enqueue(object : Callback<RespostaOcorrencias> {
                    override fun onResponse(call: Call<RespostaOcorrencias>, response: Response<RespostaOcorrencias>) {
                        if (response.isSuccessful) {
                            if (response.body()?.status == true) {
                                gMap.clear()
                                val ocorrencias: List<LinhaOcorrencia>? = response.body()?.data
                                if (ocorrencias != null) {
                                    for (linha in ocorrencias) {
                                        val ocorrencia = linha.ocorrencia
                                        val idOcorrencia = ocorrencia.id_ocorrencia
                                        val lat = ocorrencia.latitude.toDouble()
                                        val lng = ocorrencia.longitude.toDouble()
                                        if (nomeUser == ocorrencia.nomeUtilizador) {
                                            val obj: List<Int> = listOf(0, idOcorrencia)
                                            val marker = gMap.addMarker(MarkerOptions()
                                                    .position(LatLng(lat, lng)))
                                            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                            marker.tag = obj
                                        } else {
                                            val marker = gMap.addMarker(MarkerOptions()
                                                    .position(LatLng(lat, lng)))
                                            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                            marker.tag = idOcorrencia
                                        }
                                    }
                                }
                                if(this@MapaFragment::lastLocation.isInitialized) {
                                    createUserMarker()
                                }
                            } else {
                                Toast.makeText(
                                        this@MapaFragment.context,
                                        response.body()?.MSG,
                                        Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<RespostaOcorrencias>, t: Throwable) {
                        Toast.makeText(this@MapaFragment.context, t.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }
            else {
                requestOcorrencias()
            }
        }

        botaoFiltros = root.findViewById(R.id.botao_filtrar_tipo)
        botaoFiltros.setOnClickListener { _ ->
            val dialogView = LayoutInflater.from(root.context).inflate(R.layout.filtros_dialog, null)
            val mBuilder = AlertDialog.Builder(root.context)
                    .setView(dialogView)
            val mAlertDialog = mBuilder.show()

            spinnerTipos = dialogView.findViewById(R.id.spinner_tipos)
            numKm = dialogView.findViewById(R.id.num_km)

            val request = ServiceBuilder.buildService(EndPoints::class.java)
            val call = request.getAllTiposOcorrencia()
            call.enqueue(object : Callback<RespostaTipo> {
                override fun onResponse(call: Call<RespostaTipo>, response: Response<RespostaTipo>) {
                    if (response.isSuccessful) {
                        if (response.body()?.status == true) {
                            val adapter = SpinnerTiposAdaptor(root.context, response.body()!!.data)
                            spinnerTipos.adapter = adapter
                            spinnerTipos.onItemSelectedListener = this@MapaFragment
                        } else {
                            Toast.makeText(
                                    this@MapaFragment.context,
                                    response.body()?.MSG,
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<RespostaTipo>, t: Throwable) {
                    Toast.makeText(this@MapaFragment.context, t.message, Toast.LENGTH_SHORT).show()
                }
            })

            dialogView.findViewById<Button>(R.id.btn_filtrar_tipo).setOnClickListener {
                val requestTipos = ServiceBuilder.buildService(EndPoints::class.java)
                val callTipos = requestTipos.getAllOcorrenciasTipo(tipo = tipoSelecionado)
                callTipos.enqueue(object : Callback<RespostaOcorrencias> {
                    override fun onResponse(call: Call<RespostaOcorrencias>, response: Response<RespostaOcorrencias>) {
                        if (response.isSuccessful) {
                            if (response.body()?.status == true) {
                                gMap.clear()
                                val ocorrencias: List<LinhaOcorrencia>? = response.body()?.data
                                if (ocorrencias != null) {
                                    for (linha in ocorrencias) {
                                        val ocorrencia = linha.ocorrencia
                                        val idOcorrencia = ocorrencia.id_ocorrencia
                                        val lat = ocorrencia.latitude.toDouble()
                                        val lng = ocorrencia.longitude.toDouble()
                                        if (nomeUser == ocorrencia.nomeUtilizador) {
                                            val obj: List<Int> = listOf(0, idOcorrencia)
                                            val marker = gMap.addMarker(MarkerOptions()
                                                    .position(LatLng(lat, lng)))
                                            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                            marker.tag = obj
                                        } else {
                                            val marker = gMap.addMarker(MarkerOptions()
                                                    .position(LatLng(lat, lng)))
                                            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                            marker.tag = idOcorrencia
                                        }
                                    }
                                    mAlertDialog.dismiss()
                                }
                                if(this@MapaFragment::lastLocation.isInitialized) {
                                    createUserMarker()
                                }
                            } else {
                                Toast.makeText(
                                        this@MapaFragment.context,
                                        response.body()?.MSG,
                                        Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<RespostaOcorrencias>, t: Throwable) {
                        Toast.makeText(this@MapaFragment.context, t.message, Toast.LENGTH_SHORT).show()
                    }
                })

            }

            dialogView.findViewById<Button>(R.id.btn_filtrar_raio).setOnClickListener {
                if(numKm.text.isNotEmpty()) {
                    val numeroKm = numKm.text.toString().toFloatOrNull()
                    if(numeroKm != null) {
                        val requestRaio = ServiceBuilder.buildService(EndPoints::class.java)
                        val callRaio = requestRaio.getAllOcorrenciasRaio(raio = numeroKm, lat = BigDecimal(lastLocation.latitude),
                                long = BigDecimal(lastLocation.longitude))
                        callRaio.enqueue(object : Callback<RespostaOcorrenciasRaio> {
                            override fun onResponse(call: Call<RespostaOcorrenciasRaio>, response: Response<RespostaOcorrenciasRaio>) {
                                if (response.isSuccessful) {
                                    if (response.body()?.status == true) {
                                        gMap.clear()
                                        val ocorrencias: List<Ocorrencia>? = response.body()?.data
                                        if (ocorrencias != null) {
                                            for (ocorrencia in ocorrencias) {
                                                val idOcorrencia = ocorrencia.id_ocorrencia
                                                val lat = ocorrencia.latitude.toDouble()
                                                val lng = ocorrencia.longitude.toDouble()
                                                if (nomeUser == ocorrencia.nomeUtilizador) {
                                                    val obj: List<Int> = listOf(0, idOcorrencia)
                                                    val marker = gMap.addMarker(MarkerOptions()
                                                            .position(LatLng(lat, lng)))
                                                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                                    marker.tag = obj
                                                } else {
                                                    val marker = gMap.addMarker(MarkerOptions()
                                                            .position(LatLng(lat, lng)))
                                                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                                    marker.tag = idOcorrencia
                                                }
                                            }
                                            mAlertDialog.dismiss()
                                        }
                                        if(this@MapaFragment::lastLocation.isInitialized) {
                                            createUserMarker()
                                        }
                                    } else {
                                        Toast.makeText(
                                                this@MapaFragment.context,
                                                response.body()?.MSG,
                                                Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<RespostaOcorrenciasRaio>, t: Throwable) {
                                Toast.makeText(this@MapaFragment.context, t.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                    else {
                        Toast.makeText(this@MapaFragment.context, getString(R.string.erro_num_km_tpDados), Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    Toast.makeText(this@MapaFragment.context, getString(R.string.erro_num_km), Toast.LENGTH_SHORT).show()
                }
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(root.context)

        //CALLBACK PARA A OBTENÇÃO PERIÓDICA DA LOCALIZAÇÃO
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                if(posicaoUserMarker != null) {
                    posicaoUserMarker!!.position = LatLng(lastLocation.latitude, lastLocation.longitude)
                }
                else {
                    createUserMarker()
                }
                if(resetCamera) {
                    val loc = LatLng(lastLocation.latitude, lastLocation.longitude)
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15.0f))
                    resetCamera = false
                }
            }
        }

        createLocationRequest()

        //PROCEDIMENTOS RELACIONADOS COM O SENSOR DE MOVIMENTO
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        mSensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensorMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        return root
    }

    /* CRIAR O MARCADOR DO UTILIZADOR NO MAPA */
    private fun createUserMarker() {
        posicaoUserMarker = gMap.addMarker(MarkerOptions()
            .position(LatLng(lastLocation.latitude, lastLocation.longitude))
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_arrow))
            .anchor(0.5f, 0.5f)
            .flat(true))
        posicaoUserMarker!!.tag = ""
    }

    /*OBTER O MAPA QUANDO A VIEW ESTIVER CRIADA*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    /*INICIAR A OBTENÇÃO E ATUALIZAÇÃO DA LOCALIZAÇÃO*/
    private fun startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this.requireActivity(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this.requireActivity(),
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_ACCESS_CODE)
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    /*DEFINIÇÃO DO PEDIDO DE LOCALIZAÇÃO*/
    private fun createLocationRequest() {
        locationRequest = LocationRequest.create()
        locationRequest.interval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /*INICIAR A OBTENÇÃO DA LOCALIZAÇÃO APENAS UMA VEZ NO MOMENTO DE ADIÇÃO*/
    private fun startSingleLocationRequest() {
        if(ActivityCompat.checkSelfPermission(this.requireActivity(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this.requireActivity(),
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_ACCESS_CODE)
            return
        }
        singleFusedLocationClient.requestLocationUpdates(singleLocationRequest, singleLocationCallback, null)
    }

    /*DEFINIÇÃO DO PEDIDO DE LOCALIZAÇÃO PARA QUE SEJA EXECUTADO NO MOMENTO*/
    private fun createSingleLocationRequest() {
        singleLocationRequest = LocationRequest.create()
        singleLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /*DETERMINAÇÃO DO TIPO DE PONTO SELECIONADO NO MAPA, ENCAMINHANDO PARA A RESPETIVA ATIVIDADE*/
    override fun onMarkerClick(p0: Marker?): Boolean {
        val tag = p0?.tag
        if(tag is List<*>) {
            val intent = Intent(this.context, EditarRemoverOcorrencia::class.java)
            intent.putExtra("ID_OCORRENCIA", tag[1].toString())
            startActivityForResult(intent, reqCodeEditarRemoverOcorrencia)
        }
        if(tag is Int) {
            val intent = Intent(this.context, VerificarOcorrencia::class.java)
            intent.putExtra("ID_OCORRENCIA", tag.toString())
            startActivity(intent)
        }
        return true
    }

    /*ATUALIZAÇÃO DAS OCORRÊNCIAS NO MAPA PARA QUANDO É ADICIONADA OU REMOVIDA UMA OCORRÊNCIA*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == reqCodeAdicionarOcorrencia && resultCode == Activity.RESULT_OK) {
            requestOcorrencias()
        }
        if(requestCode == reqCodeEditarRemoverOcorrencia && resultCode == EditarRemoverOcorrencia.RESULT_REMOVE) {
            requestOcorrencias()
        }
    }

    /*FUNÇÃO DE OBTENÇÃO DAS OCORRÊNCIAS, LIMPANDO O MAPA*/
    private fun requestOcorrencias() {
        gMap.clear()
        val request = ServiceBuilder.buildService(EndPoints::class.java)
        if(nomeUser != "") {
            val call = request.getOcorrenciasUtilizador(nomeUser = nomeUser)
            call.enqueue(object : Callback<RespostaOcorrencias> {
                override fun onResponse(call: Call<RespostaOcorrencias>, response: Response<RespostaOcorrencias>) {
                    if (response.isSuccessful) {
                        if (response.body()?.status == true) {
                            val ocorrencias: List<LinhaOcorrencia>? = response.body()?.data
                            if (ocorrencias != null) {
                                for (linha in ocorrencias) {
                                    val ocorrencia = linha.ocorrencia
                                    val idOcorrencia = ocorrencia.id_ocorrencia
                                    val lat = ocorrencia.latitude.toDouble()
                                    val lng = ocorrencia.longitude.toDouble()
                                    val obj: List<Int> = listOf(0, idOcorrencia)
                                    val marker = gMap.addMarker(MarkerOptions()
                                            .position(LatLng(lat, lng)))
                                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                    marker.tag = obj
                                }
                            }
                        } else {
                            Toast.makeText(
                                    this@MapaFragment.context,
                                    response.body()?.MSG,
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<RespostaOcorrencias>, t: Throwable) {
                    Toast.makeText(this@MapaFragment.context, t.message, Toast.LENGTH_SHORT).show()
                }
            })
            val callRestantesOcorrencias = request.getOcorrenciasMenosUser(nomeUser = nomeUser)
            callRestantesOcorrencias.enqueue(object : Callback<RespostaOcorrencias> {
                override fun onResponse(call: Call<RespostaOcorrencias>, response: Response<RespostaOcorrencias>) {
                    if (response.isSuccessful) {
                        if (response.body()?.status == true) {
                            val ocorrencias: List<LinhaOcorrencia>? = response.body()?.data
                            if (ocorrencias != null) {
                                for (linha in ocorrencias) {
                                    val ocorrencia = linha.ocorrencia
                                    val idOcorrencia = ocorrencia.id_ocorrencia
                                    val lat = ocorrencia.latitude.toDouble()
                                    val lng = ocorrencia.longitude.toDouble()
                                    val marker = gMap.addMarker(MarkerOptions()
                                            .position(LatLng(lat, lng)))
                                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                    marker.tag = idOcorrencia
                                }
                            }
                        } else {
                            Toast.makeText(
                                    this@MapaFragment.context,
                                    response.body()?.MSG,
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<RespostaOcorrencias>, t: Throwable) {
                    Toast.makeText(this@MapaFragment.context, t.message, Toast.LENGTH_SHORT).show()
                }
            })
        }
        else {
            val call = request.getAllOcorrencias()
            call.enqueue(object : Callback<List<LinhaOcorrencia>> {
                override fun onResponse(call: Call<List<LinhaOcorrencia>>, response: Response<List<LinhaOcorrencia>>) {
                    if (response.isSuccessful) {
                        val ocorrencias: List<LinhaOcorrencia>? = response.body()
                        if (ocorrencias != null) {
                            for (linha in ocorrencias) {
                                val ocorrencia = linha.ocorrencia
                                val idOcorrencia = ocorrencia.id_ocorrencia
                                val lat = ocorrencia.latitude.toDouble()
                                val lng = ocorrencia.longitude.toDouble()
                                val marker = gMap.addMarker(MarkerOptions()
                                        .position(LatLng(lat, lng)))
                                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                marker.tag = idOcorrencia
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<List<LinhaOcorrencia>>, t: Throwable) {
                    Toast.makeText(this@MapaFragment.context, t.message, Toast.LENGTH_SHORT).show()
                }
            })
        }
        if(this::lastLocation.isInitialized) {
            createUserMarker()
        }
    }

    /*OBTENÇÃO DO TIPO DE OCORRÊNCIA SELECIONADO NO SPINNER*/
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val text: String = parent?.getItemAtPosition(position).toString()
        tipoSelecionado = text
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(this@MapaFragment.context, getString(R.string.obter_tipos_erro), Toast.LENGTH_LONG).show()
    }

    override fun onStart() {
        super.onStart()
        mSensorManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mSensorMagnetometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onStop() {
        super.onStop()
        mSensorManager.unregisterListener(this)
    }

    /*PARAR OS PEDIDOS DE LOCALIZAÇÃO QUANDO A ATIVIDADE ENTRAR EM PAUSA*/
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    /*RESUMIR OS PEDIDOS DE LOCALIZAÇÃO QUANDO A APLICAÇÃO RESUMIR A ATIVIDADE*/
    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }
    /*MÉTODOS RELATIVOS À OBTENÇÃO DA INFORMAÇÃO DE SENSORES*/
    override fun onSensorChanged(event: SensorEvent?) {
        val sensorType = event?.sensor?.type
        when(sensorType) {
            Sensor.TYPE_ACCELEROMETER -> {
                mAccelerometerData = event.values.clone()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                mMagnetometerData = event.values.clone()
            }
            else -> return
        }

        val rotationMatrix = FloatArray(9)
        val rotationOk: Boolean = SensorManager.getRotationMatrix(rotationMatrix,
                null, mAccelerometerData, mMagnetometerData)
        val orientationValues = FloatArray(3)
        if (rotationOk) {
            SensorManager.getOrientation(rotationMatrix, orientationValues)
        }
        azimuth = orientationValues[0]
        pitch = orientationValues[1]
        roll = orientationValues[2]

        if (posicaoUserMarker != null) {
            posicaoUserMarker!!.setRotation(azimuth * 57.2957795f)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    companion object {
        internal const val LOCATION_PERMISSION_ACCESS_CODE = 1
    }
}