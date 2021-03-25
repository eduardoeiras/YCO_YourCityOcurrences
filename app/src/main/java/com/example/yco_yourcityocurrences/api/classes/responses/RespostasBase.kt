package com.example.yco_yourcityocurrences.api.classes.responses

data class Resposta(
    val status: Boolean,
    val MSG: String
)

data class RespostaImg(
        val urlImagem: String,
        val status: Boolean,
        val MSG: String
)