package com.example.yco_yourcityocurrences.ui.ocorrencia

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.yco_yourcityocurrences.R
import com.example.yco_yourcityocurrences.api.classes.EndPoints
import com.example.yco_yourcityocurrences.api.classes.ServiceBuilder
import com.example.yco_yourcityocurrences.api.classes.responses.Ocorrencia
import com.example.yco_yourcityocurrences.api.classes.responses.Resposta
import com.example.yco_yourcityocurrences.api.classes.responses.RespostaImg
import com.example.yco_yourcityocurrences.api.classes.responses.RespostaOcorrencias
import com.example.yco_yourcityocurrences.ui.mapa.MapaFragment
import com.example.yco_yourcityocurrences.ui.notas.VerEditarNotaActivity
import com.squareup.picasso.Picasso
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class AdicionarOcorrencia : AppCompatActivity() {
    private lateinit var titulo: EditText
    private lateinit var imagem: ImageView
    private lateinit var no_img_text: TextView
    private lateinit var descricao: EditText
    private lateinit var tipo: EditText
    private lateinit var morada: TextView
    private lateinit var coords: TextView

    private lateinit var titulo_erro: TextView
    private lateinit var descricao_erro: TextView
    private lateinit var tipo_erro: TextView

    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private var nomeUser = ""

    private var urlImgSubmetida: String = ""
    private var urlImgDispositivo: String = ""

    private val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val REQUEST_EXTERNAL_STORAGE = 3
    private var permissaoConcedida = false
    private val PICK_IMAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_adicionar_ocorrencia)

        titulo = findViewById(R.id.add_ocorrencia_title)
        imagem = findViewById(R.id.add_ocorrencia_img)
        no_img_text = findViewById(R.id.add_ocorrencia_no_img)
        descricao = findViewById(R.id.add_ocorrencia_desc)
        tipo = findViewById(R.id.add_ocorrencia_tipo)

        morada = findViewById(R.id.add_ocorrencia_morada)
        coords = findViewById(R.id.add_ocorrencia_coords)

        titulo_erro = findViewById(R.id.add_ocorrencia_titulo_erro)
        descricao_erro = findViewById(R.id.add_ocorrencia_desc_erro)
        tipo_erro = findViewById(R.id.add_ocorrencia_tipo_erro)

        lat = intent.getDoubleExtra("LAT", 0.0)
        lng = intent.getDoubleExtra("LNG", 0.0)

        val coordsText = "$lat, $lng"
        coords.text = coordsText
        morada.text = getAdress(lat, lng)

        val sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.user_creds_file_key), Context.MODE_PRIVATE)
        nomeUser = sharedPreferences.getString(getString(R.string.username), "").toString()
        if(nomeUser == "") {
            Toast.makeText(this, getString(R.string.erro_obtencao_username), Toast.LENGTH_SHORT).show()
            val replyIntent = Intent()
            setResult(Activity.RESULT_CANCELED, replyIntent)
            finish()
        }
    }

    fun getAdress(lat: Double, lng: Double): String {
        val geocoder = Geocoder(this)
        val list = geocoder.getFromLocation(lat, lng, 1)
        return list[0].getAddressLine(0)
    }

    fun verificarCampos(): Boolean {
        var guardar = true

        if (titulo.text.toString().isEmpty()) {
            titulo_erro.text = getString(R.string.ocorrencia_titulo_erro)
            guardar = false
        }
        else {
            titulo_erro.text = ""
        }

        if (descricao.text.toString().isEmpty()) {
            descricao_erro.text = getString(R.string.ocorrencia_desc_erro)
            guardar = false
        }
        else {
            descricao_erro.text = ""
        }

        if (tipo.text.toString().isEmpty()) {
            tipo_erro.text = getString(R.string.ocorrencia_tipo_erro)
            guardar = false
        }
        else {
            tipo_erro.text = ""
        }

        return guardar
    }

    /*VERIFICAÇÃO DAS PERMISÕES DE ACESSO AO SISTEMA DE FICHEIROS DO DISPOSITIVO*/
    private fun verificarPermissaoStorage() {
        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            )
        } else {
            permissaoConcedida = true
            procederSubmissaoImg(permissaoConcedida)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {
                permissaoConcedida = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                procederSubmissaoImg(permissaoConcedida)
            }
        }
    }

    //REALIZAR A SELEÇÃO DA IMAGEM SE EXISTIREM AS PERMISSÕES NECESSÁRIAS
    private fun procederSubmissaoImg(estadoPermissao: Boolean) {
        if (estadoPermissao) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE)
        } else {
            Toast.makeText(this@AdicionarOcorrencia, getString(R.string.erro_permissoes_storage), Toast.LENGTH_LONG).show()
        }
    }

    //OBTENÇÃO DO CAMINHO REAL NO DISPOSITIVO DA IMAGEM SELECIONADA
    fun getRealPathFromURI(context: Context, ac_Uri: Uri?): String {
        val result: String
        val isok: Boolean
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(ac_Uri!!, proj, null, null, null)
            val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            result = cursor.getString(columnIndex)
            isok = true
        } finally {
            cursor?.close()
        }
        return if (isok) result else ""
    }

    //OBTENÇÃO DO URL NO DISPOSITIVO PARA A IMAGEM SELECIONADA, APRESENTANDO-A NO ECRÃ
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && data != null) {
            val uri: Uri? = data.data
            if (uri != null) {
                val imagemSelecionada: Uri = data.data!!
                val caminhoImg = getRealPathFromURI(this, imagemSelecionada)
                if (caminhoImg != "") {
                    urlImgDispositivo = caminhoImg
                    no_img_text.visibility = View.GONE
                    imagem.setImageURI(uri)
                } else {
                    Toast.makeText(this@AdicionarOcorrencia, getString(R.string.erro_select_img), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun selecionarImg(view: View) {
        if(view is Button) {
            verificarPermissaoStorage()
        }
    }

    fun adicionarOcorrencia(view: View) {
        if(view is Button) {
            if(verificarCampos() && urlImgDispositivo != "") {
                val file = File(urlImgDispositivo)
                val reqFile = RequestBody.create(MediaType.parse("image/*"), file)
                val body = MultipartBody.Part.createFormData("imagem", file.name, reqFile)
                val name = RequestBody.create(MediaType.parse("text/plain"), "imagem")
                val request = ServiceBuilder.buildService(EndPoints::class.java)
                val call = request.submeterImagem(body, name)
                call.enqueue(object : Callback<RespostaImg> {
                    override fun onResponse(call: Call<RespostaImg>, response: Response<RespostaImg>) {
                        if (response.isSuccessful) {
                            if (response.body()?.status == true) {
                                urlImgSubmetida = response.body()!!.urlImagem
                                realizarCriacaoOcorrencia()
                            }
                            else {
                                if(response.body()?.urlImagem != "") {
                                    urlImgSubmetida = response.body()!!.urlImagem
                                    realizarCriacaoOcorrencia()
                                }
                                else {
                                    Toast.makeText(
                                            this@AdicionarOcorrencia,
                                            response.body()?.MSG,
                                            Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<RespostaImg>, t: Throwable) {
                        Toast.makeText(this@AdicionarOcorrencia, t.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }
            else {
                Toast.makeText(this, getString(R.string.img_obrigatoria), Toast.LENGTH_LONG).show()
            }
        }
    }

    fun realizarCriacaoOcorrencia() {
        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val dataAtual = LocalDateTime.now()
        val dataFormatada = dataAtual.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        val call = request.criarOcorrencia(titulo = titulo.text.toString(), desc = descricao.text.toString(), imagem = urlImgSubmetida,
        tipo = tipo.text.toString(), dataComunicacao = dataFormatada, latitude = BigDecimal(lat), longitude = BigDecimal(lng), nomeUtilizador = nomeUser)
        call.enqueue(object : Callback<Resposta> {
            override fun onResponse(call: Call<Resposta>, response: Response<Resposta>) {
                if (response.isSuccessful) {
                    if (response.body()?.status == true) {
                        Toast.makeText(
                                this@AdicionarOcorrencia,
                                getString(R.string.ocorrencia_adicionada),
                                Toast.LENGTH_LONG
                        ).show()
                        val replyIntent = Intent()
                        setResult(Activity.RESULT_OK, replyIntent)
                        finish()
                    } else {
                        Toast.makeText(
                                this@AdicionarOcorrencia,
                                response.body()?.MSG,
                                Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<Resposta>, t: Throwable) {
                Toast.makeText(this@AdicionarOcorrencia, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun cancelar(view: View) {
        if(view is Button) {
            val replyIntent = Intent()
            setResult(Activity.RESULT_CANCELED, replyIntent)
            finish()
        }
    }
}