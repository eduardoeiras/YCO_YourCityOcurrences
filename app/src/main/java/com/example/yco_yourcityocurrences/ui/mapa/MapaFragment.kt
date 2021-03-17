package com.example.yco_yourcityocurrences.ui.mapa

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.yco_yourcityocurrences.ActivityLoginRealizado
import com.example.yco_yourcityocurrences.R
import com.example.yco_yourcityocurrences.api.classes.EndPoints
import com.example.yco_yourcityocurrences.api.classes.ServiceBuilder
import com.example.yco_yourcityocurrences.api.classes.responses.LinhaOcorrencia
import com.example.yco_yourcityocurrences.api.classes.responses.Ocorrencia
import com.example.yco_yourcityocurrences.api.classes.responses.RespostaOcorrencias
import com.example.yco_yourcityocurrences.ui.ocorrencia.VerificarOcorrencia

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


    private val callback = OnMapReadyCallback { googleMap ->
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
                                    val marker = googleMap.addMarker(MarkerOptions()
                                            .position(LatLng(lat, lng)))
                                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
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
                                    val marker = googleMap.addMarker(MarkerOptions()
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
                                val marker = googleMap.addMarker(MarkerOptions()
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
        googleMap.setOnMarkerClickListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPreferences = this.requireActivity().getSharedPreferences(getString(R.string.user_creds_file_key), Context.MODE_PRIVATE)
        nomeUser = sharedPreferences.getString(getString(R.string.username), "").toString()

        return inflater.inflate(R.layout.fragment_mapa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        val tag = p0?.tag
        if(tag is List<*>) {

        }
        else {
            val intent = Intent(this.context, VerificarOcorrencia::class.java)
            intent.putExtra("ID_OCORRENCIA", tag.toString())
            startActivity(intent)
        }
        return true
    }
}