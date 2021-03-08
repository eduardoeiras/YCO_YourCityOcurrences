package com.example.yco_yourcityocurrences.ui.registo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.yco_yourcityocurrences.R

class RegistoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.title = resources.getString(R.string.registo_activity_title)
        setContentView(R.layout.activity_registo)
    }

    fun cancelarRegisto(@Suppress("UNUSED_PARAMETER") view: View) {
        finish()
    }

    fun realizarRegisto(@Suppress("UNUSED_PARAMETER") view: View) {

    }
}