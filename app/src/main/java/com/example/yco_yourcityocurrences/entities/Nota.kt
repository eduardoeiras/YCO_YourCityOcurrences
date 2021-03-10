package com.example.yco_yourcityocurrences.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.sql.Date

@Entity(tableName = "notas_table")
class Nota (
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @ColumnInfo(name = "titulo") val titulo: String,
    @ColumnInfo(name = "conteudo") val conteudo: String,
    @ColumnInfo(name = "data") val data: String
) : Serializable