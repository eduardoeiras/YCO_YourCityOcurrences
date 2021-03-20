package com.example.yco_yourcityocurrences.adaptors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yco_yourcityocurrences.R
import com.example.yco_yourcityocurrences.entities.Nota

class NotaAdaptor internal constructor(var clickListener: OnNotaClickListener) : RecyclerView.Adapter<NotaAdaptor.NotaViewHolder>() {

    private var notas = emptyList<Nota>()

    class NotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titulo: TextView = itemView.findViewById(R.id.titulo_nota)
        private val conteudo: TextView = itemView.findViewById(R.id.conteudo_nota)

        fun inicializar(item : Nota, action : OnNotaClickListener) {
            titulo.text = item.titulo
            val resumoTexto: String
            if(item.conteudo.length <= 100) {
                resumoTexto = item.conteudo
            }
            else {
                val resumoString = item.conteudo.take(100)
                resumoTexto = "$resumoString ..."
            }
            conteudo.text = resumoTexto

            itemView.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotaViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_nota, parent, false)
        return NotaViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return notas.size
    }

    override fun onBindViewHolder(holder: NotaViewHolder, position: Int) {
        val current = notas[position]

        holder.inicializar(current, clickListener)
    }

    internal fun setNotas(notas: List<Nota>) {
        this.notas = notas
        notifyDataSetChanged()
    }

    interface OnNotaClickListener {
        fun onItemClick(item : Nota, position: Int)
    }
}