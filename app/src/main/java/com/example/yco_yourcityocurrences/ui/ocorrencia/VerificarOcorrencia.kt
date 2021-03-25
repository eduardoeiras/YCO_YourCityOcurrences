package com.example.yco_yourcityocurrences.ui.ocorrencia

import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.yco_yourcityocurrences.R
import com.example.yco_yourcityocurrences.api.classes.EndPoints
import com.example.yco_yourcityocurrences.api.classes.ServiceBuilder
import com.example.yco_yourcityocurrences.api.classes.responses.RespostaOcorrencias
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerificarOcorrencia : AppCompatActivity() {

    private lateinit var titulo: TextView
    private lateinit var imagem: ImageView
    private lateinit var descricao: TextView
    private lateinit var tipo: TextView
    private lateinit var morada: TextView
    private lateinit var coordenadas: TextView
    private lateinit var nomeUtilizador: TextView
    private lateinit var data: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verificar_ocorrencia)

        titulo = findViewById(R.id.ocorrencia_titulo)
        imagem = findViewById(R.id.img_ocorrencia)
        descricao = findViewById(R.id.ocorrencia_desc)
        tipo = findViewById(R.id.ocorrencia_tipo)
        morada = findViewById(R.id.ocorrencia_morada)
        coordenadas = findViewById(R.id.ocorrencia_coords)
        nomeUtilizador = findViewById(R.id.ocorrencia_username)
        data = findViewById(R.id.ocorrencia_data)

        val id = this.intent.getStringExtra("ID_OCORRENCIA").toString()
        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val call = request.getOcorrenciaPorId(id = id.toInt())
        call.enqueue(object : Callback<RespostaOcorrencias> {
            override fun onResponse(call: Call<RespostaOcorrencias>, response: Response<RespostaOcorrencias>) {
                if (response.isSuccessful) {
                    if (response.body()?.status == true) {
                        val ocorrencia = response.body()?.data?.get(0)?.ocorrencia
                        if(ocorrencia != null) {
                            titulo.setText(ocorrencia.titulo)
                            descricao.setText(ocorrencia.descricao)
                            tipo.setText(ocorrencia.tipo)
                            Picasso.get().load(ocorrencia.imagem).into(imagem)
                            val latLng = "${ocorrencia.latitude}, ${ocorrencia.longitude}"
                            val adress = getAdress(ocorrencia.latitude.toDouble(), ocorrencia.longitude.toDouble())
                            morada.setText(adress)
                            coordenadas.setText(latLng)
                            nomeUtilizador.setText(ocorrencia.nomeUtilizador)
                            data.setText(ocorrencia.dataComunicacao)
                        }

                    } else {
                        Toast.makeText(
                            this@VerificarOcorrencia,
                            response.body()?.MSG,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            override fun onFailure(call: Call<RespostaOcorrencias>, t: Throwable) {
                Toast.makeText(this@VerificarOcorrencia, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun getAdress(lat: Double, lng: Double): String {
        val geocoder = Geocoder(this)
        val list = geocoder.getFromLocation(lat, lng, 1)
        return list[0].getAddressLine(0)
    }

    fun voltar(view: View) {
        if(view is ImageButton) {
            finish()
        }
    }
}