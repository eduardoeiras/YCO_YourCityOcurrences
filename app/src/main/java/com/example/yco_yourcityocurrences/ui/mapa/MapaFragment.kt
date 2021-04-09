package com.example.yco_yourcityocurrences.ui.mapa

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.example.yco_yourcityocurrences.R
import com.example.yco_yourcityocurrences.api.classes.EndPoints
import com.example.yco_yourcityocurrences.api.classes.ServiceBuilder
import com.example.yco_yourcityocurrences.api.classes.responses.LinhaOcorrencia
import com.example.yco_yourcityocurrences.api.classes.responses.RespostaOcorrencias
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

class MapaFragment : Fragment(), GoogleMap.OnMarkerClickListener {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var nomeUser: String

    private lateinit var gMap: GoogleMap

    private lateinit var lastLocation: Location
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var fabAdicionarOcorrencia: View
    private lateinit var layoutLabels: ConstraintLayout
    private lateinit var botaoFiltros: ImageButton

    private var resetCamera = true
    private var reqCodeAdicionarOcorrencia = 1
    private var reqCodeEditarRemoverOcorrencia = 2

    private val callback = OnMapReadyCallback { googleMap ->
        gMap = googleMap
        requestOcorrencias()
        googleMap.setOnMarkerClickListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_mapa, container, false)
        sharedPreferences = this.requireActivity().getSharedPreferences(getString(R.string.user_creds_file_key), Context.MODE_PRIVATE)
        nomeUser = sharedPreferences.getString(getString(R.string.username), "").toString()

        root.findViewById<ImageButton>(R.id.reset_location).setOnClickListener {
                _ -> resetCamera = true
        }

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

        fabAdicionarOcorrencia.setOnClickListener { _ ->
            val intent = Intent(this.context, AdicionarOcorrencia::class.java)
            intent.putExtra("LAT", lastLocation.latitude)
            intent.putExtra("LNG", lastLocation.longitude)
            startActivityForResult(intent, reqCodeAdicionarOcorrencia)
        }

        botaoFiltros = root.findViewById(R.id.botao_filtrar_tipo)

        botaoFiltros.setOnClickListener { _ ->

        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(root.context)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                if(resetCamera) {
                    lastLocation = p0.lastLocation
                    val loc = LatLng(lastLocation.latitude, lastLocation.longitude)
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15.0f))
                    resetCamera = false
                }
            }
        }

        createLocationRequest()

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
        if(ActivityCompat.checkSelfPermission( this.requireActivity(),
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

    /*DETERMINAÇÃO DO TIPO DE PONTO SELECIONADO NO MAPA, ENCAMINHANDO PARA A RESPETIVA ATIVIDADE*/
    override fun onMarkerClick(p0: Marker?): Boolean {
        val tag = p0?.tag
        if(tag is List<*>) {
            val intent = Intent(this.context, EditarRemoverOcorrencia::class.java)
            intent.putExtra("ID_OCORRENCIA", tag[1].toString())
            startActivityForResult(intent, reqCodeEditarRemoverOcorrencia)
        }
        else {
            val intent = Intent(this.context, VerificarOcorrencia::class.java)
            intent.putExtra("ID_OCORRENCIA", tag.toString())
            startActivity(intent)
        }
        return true
    }

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
    fun requestOcorrencias() {
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

    companion object {
        private const val LOCATION_PERMISSION_ACCESS_CODE = 1
    }
}