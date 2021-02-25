package com.example.yco_yourcityocurrences.ui.notas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.yco_yourcityocurrences.R

class NotasFragment : Fragment() {

    private lateinit var notasViewModel: NotasViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        notasViewModel =
            ViewModelProvider(this).get(NotasViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_notas, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        notasViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}