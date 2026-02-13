package com.example.notify.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.cloudinary.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: CloudinaryService = retrofit.create(CloudinaryService::class.java)
}
