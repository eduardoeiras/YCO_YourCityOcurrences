package com.example.yco_yourcityocurrences

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton

class AdicionarNotaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_nota)
        this.title = R.string.new_note_activity_title.toString()
    }

    fun cancelarNota(view: View) {
        finish()
    }
}