package com.example.yco_yourcityocurrences.api.classes.responses

data class RespostaOcorrencias (
        val data: List<LinhaOcorrencia>,
        val status: Boolean,
        val MSG: String
        )

data class LinhaOcorrencia(
        val ocorrencia: Ocorrencia,
        val utilizador: List<Utilizador>
)

data class Ocorrencia(
        val id_ocorrencia: Int,
        val titulo: String,
        val descricao: String,
        val tipo: String,
        val imagem: String,
        val dataComunicacao: String,
        val latitude: String,
        val longitude: String,
        val nomeUtilizador: String
)

data class Utilizador(
        val nomeCompleto: String
)

data class RespostaOcorrenciasRaio (
        val data: List<Ocorrencia>,
        val status: Boolean,
        val MSG: String
)
