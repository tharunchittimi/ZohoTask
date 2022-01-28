package com.example.zohotask.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

open class BaseOkHttp {
    private val NETWORK_CONNECTION_TIMEOUT = 60L

    fun provideOKHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(NETWORK_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(NETWORK_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NETWORK_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

}