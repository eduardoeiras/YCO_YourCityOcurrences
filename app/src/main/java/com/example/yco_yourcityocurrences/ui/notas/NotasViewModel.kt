package com.example.yco_yourcityocurrences.ui.notas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotasViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is notas Fragment"
    }
    val text: LiveData<String> = _text
}