package com.example.yco_yourcityocurrences.ui.notas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yco_yourcityocurrences.R
import com.example.yco_yourcityocurrences.adaptors.NotaAdaptor
import com.example.yco_yourcityocurrences.dataclasses.Nota

class NotasFragment : Fragment() {

    private lateinit var listaNotas: ArrayList<Nota>
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_notas, container, false)

        listaNotas = ArrayList<Nota>()

        for (i in 0 until 500) {
            listaNotas.add(Nota(i, "Nota $i", "conteudo da nota $i com coisas e coisas que nunca mais acaba......"))
        }

        recyclerView = root.findViewById(R.id.recyclerview_notas)

        recyclerView.adapter = NotaAdaptor(listaNotas)
        recyclerView.layoutManager = LinearLayoutManager(root.context)

        return root
    }
}