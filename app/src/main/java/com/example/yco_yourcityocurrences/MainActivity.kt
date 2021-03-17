package com.example.yco_yourcityocurrences

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.yco_yourcityocurrences.api.classes.EndPoints
import com.example.yco_yourcityocurrences.api.classes.responses.Resposta
import com.example.yco_yourcityocurrences.api.classes.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_notas, R.id.navigation_mapa, R.id.navigation_login, R.id.navigation_ajuda
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        sharedPreferences = this.getSharedPreferences(getString(R.string.user_creds_file_key), Context.MODE_PRIVATE)

        val nomeUser = sharedPreferences.getString(getString(R.string.username), "")
        val pwd = sharedPreferences.getString(getString(R.string.password), "")

        if(nomeUser.toString().isNotEmpty() && pwd.toString().isNotEmpty()) {
            val request = ServiceBuilder.buildService(EndPoints::class.java)
            val call = request.realizarLogin(nomeUser = nomeUser, pwd = pwd)
            call.enqueue(object : Callback<Resposta> {
                override fun onResponse(call: Call<Resposta>, response: Response<Resposta>) {
                    if (response.isSuccessful) {
                        if (response.body()?.status == true) {
                            val intent = Intent(this@MainActivity, ActivityLoginRealizado::class.java)
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                    this@MainActivity,
                                    response.body()?.MSG,
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                override fun onFailure(call: Call<Resposta>, t: Throwable) {
                    Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}