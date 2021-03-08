package com.example.yco_yourcityocurrences.ui.ocorrencia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.example.yco_yourcityocurrences.R

class VerificarOcorrencia : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verificar_ocorrencia)
    }

    fun voltar(view: View) {
        if(view is Button) {
            finish()
        }
    }
}