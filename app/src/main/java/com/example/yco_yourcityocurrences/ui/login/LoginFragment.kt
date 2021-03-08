package com.example.yco_yourcityocurrences.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.yco_yourcityocurrences.R
import com.example.yco_yourcityocurrences.ui.registo.RegistoActivity

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_login, container, false)

        //On Click Listener para realizar o processo de login
        root.findViewById<Button>(R.id.botao_login).setOnClickListener {
            _ -> realizarLogin()
        }

        //On Click Listener para iniciar a atividade de registo
        root.findViewById<Button>(R.id.botao_registo).setOnClickListener {
            _ -> realizarRegisto()
        }

        return root
    }

    fun realizarLogin() {
        Toast.makeText(this.context, "Botão de Login", Toast.LENGTH_SHORT).show()
    }

    fun realizarRegisto() {
        val intent = Intent(this.context, RegistoActivity::class.java)
        startActivity(intent)
    }
}