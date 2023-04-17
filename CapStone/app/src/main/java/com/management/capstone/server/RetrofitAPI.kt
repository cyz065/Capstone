package com.management.capstone.server

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*


interface RetrofitAPI {
    @Multipart
    @POST("/images/")
    fun requestPicture(
        @Part body:MultipartBody.Part): Call<ResponsePicture>
}