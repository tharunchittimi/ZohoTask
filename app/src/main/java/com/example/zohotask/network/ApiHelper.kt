package com.example.zohotask.network


object ApiHelper {
    fun getApi(): API {
        return BaseRetrofit.getMyRetrofit().create(API::class.java)
    }
}