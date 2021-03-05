package com.example.yco_yourcityocurrences.adaptors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yco_yourcityocurrences.R
import com.example.yco_yourcityocurrences.dataclasses.Nota

class NotaAdaptor(val list: ArrayList<Nota>) : RecyclerView.Adapter<NotaAdaptor.NotaViewHolder>() {

    class NotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.titulo_nota)
        val conteudo: TextView = itemView.findViewById(R.id.conteudo_nota)

        fun inicializar(item : Nota) {
            titulo.text = item.titulo
            conteudo.text = item.conteudo
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotaViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_nota, parent, false)
        return NotaViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: NotaViewHolder, position: Int) {
        val currentPlace = list[position]

        holder.inicializar(currentPlace)

    }
}