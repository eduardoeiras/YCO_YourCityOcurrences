package com.example.yco_yourcityocurrences.ui.definicoes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.yco_yourcityocurrences.R

class DefinicoesFragment : Fragment() {

    private lateinit var definicoesViewModel: DefinicoesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        definicoesViewModel =
            ViewModelProvider(this).get(DefinicoesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_definicoes, container, false)
        val textView: TextView = root.findViewById(R.id.text_notifications)
        definicoesViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}