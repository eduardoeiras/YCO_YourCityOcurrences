package com.example.yco_yourcityocurrences.api.classes

import com.example.yco_yourcityocurrences.api.classes.responses.LinhaOcorrencia
import com.example.yco_yourcityocurrences.api.classes.responses.Resposta
import com.example.yco_yourcityocurrences.api.classes.responses.RespostaOcorrencias
import retrofit2.Call
import retrofit2.http.*

interface EndPoints {

    @FormUrlEncoded
    @POST("utilizadores/realizarLogin")
    fun realizarLogin(@Field("nomeUser") nomeUser: String?, @Field("pwd") pwd: String?) : Call<Resposta>


    @GET("ocorrencias/getOcorrenciaUtilizador/{nomeUtilizador}")
    fun getOcorrenciasUtilizador(@Path("nomeUtilizador") nomeUser: String?) : Call<RespostaOcorrencias>

    @GET("ocorrencias/getOcorrenciasButUsers/{nomeUtilizador}")
    fun getOcorrenciasMenosUser(@Path("nomeUtilizador") nomeUser: String?) : Call<RespostaOcorrencias>

    @GET("ocorrencias/getAll")
    fun getAllOcorrencias() : Call<List<LinhaOcorrencia>>
}