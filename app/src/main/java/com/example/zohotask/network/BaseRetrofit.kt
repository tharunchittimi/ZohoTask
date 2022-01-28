package com.example.zohotask.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object BaseRetrofit : BaseOkHttp() {

    private var retrofit: Retrofit? = null
    private const val baseUrl = "https://randomuser.me/api/"

    init {
        createRetrofit()
    }

    fun getMyRetrofit(): Retrofit {
        if (retrofit == null) {
            createRetrofit()
        }
        return retrofit!!
    }

    private fun createRetrofit() {
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(provideOKHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}