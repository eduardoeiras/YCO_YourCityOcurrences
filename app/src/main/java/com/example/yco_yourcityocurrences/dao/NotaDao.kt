package com.example.yco_yourcityocurrences.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.yco_yourcityocurrences.entities.Nota

@Dao
interface NotaDao {

    @Query("SELECT * FROM notas_table ORDER BY id DESC")
    fun getNotasPorDataMaisRecente(): LiveData<List<Nota>>

    @Update
    suspend fun updateNota(nota: Nota)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserirNota(nota: Nota)

    @Query("DELETE FROM notas_table WHERE id==:id")
    suspend fun deleteNota(id: Int)
}