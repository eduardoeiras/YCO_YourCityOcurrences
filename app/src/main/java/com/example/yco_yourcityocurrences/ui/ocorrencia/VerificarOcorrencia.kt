package com.example.yco_yourcityocurrences.ui.ocorrencia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import com.example.yco_yourcityocurrences.R

class VerificarOcorrencia : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verificar_ocorrencia)
        val id = this.intent.getStringExtra("ID_OCORRENCIA").toString()
        Log.i("ID_OCORRENCIA", id)
    }

    fun voltar(view: View) {
        if(view is ImageButton) {
            finish()
        }
    }
}