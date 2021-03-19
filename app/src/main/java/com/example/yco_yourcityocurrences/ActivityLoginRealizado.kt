package com.example.yco_yourcityocurrences

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import androidx.activity.addCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yco_yourcityocurrences.ui.ajuda.AjudaFragment
import com.example.yco_yourcityocurrences.ui.mapa.MapaFragment
import com.example.yco_yourcityocurrences.ui.notas.NotasFragment

class ActivityLoginRealizado : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main_login_realizado)

        val navView: BottomNavigationView = findViewById(R.id.nav_view_login_realizado)

        navegarParaFragmento(MapaFragment())
        navView.selectedItemId = R.id.navigation_mapa

        navView.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.navigation_notas -> navegarParaFragmento(NotasFragment())
                R.id.navigation_mapa -> navegarParaFragmento(MapaFragment())
                R.id.navigation_log_out -> realizarLogout()
                R.id.navigation_ajuda -> navegarParaFragmento(AjudaFragment())
            }
            true
        }


    }

    private fun realizarLogout() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.warning_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
                .setView(dialogView)
        val mAlertDialog = mBuilder.show()

        dialogView.findViewById<Button>(R.id.warning_botao_sim).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            val sharedPreferences = this.getSharedPreferences(getString(R.string.user_creds_file_key), Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                clear()
                commit()
            }
            mAlertDialog.dismiss()
            this.finish()
        }

        dialogView.findViewById<Button>(R.id.warning_botao_nao).setOnClickListener {
            mAlertDialog.dismiss()
        }
    }


    private fun navegarParaFragmento(fragmento: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment_login_realizado, fragmento)
            commit()
        }
    }
}