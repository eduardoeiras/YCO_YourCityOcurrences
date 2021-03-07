package com.example.yco_yourcityocurrences.ui.notas

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yco_yourcityocurrences.AdicionarNotaActivity
import com.example.yco_yourcityocurrences.R
import com.example.yco_yourcityocurrences.adaptors.NotaAdaptor
import com.example.yco_yourcityocurrences.dataclasses.Nota
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class NotasFragment : Fragment(), NotaAdaptor.OnNotaClickListener {

    private lateinit var listaNotas: ArrayList<Nota>
    private lateinit var recyclerView: RecyclerView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_notas, container, false)

        listaNotas = ArrayList<Nota>()

        //SUBSTITUIR COM A LISTA OBTIDA DA BASE DE DADOS LOCAL E ENVIAR PARA O ADAPTOR
        val current = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val formatted = current.format(formatter)
        for (i in 0 until 500) {
            listaNotas.add(Nota(i, "Nota $i", "conteudo da nota $i com coisas e coisas que nunca mais acaba......", formatted))
        }

        recyclerView = root.findViewById(R.id.recyclerview_notas)

        recyclerView.adapter = NotaAdaptor(listaNotas, this)
        recyclerView.layoutManager = LinearLayoutManager(root.context)

        val fab: View = root.findViewById(R.id.adicionarNota)
        fab.setOnClickListener { view ->
            val intent = Intent(root.context, AdicionarNotaActivity::class.java)
            startActivity(intent)
        }

        return root
    }

    override fun onItemClick(item: Nota, position: Int) {
        Toast.makeText(this.context, item.titulo, Toast.LENGTH_SHORT).show()
    }
}