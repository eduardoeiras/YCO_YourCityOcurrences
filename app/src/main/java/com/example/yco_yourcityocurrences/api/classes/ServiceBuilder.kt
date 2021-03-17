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

    val certificatePinner = CertificatePinner.Builder()
            //ACIÇÃO DAS CHAVES DE ENCRIPTAÇÃO PARA O WEBSITE QUE CONTÉM A CAMADA DE SERVIÇOS REST DESENVOLVIDA
            .add("ycowebservices.000webhostapp.com", "sha256/EnbMaM1S7402Ax4qI67HpYrx3N/stQMIIkFO12P82Ak=")
            .add("ycowebservices.000webhostapp.com", "sha256/nKWcsYrc+y5I8vLf1VGByjbt+Hnasjl+9h8lNKJytoE=")
            .add("ycowebservices.000webhostapp.com", "sha256/r/mIkG3eEpVdm+u/ko/cwxzOMo1bk4TyHIlByibiA5E=")
            .build()

    private val client = OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .addInterceptor(logging)
            .build()



    private val retrofit = Retrofit.Builder()
        .baseUrl("https://ycowebservices.000webhostapp.com/yco_ws/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun<T> buildService(service: Class<T>): T {
        return  retrofit.create(service)
    }
}