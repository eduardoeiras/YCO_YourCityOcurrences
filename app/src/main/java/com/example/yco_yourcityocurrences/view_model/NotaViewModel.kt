package com.example.yco_yourcityocurrences.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.yco_yourcityocurrences.db.NotaRepository
import com.example.yco_yourcityocurrences.db.NotasDB
import com.example.yco_yourcityocurrences.entities.Nota
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotaViewModel(application: Application) : AndroidViewModel(application){
    private val repository: NotaRepository

    val todasNotas: LiveData<List<Nota>>

    init {
        val notaDao = NotasDB.getDatabase(application, viewModelScope).notaDao()
        repository = NotaRepository(notaDao)
        todasNotas = repository.todasNotas

    }

    //Corotina de inserção da nota para não bloquear a UI
    fun insertNota(nota: Nota) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertNota(nota)
    }

    fun updateNota(nota: Nota) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateNota(nota)
    }

    fun deleteNota(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteNota(id)
    }
}