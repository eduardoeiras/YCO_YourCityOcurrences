package com.example.yco_yourcityocurrences

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.lang.Exception

class SpashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spash_screen)

        val background = object : Thread() {
            override fun run() {
                try {
                    Thread.sleep(3000)

                    val intent = Intent(baseContext, MainActivity::class.java)
                    startActivity(intent)
                } catch (e : Exception) {
                    e.printStackTrace()
                }
            }
        }
        background.start()
    }
}