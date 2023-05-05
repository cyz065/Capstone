package com.management.capstone.server

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface RetrofitAPI {
    @Multipart
    @POST("/images/")
    fun requestPicture(
        @Part body:MultipartBody.Part): Call<ResponsePicture>

    @POST("/scores/")
    fun requestScore(@Body body:RequestScore):Call<ResponseScore>

    @GET("/ranking/")
    fun getRanking():Call<List<ResponseRanking>>
}