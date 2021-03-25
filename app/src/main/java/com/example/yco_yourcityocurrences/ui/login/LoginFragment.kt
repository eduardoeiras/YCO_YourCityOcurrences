package com.example.yco_yourcityocurrences.ui.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.yco_yourcityocurrences.AES256.ChCrypto
import com.example.yco_yourcityocurrences.ActivityLoginRealizado
import com.example.yco_yourcityocurrences.R
import com.example.yco_yourcityocurrences.api.classes.EndPoints
import com.example.yco_yourcityocurrences.api.classes.ServiceBuilder
import com.example.yco_yourcityocurrences.api.classes.responses.Resposta
import com.example.yco_yourcityocurrences.ui.registo.RegistoActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginFragment : Fragment() {

    private lateinit var editNomeUser: EditText
    private lateinit var editPwd: EditText
    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_login, container, false)

        //On Click Listener para realizar o processo de login
        root.findViewById<Button>(R.id.botao_login).setOnClickListener { _ -> realizarLogin()
        }

        //On Click Listener para iniciar a atividade de registo
        root.findViewById<Button>(R.id.botao_registo).setOnClickListener { _ -> realizarRegisto()
        }

        editNomeUser = root.findViewById(R.id.editTextTextPersonName)
        editPwd = root.findViewById(R.id.editTextTextPassword)
        sharedPreferences = root.context.getSharedPreferences(getString(R.string.user_creds_file_key), Context.MODE_PRIVATE)

        return root
    }

    fun realizarLogin() {
        val nomeUser = editNomeUser.text.toString()
        val pwd = editPwd.text.toString()
        if (nomeUser.isNotEmpty() && pwd.isNotEmpty()) {
            val request = ServiceBuilder.buildService(EndPoints::class.java)
            val encryptedUserName = ChCrypto.aesEncrypt(nomeUser, "4u7x!A%D*G-KaPdSgVkYp3s5v8y/B?E(")
            val encryptedPwd = ChCrypto.aesEncrypt(pwd, "4u7x!A%D*G-KaPdSgVkYp3s5v8y/B?E(")
            val call = request.realizarLogin(nomeUser = encryptedUserName, pwd = encryptedPwd)
            call.enqueue(object : Callback<Resposta> {
                override fun onResponse(call: Call<Resposta>, response: Response<Resposta>) {
                    if (response.isSuccessful) {
                        if (response.body()?.status == true) {
                            Toast.makeText(
                                this@LoginFragment.context,
                                "Login realizado com sucesso!",
                                Toast.LENGTH_SHORT
                            ).show()
                            with(sharedPreferences.edit()) {
                                putString(getString(R.string.username), nomeUser)
                                putString(getString(R.string.password), pwd)
                                commit()
                            }
                            val intent = Intent(this@LoginFragment.context, ActivityLoginRealizado::class.java)
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                            this@LoginFragment.activity?.finish()
                        } else {
                            Toast.makeText(
                                this@LoginFragment.context,
                                response.body()?.MSG,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<Resposta>, t: Throwable) {
                    Toast.makeText(this@LoginFragment.context, t.message, Toast.LENGTH_SHORT).show()
                }
            })
        }
        else {
            Toast.makeText(this.context, getString(R.string.login_form_error), Toast.LENGTH_SHORT).show()
        }
    }

    fun realizarRegisto() {
        val intent = Intent(this.context, RegistoActivity::class.java)
        startActivity(intent)
    }
}