package com.example.notify.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class CloudinaryResponse(
    val secure_url: String
)

interface CloudinaryService {

    @Multipart
    @POST("v1_1/de6erh571/image/upload") // 👈 apna cloud name
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("upload_preset") preset: RequestBody
    ): Response<CloudinaryResponse>
}
