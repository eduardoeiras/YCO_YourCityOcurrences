package com.example.yco_yourcityocurrences.ui.notas

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.example.yco_yourcityocurrences.R

class AdicionarNotaActivity : AppCompatActivity() {

    private lateinit var editTitulo: EditText
    private lateinit var editConteudo: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_nota)
        this.title = resources.getString(R.string.new_note_activity_title)

        editTitulo = findViewById(R.id.edit_titulo_nota)
        editConteudo = findViewById(R.id.conteudo_nota)

    }

    fun cancelarNota( view: View) {
        if(view is ImageButton) {
            val replyIntent = Intent()
            setResult(Activity.RESULT_CANCELED, replyIntent)
            finish()
        }
    }

    fun adicionarNota(view: View) {
        val replyIntent = Intent()
        if(!verificaCampos()) {
            replyIntent.putExtra(REPLY_TITLE, editTitulo.text.toString())
            replyIntent.putExtra(REPLY_CONTENT, editConteudo.text.toString())
            setResult(Activity.RESULT_OK, replyIntent)
            finish()
        }
    }

    private fun verificaCampos() : Boolean {
        var erro = false

        if(editTitulo.text.isEmpty()) {
            erro = true
            findViewById<TextView>(R.id.add_nota_title_error).setText(resources.getString(R.string.nota_add_titulo_erro))
        }
        if (editConteudo.text.isEmpty()) {
            erro = true
            findViewById<TextView>(R.id.add_nota_conteudo_error).setText(resources.getString(R.string.nota_add_conteudo_erro))
        }
        return erro
    }

    companion object {
        const val REPLY_TITLE = "title"
        const val REPLY_CONTENT = "content"
    }
}