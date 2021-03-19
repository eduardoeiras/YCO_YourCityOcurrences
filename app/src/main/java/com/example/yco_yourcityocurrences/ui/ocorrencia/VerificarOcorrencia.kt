package com.example.yco_yourcityocurrences.ui.ocorrencia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.yco_yourcityocurrences.R
import com.example.yco_yourcityocurrences.api.classes.EndPoints
import com.example.yco_yourcityocurrences.api.classes.ServiceBuilder
import com.example.yco_yourcityocurrences.api.classes.responses.RespostaOcorrencias
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerificarOcorrencia : AppCompatActivity() {

    private lateinit var titulo: TextView
    private lateinit var imagem: ImageView
    private lateinit var descricao: TextView
    private lateinit var rua: TextView
    private lateinit var distrito: TextView
    private lateinit var coordenadas: TextView
    private lateinit var nomeUtilizador: TextView
    private lateinit var data: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verificar_ocorrencia)

        titulo = findViewById(R.id.ocorrencia_titulo)
        imagem = findViewById(R.id.img_ocorrencia)
        descricao = findViewById(R.id.ocorrencia_desc)
        rua = findViewById(R.id.ocorrencia_rua)
        distrito = findViewById(R.id.ocorrencia_distrito)
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

    fun voltar(view: View) {
        if(view is ImageButton) {
            finish()
        }
    }
}