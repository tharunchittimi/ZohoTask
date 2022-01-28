package com.example.zohotask.network

import com.example.zohotask.model.UsersListResponse
import com.example.zohotask.model.WeatherReportResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface API {

    @GET
    fun getUserDetails(
        @Url url: String,
        @Query("results") results: String
        ): Call<UsersListResponse>

    @GET
    fun getLocationBasedWeatherDetails(
        @Url url: String,
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") appid: String
        ): Call<WeatherReportResponse>

}