package com.example.yco_yourcityocurrences.ui.definicoes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DefinicoesViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is definições Fragment"
    }
    val text: LiveData<String> = _text
}