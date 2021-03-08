package com.example.yco_yourcityocurrences.ui.notas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.yco_yourcityocurrences.R

class AdicionarNotaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_nota)
        this.title = resources.getString(R.string.new_note_activity_title)
    }

    fun cancelarNota(@Suppress("UNUSED_PARAMETER") view: View) {
        finish()
    }
}