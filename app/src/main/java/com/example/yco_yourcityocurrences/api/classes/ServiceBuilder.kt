package com.example.yco_yourcityocurrences.api.classes

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ServiceBuilder {

    //Logger dos requests
    //TODO("REMOVER PARA VERSÃO FINAL")
    val logging : HttpLoggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    private val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

    private val retrofit = Retrofit.Builder()
            //Colocar o ip antes de lançar a app
        .baseUrl("http://192.168.1.8:80/yco_ws/index.php/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun<T> buildService(service: Class<T>): T {
        return  retrofit.create(service)
    }
}