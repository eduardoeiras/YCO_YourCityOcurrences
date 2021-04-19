package com.example.yco_yourcityocurrences.api.classes.responses

data class RespostaTipo (
        val data: List<Tipo>,
        val status: Boolean,
        val MSG: String
)

data class Tipo (
        val id: Int,
        val nome: String,
)