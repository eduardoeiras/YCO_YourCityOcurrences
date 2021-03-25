package com.example.yco_yourcityocurrences.ui.notas

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.example.yco_yourcityocurrences.R
import com.example.yco_yourcityocurrences.entities.Nota

class VerEditarNotaActivity : AppCompatActivity() {

    private lateinit var nota: Nota

    private lateinit var titulo: EditText
    private lateinit var conteudo: EditText
    private lateinit var data: TextView

    private var guardar: Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_ver_editar_nota)
        nota = (intent.getSerializableExtra("nota") as? Nota)!!

        titulo = findViewById(R.id.ev_titulo_nota)
        conteudo = findViewById(R.id.ev_conteudo_nota)
        data = findViewById(R.id.ev_data)

        titulo.setText(nota.titulo)
        conteudo.setText(nota.conteudo)
        data.setText(nota.data)
    }

    fun voltar(view: View) {
        if(view is ImageButton) {
            val replyIntent = Intent()
            if(verificarAlteracoes() && guardar) {
                replyIntent.putExtra(REPLY_TITLE, titulo.text.toString())
                replyIntent.putExtra(REPLY_CONTENT, conteudo.text.toString())
                replyIntent.putExtra(REPLY_DATA, nota.data)
                replyIntent.putExtra(REPLY_ID, nota.id.toString())
                setResult(Activity.RESULT_OK, replyIntent)
                finish()
            }
            else {
                setResult(Activity.RESULT_CANCELED, replyIntent)
                finish()
            }
        }
    }

    fun verificarAlteracoes() : Boolean {
        var alterado = false

        if(!titulo.text.equals(nota.titulo)) {
            if(titulo.text.toString().isNotEmpty()) {
                alterado = true
            }
            else {
                Toast.makeText(this, resources.getString(R.string.nota_add_titulo_erro), Toast.LENGTH_SHORT).show()
                guardar = false
            }
        }
        if(!conteudo.text.equals(nota.conteudo)) {
            if(conteudo.text.toString().isNotEmpty()) {
                alterado = true
            }
            else {
                Toast.makeText(this, resources.getString(R.string.nota_add_conteudo_erro), Toast.LENGTH_SHORT).show()
                guardar = false
            }
        }

        return alterado
    }

    companion object {
        const val REPLY_TITLE = "title"
        const val REPLY_CONTENT = "content"
        const val REPLY_DATA = "data"
        const val REPLY_ID = "id"
        const val RESULT_REMOVE = 1
    }

    fun removerNota(view: View) {
        if(view is Button) {
            val replyIntent = Intent()
            replyIntent.putExtra(REPLY_ID, nota.id.toString())
            setResult(RESULT_REMOVE, replyIntent)
            finish()
        }
    }
}