package com.example.yco_yourcityocurrences.db

import androidx.lifecycle.LiveData
import com.example.yco_yourcityocurrences.dao.NotaDao
import com.example.yco_yourcityocurrences.entities.Nota

class NotaRepository(private val notaDao: NotaDao) {

    val todasNotas: LiveData<List<Nota>> = notaDao.getNotasPorDataMaisRecente()

    suspend fun insertNota(nota: Nota) {
        notaDao.inserirNota(nota)
    }

    suspend fun updateNota(nota: Nota) {
        notaDao.updateNota(nota)
    }

    suspend fun deleteNota(id: Int) {
        notaDao.deleteNota(id)
    }
}