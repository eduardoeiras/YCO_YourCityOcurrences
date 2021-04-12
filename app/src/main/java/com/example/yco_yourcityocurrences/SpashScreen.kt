package com.example.yco_yourcityocurrences

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.yco_yourcityocurrences.AES256.ChCrypto
import com.example.yco_yourcityocurrences.api.classes.EndPoints
import com.example.yco_yourcityocurrences.api.classes.ServiceBuilder
import com.example.yco_yourcityocurrences.api.classes.responses.Resposta
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class SpashScreen : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spash_screen)

        val background = object : Thread() {
            override fun run() {
                try {
                    sharedPreferences = getSharedPreferences(getString(R.string.user_creds_file_key), Context.MODE_PRIVATE)

                    val loggedIn = sharedPreferences.getBoolean(getString(R.string.log_in_done), false)

                    if(loggedIn) {
                        sleep(3000)
                        val intent = Intent(this@SpashScreen, ActivityLoginRealizado::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finish()
                    }
                    else {
                        sleep(3000)
                        val intent = Intent(baseContext, MainActivity::class.java)
                        startActivity(intent)
                    }

                } catch (e : Exception) {
                    e.printStackTrace()
                }
            }
        }
        background.start()
    }
}