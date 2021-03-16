package com.example.yco_yourcityocurrences.api.classes

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface EndPoints {

    @FormUrlEncoded
    @POST("utilizadores/realizarLogin")
    fun realizarLogin(@Field("nomeUser") nomeUser: String?, @Field("pwd") pwd: String?) : Call<Resposta>
}