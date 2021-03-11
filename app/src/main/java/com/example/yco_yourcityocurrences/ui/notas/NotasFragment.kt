package com.example.yco_yourcityocurrences.ui.notas

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yco_yourcityocurrences.R
import com.example.yco_yourcityocurrences.adaptors.NotaAdaptor
import com.example.yco_yourcityocurrences.entities.Nota
import com.example.yco_yourcityocurrences.view_model.NotaViewModel
import java.text.DateFormat.getDateInstance
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.collections.ArrayList

class NotasFragment : Fragment(), NotaAdaptor.OnNotaClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var notaViewModel: NotaViewModel
    private val novaNotaActivityReqCode = 1
    private val verificarEditarNotaReqCode = 2
    val ITEM_NOTA = "nota"

    //@RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_notas, container, false)

        //val data =  Date()

        //Recycler View
        recyclerView = root.findViewById(R.id.recyclerview_notas)
        val adaptor = NotaAdaptor(root.context, this)
        recyclerView.adapter = adaptor
        recyclerView.layoutManager = LinearLayoutManager(root.context)

        //View Model
        notaViewModel = ViewModelProvider(this).get(NotaViewModel::class.java)
        notaViewModel.todasNotas.observe(viewLifecycleOwner, {notas ->
            notas?.let { adaptor.setNotas(it) }
        })


        val fab: View = root.findViewById(R.id.adicionarNota)

        fab.setOnClickListener { _ ->
            val intent = Intent(this.context, AdicionarNotaActivity::class.java)
            startActivityForResult(intent, novaNotaActivityReqCode)
        }

        return root
    }

    override fun onItemClick(item: Nota, position: Int) {
        val intent = Intent(this.context, VerEditarNotaActivity::class.java)
        intent.putExtra(ITEM_NOTA, item)
        startActivityForResult(intent, verificarEditarNotaReqCode)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == novaNotaActivityReqCode && resultCode == Activity.RESULT_OK) {
            val titulo = data?.getStringExtra(AdicionarNotaActivity.REPLY_TITLE).toString()
            val conteudo = data?.getStringExtra(AdicionarNotaActivity.REPLY_CONTENT).toString()
            val dataAtual = LocalDateTime.now()
            val dataFormatada = dataAtual.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))

            val nota = Nota(titulo = titulo, conteudo = conteudo, data = dataFormatada)
            notaViewModel.insertNota(nota)
        }

        if(requestCode == verificarEditarNotaReqCode && resultCode == Activity.RESULT_OK) {
            val titulo = data?.getStringExtra(VerEditarNotaActivity.REPLY_TITLE).toString()
            val conteudo = data?.getStringExtra(VerEditarNotaActivity.REPLY_CONTENT).toString()
            val dataNota = data?.getStringExtra(VerEditarNotaActivity.REPLY_DATA).toString()
            val id = data?.getStringExtra(VerEditarNotaActivity.REPLY_ID).toString().toInt()


            val nota = Nota(id = id, titulo = titulo, conteudo = conteudo, data = dataNota)
            notaViewModel.updateNota(nota)
        }

        if(requestCode == verificarEditarNotaReqCode && resultCode == VerEditarNotaActivity.RESULT_REMOVE) {
            val id = data?.getStringExtra(VerEditarNotaActivity.REPLY_ID).toString().toInt()

            notaViewModel.deleteNota(id = id)
        }
    }
}