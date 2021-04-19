package com.example.yco_yourcityocurrences.ui.ocorrencia

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.yco_yourcityocurrences.R
import com.example.yco_yourcityocurrences.adaptors.SpinnerTiposAdaptor
import com.example.yco_yourcityocurrences.api.classes.EndPoints
import com.example.yco_yourcityocurrences.api.classes.ServiceBuilder
import com.example.yco_yourcityocurrences.api.classes.responses.*
import com.squareup.picasso.Picasso
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class EditarRemoverOcorrencia : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var titulo: EditText
    private lateinit var imagem: ImageView
    private lateinit var descricao: EditText
    private var idTipoSelecionado: Int = 0
    private lateinit var morada: TextView
    private lateinit var coordenadas: TextView
    private lateinit var nomeUtilizador: TextView
    private lateinit var data: TextView
    private lateinit var spinnerTipos: Spinner

    private lateinit var titulo_erro: TextView
    private lateinit var descricao_erro: TextView

    private lateinit var ocorrenciaOriginal: Ocorrencia

    private var urlImgSubmetida: String = ""

    private val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val REQUEST_EXTERNAL_STORAGE = 3
    private var permissaoConcedida = false

    val PICK_IMAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_remover_ocorrencia)

        titulo = findViewById(R.id.er_ocorrencia_titulo)
        imagem = findViewById(R.id.img_ocorrencia)
        descricao = findViewById(R.id.er_ocorrencia_desc)
        morada = findViewById(R.id.add_ocorrencia_morada)
        coordenadas = findViewById(R.id.add_ocorrencia_coords)
        nomeUtilizador = findViewById(R.id.ocorrencia_username)
        data = findViewById(R.id.ocorrencia_data)

        titulo_erro = findViewById(R.id.ocorrencia_titulo_erro)
        descricao_erro = findViewById(R.id.ocorrencia_descricao_erro)
        spinnerTipos = findViewById(R.id.er_ocorrencia_tipo)

        getTipos()

        val id = this.intent.getStringExtra("ID_OCORRENCIA")?.toInt()
        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val call = request.getOcorrenciaPorId(id = id)
        call.enqueue(object : Callback<RespostaOcorrencias> {
            override fun onResponse(call: Call<RespostaOcorrencias>, response: Response<RespostaOcorrencias>) {
                if (response.isSuccessful) {
                    if (response.body()?.status == true) {
                        val ocorrencia = response.body()?.data?.get(0)?.ocorrencia
                        if (ocorrencia != null) {
                            ocorrenciaOriginal = ocorrencia
                            titulo.setText(ocorrencia.titulo)
                            descricao.setText(ocorrencia.descricao)
                            idTipoSelecionado = ocorrencia.tipo_id
                            spinnerTipos.setSelection(getIndex(spinnerTipos, idTipoSelecionado))
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
                                this@EditarRemoverOcorrencia,
                                response.body()?.MSG,
                                Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<RespostaOcorrencias>, t: Throwable) {
                Toast.makeText(this@EditarRemoverOcorrencia, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getIndex(spinner: Spinner, idTipo: Int): Int {
        for (i in 0 until spinner.count) {
            val tipo = spinner.getItemAtPosition(i) as Tipo
            if (tipo.id == idTipo) {
                return i
            }
        }
        return 0
    }

    fun getTipos() {
        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val call = request.getAllTiposOcorrencia()
        call.enqueue(object : Callback<RespostaTipo> {
            override fun onResponse(call: Call<RespostaTipo>, response: Response<RespostaTipo>) {
                if (response.isSuccessful) {
                    if (response.body()?.status == true) {
                        val adapter = SpinnerTiposAdaptor(this@EditarRemoverOcorrencia, response.body()!!.data)
                        spinnerTipos.adapter = adapter
                        spinnerTipos.onItemSelectedListener = this@EditarRemoverOcorrencia
                    } else {
                        Toast.makeText(
                                this@EditarRemoverOcorrencia,
                                response.body()?.MSG,
                                Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<RespostaTipo>, t: Throwable) {
                Toast.makeText(this@EditarRemoverOcorrencia, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun getAdress(lat: Double, lng: Double): String {
        val geocoder = Geocoder(this)
        val list = geocoder.getFromLocation(lat, lng, 1)
        return list[0].getAddressLine(0)
    }

    fun verificarCampos() : Boolean {
        var guardar = true

        if(titulo.text.toString().isNotEmpty()) {
            if(titulo.text.toString() != ocorrenciaOriginal.titulo) {
                titulo_erro.setText("")
            }
        }
        else {
            titulo_erro.setText(getString(R.string.ocorrencia_titulo_erro))
            guardar = false
        }

        if(descricao.text.toString().isNotEmpty()) {
            if(descricao.text.toString() != ocorrenciaOriginal.descricao) {
                descricao_erro.setText("")
            }
        }
        else {
            descricao_erro.setText(getString(R.string.ocorrencia_desc_erro))
            guardar = false
        }

        if(titulo.text.toString() == ocorrenciaOriginal.titulo && descricao.text.toString() == ocorrenciaOriginal.descricao
                && urlImgSubmetida != "") {
            guardar = true
        }

        return guardar
    }

    private fun verificaAlteracoes() : Boolean {
        var existemAlteracoes: Boolean

        existemAlteracoes = !(titulo.text.toString() == ocorrenciaOriginal.titulo && descricao.text.toString() == ocorrenciaOriginal.descricao
                && urlImgSubmetida == "")

        if(idTipoSelecionado != ocorrenciaOriginal.tipo_id) {
            existemAlteracoes = true
        }

        return existemAlteracoes
    }

    fun voltar(view: View) {
        if(view is ImageButton) {
            if(verificaAlteracoes()) {
                if(verificarCampos()) {
                    val request = ServiceBuilder.buildService(EndPoints::class.java)
                    val call: Call<Resposta> = if(urlImgSubmetida != "") {
                        request.atualizarOcorrencia(ocorrenciaOriginal.id_ocorrencia, titulo = titulo.text.toString(),
                                desc = descricao.text.toString(), tipo = idTipoSelecionado, imagem = urlImgSubmetida)
                    } else {
                        request.atualizarOcorrencia(ocorrenciaOriginal.id_ocorrencia, titulo = titulo.text.toString(),
                                desc = descricao.text.toString(), tipo = idTipoSelecionado, imagem = ocorrenciaOriginal.imagem)
                    }
                    call.enqueue(object : Callback<Resposta> {
                        override fun onResponse(call: Call<Resposta>, response: Response<Resposta>) {
                            if (response.isSuccessful) {
                                if (response.body()?.status == true) {
                                    Toast.makeText(
                                            this@EditarRemoverOcorrencia,
                                            getString(R.string.ocorrencia_atualizada),
                                            Toast.LENGTH_LONG
                                    ).show()
                                    val replyIntent = Intent()
                                    setResult(RESULT_EDIT, replyIntent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                            this@EditarRemoverOcorrencia,
                                            response.body()?.MSG,
                                            Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }

                        override fun onFailure(call: Call<Resposta>, t: Throwable) {
                            Toast.makeText(this@EditarRemoverOcorrencia, t.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
            else {
                val replyIntent = Intent()
                setResult(RESULT_CANCELED, replyIntent)
                finish()
            }
        }
    }

    //DIÁLOGO DE ALTERAÇÃO DA IMAGEM
    fun alterarImagem(view: View) {
        if(view is ImageView) {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.warning_dialog, null)
            val mBuilder = AlertDialog.Builder(this)
                    .setView(dialogView)
            val mAlertDialog = mBuilder.show()

            dialogView.findViewById<TextView>(R.id.wd_message).setText(getString(R.string.alt_img_warning))

            dialogView.findViewById<Button>(R.id.warning_botao_sim).setOnClickListener {
                verificarPermissaoStorage()
                mAlertDialog.dismiss()
            }

            dialogView.findViewById<Button>(R.id.warning_botao_nao).setOnClickListener {
                mAlertDialog.dismiss()
            }
        }
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
        }
        else {
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
        if(estadoPermissao) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE)
        }
        else {
            Toast.makeText(this@EditarRemoverOcorrencia, getString(R.string.erro_permissoes_storage), Toast.LENGTH_LONG).show()
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
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            result = cursor.getString(column_index)
            isok = true
        } finally {
            cursor?.close()
        }
        return if (isok) result else ""
    }

    //REALIZAÇÃO DO PEDIDO POST À API PARA A SUBMISSÃO DA IMAGEM
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && data != null) {
            val uri: Uri? = data.data
            if(uri != null) {
                val imagemSelecionada: Uri = data.data!!
                val caminhoImg = getRealPathFromURI(this, imagemSelecionada)
                if(caminhoImg != "") {
                    val file = File(caminhoImg)
                    val reqFile = RequestBody.create(MediaType.parse("image/*"), file)
                    val body = MultipartBody.Part.createFormData("imagem", file.name, reqFile)
                    val name = RequestBody.create(MediaType.parse("text/plain"), "imagem")
                    val request = ServiceBuilder.buildService(EndPoints::class.java)
                    val call = request.submeterImagem(body, name)
                    call.enqueue(object : Callback<RespostaImg> {
                        override fun onResponse(call: Call<RespostaImg>, response: Response<RespostaImg>) {
                            if (response.isSuccessful) {
                                if (response.body()?.status == true) {
                                    Picasso.get().load(uri).into(imagem)
                                    urlImgSubmetida = response.body()!!.urlImagem
                                } else {
                                    if (response.body()?.urlImagem != "") {
                                        Picasso.get().load(uri).into(imagem)
                                        urlImgSubmetida = response.body()!!.urlImagem
                                    } else {
                                        Toast.makeText(
                                                this@EditarRemoverOcorrencia,
                                                response.body()?.MSG,
                                                Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        }

                        override fun onFailure(call: Call<RespostaImg>, t: Throwable) {
                            Toast.makeText(this@EditarRemoverOcorrencia, t.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                else {
                    Toast.makeText(this@EditarRemoverOcorrencia, getString(R.string.erro_select_img), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun darComoResolvida(view: View) {
        if(view is Button) {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.warning_dialog, null)
            val mBuilder = AlertDialog.Builder(this)
                    .setView(dialogView)
            val mAlertDialog = mBuilder.show()

            val botaoSim = dialogView.findViewById<Button>(R.id.warning_botao_sim)
            val botaoNao = dialogView.findViewById<Button>(R.id.warning_botao_nao)

            dialogView.findViewById<TextView>(R.id.wd_message).setText(getString(R.string.remover_ocorrencia))

            botaoSim.setOnClickListener {
                val request = ServiceBuilder.buildService(EndPoints::class.java)
                val call = request.removerOcorrencia(ocorrenciaOriginal.id_ocorrencia)
                call.enqueue(object : Callback<Resposta> {
                    override fun onResponse(call: Call<Resposta>, response: Response<Resposta>) {
                        if (response.isSuccessful) {
                            if (response.body()?.status == true) {
                                Toast.makeText(
                                        this@EditarRemoverOcorrencia,
                                        getString(R.string.ocorrencia_removida),
                                        Toast.LENGTH_LONG
                                ).show()
                                val replyIntent = Intent()
                                setResult(RESULT_REMOVE, replyIntent)
                                finish()
                            } else {
                                Toast.makeText(
                                        this@EditarRemoverOcorrencia,
                                        response.body()?.MSG,
                                        Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<Resposta>, t: Throwable) {
                        Toast.makeText(this@EditarRemoverOcorrencia, t.message, Toast.LENGTH_SHORT).show()
                    }
                })
                mAlertDialog.dismiss()
            }

            botaoNao.setOnClickListener {
                mAlertDialog.dismiss()
            }
        }
    }

    companion object {
        const val RESULT_REMOVE = 1
        const val RESULT_EDIT = 2
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val tipoSelecionado: Tipo = parent?.getItemAtPosition(position) as Tipo
        idTipoSelecionado = tipoSelecionado.id
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(this@EditarRemoverOcorrencia, getString(R.string.obter_tipos_erro), Toast.LENGTH_LONG).show()
    }
}