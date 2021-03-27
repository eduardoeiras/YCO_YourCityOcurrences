package com.example.yco_yourcityocurrences.api.classes

import com.example.yco_yourcityocurrences.api.classes.responses.LinhaOcorrencia
import com.example.yco_yourcityocurrences.api.classes.responses.Resposta
import com.example.yco_yourcityocurrences.api.classes.responses.RespostaImg
import com.example.yco_yourcityocurrences.api.classes.responses.RespostaOcorrencias
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import java.math.BigDecimal
import java.util.*

interface EndPoints {

    //Endpoint para a realização do login
    @FormUrlEncoded
    @POST("utilizadores/realizarLogin")
    fun realizarLogin(@Field("nomeUser") nomeUser: String?, @Field("pwd") pwd: String?) : Call<Resposta>

    //Endpoints de obtenção de ocorrências
    @GET("ocorrencias/getOcorrenciaUtilizador/{nomeUtilizador}")
    fun getOcorrenciasUtilizador(@Path("nomeUtilizador") nomeUser: String?) : Call<RespostaOcorrencias>

    @GET("ocorrencias/getOcorrenciasButUsers/{nomeUtilizador}")
    fun getOcorrenciasMenosUser(@Path("nomeUtilizador") nomeUser: String?) : Call<RespostaOcorrencias>

    @GET("ocorrencias/getAll")
    fun getAllOcorrencias() : Call<List<LinhaOcorrencia>>

    @GET("ocorrencias/getOcorrencia/{id}")
    fun getOcorrenciaPorId(@Path("id") id: Int?) : Call<RespostaOcorrencias>

    //Endpoints de atualização das ocorrências
    @FormUrlEncoded
    @POST("ocorrencias/atualizarOcorrencia/{id}")
    fun atualizarOcorrencia(@Path("id") id: Int?, @Field("titulo") titulo: String?,
                            @Field("desc") desc: String?, @Field("tipo") tipo: String?,
                            @Field("imagem") imagem: String?) : Call<Resposta>

    //Endpoint para a submissão de uma imagem
    @Multipart
    @POST("ocorrencias/submeterImagem")
    fun submeterImagem(@Part imagem :MultipartBody.Part, @Part("name") name: RequestBody) : Call<RespostaImg>

    //Endpoint para a criação de uma nova ocorrência
    @FormUrlEncoded
    @POST("ocorrencias/registar")
    fun criarOcorrencia(@Field("titulo") titulo: String?, @Field("desc") desc: String?,
                        @Field("imagem") imagem: String?, @Field("tipo") tipo: String?, @Field("dataComunicacao") dataComunicacao: String?,
                        @Field("latitude") latitude: BigDecimal?, @Field("longitude") longitude: BigDecimal?,
                        @Field("nomeUtilizador") nomeUtilizador: String?
    ) : Call<Resposta>

    //Endpoint para a remoção de uma ocorrência
    @DELETE("ocorrencias/deleteOcorrencia/{id}")
    fun removerOcorrencia(@Path("id") id: Int?) : Call<Resposta>
}