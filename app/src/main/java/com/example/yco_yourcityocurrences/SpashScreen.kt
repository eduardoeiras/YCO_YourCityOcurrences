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

                    val nomeUser = sharedPreferences.getString(getString(R.string.username), "")
                    val pwd = sharedPreferences.getString(getString(R.string.password), "")

                    if(nomeUser.toString().isNotEmpty() && pwd.toString().isNotEmpty()) {
                        val request = ServiceBuilder.buildService(EndPoints::class.java)
                        if(pwd != null && nomeUser != null) {
                            val encryptedUserName = ChCrypto.aesEncrypt(nomeUser, "4u7x!A%D*G-KaPdSgVkYp3s5v8y/B?E(")
                            val encryptedPwd = ChCrypto.aesEncrypt(pwd, "4u7x!A%D*G-KaPdSgVkYp3s5v8y/B?E(")
                            val call = request.realizarLogin(nomeUser = encryptedUserName, pwd = encryptedPwd)
                            call.enqueue(object : Callback<Resposta> {
                                override fun onResponse(call: Call<Resposta>, response: Response<Resposta>) {
                                    if (response.isSuccessful) {
                                        if (response.body()?.status == true) {
                                            val intent = Intent(this@SpashScreen, ActivityLoginRealizado::class.java)
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            val intent = Intent(baseContext, MainActivity::class.java)
                                            startActivity(intent)
                                            Toast.makeText(
                                                    this@SpashScreen,
                                                    response.body()?.MSG,
                                                    Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                                override fun onFailure(call: Call<Resposta>, t: Throwable) {
                                    Toast.makeText(this@SpashScreen, t.message, Toast.LENGTH_SHORT).show()
                                    val intent = Intent(baseContext, MainActivity::class.java)
                                    startActivity(intent)
                                }
                            })
                        }
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