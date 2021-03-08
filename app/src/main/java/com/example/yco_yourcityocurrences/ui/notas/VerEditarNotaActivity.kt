package com.example.yco_yourcityocurrences.ui.notas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.example.yco_yourcityocurrences.R
import com.example.yco_yourcityocurrences.dataclasses.Nota

class VerEditarNotaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_ver_editar_nota)
        val nota = intent.getSerializableExtra("nota") as? Nota

        val titulo = findViewById<EditText>(R.id.ev_titulo_nota)
        val descricao = findViewById<EditText>(R.id.ev_conteudo_nota)
        val data = findViewById<TextView>(R.id.ev_data)

        if (nota != null) {
            titulo.setText(nota.titulo)
            descricao.setText(nota.conteudo)
            data.setText(nota.data)
        }
    }

    //VOLTAR À LISTAGEM DAS NOTAS, VERIFICANDO POR ALTERAÇÕES, CASO EXISTAM, APLICAM-SE E VOLTA-SE
    // AO ECRÃ ANTERIOR
    fun voltar(@Suppress("UNUSED_PARAMETER") view: View) {
        finish()
    }

    fun verificarAlteracoes() {

    }
}