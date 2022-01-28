package com.example.zohotask.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.zohotask.MyApplication
import com.example.zohotask.model.WeatherReportResponse
import com.example.zohotask.network.ApiHelper
import com.example.zohotask.room.DatabaseHelper
import com.example.zohotask.room.entity.UserDetailsEntity
import com.example.zohotask.util.SingleLiveEvent
import com.example.zohotask.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserDetailsViewModel : ViewModel() {

    private var userData = MutableLiveData<UserDetailsEntity>()
    private var weatherData = MutableLiveData<WeatherReportResponse>()
    private var loadingLiveData: MutableLiveData<Boolean> = MutableLiveData()

    fun getUserData(): LiveData<UserDetailsEntity> {
        return userData
    }

    fun loadingObservable(): LiveData<Boolean> {
        return loadingLiveData
    }

    fun getWeatherData(): LiveData<WeatherReportResponse> {
        return weatherData
    }

    fun getUserDataFromDb(userId: String?) {
        CoroutineScope(Dispatchers.Main).launch {
            val userList = async {
                userId?.let { DatabaseHelper.getUserDetailsBaseDao()?.getUserDetailsById(it) }
            }
            updateDetails(userList.await())
        }
    }

    private fun updateDetails(await: UserDetailsEntity?) {
        userData.value = await
    }

    fun callWeatherApi(latitude: String?, longitude: String?) {
        loadingLiveData.value = true
        ApiHelper.getApi()
            .getLocationBasedWeatherDetails(
                "https://api.openweathermap.org/data/2.5/weather",
                lat = latitude ?: "",
                lon = longitude ?: "",
                appid = "2edda5e65e6104ccd13ddb4c708ee19b"
            )
            .enqueue(object : Callback<WeatherReportResponse> {
                override fun onResponse(
                    call: Call<WeatherReportResponse>,
                    response: Response<WeatherReportResponse>
                ) {
                    loadingLiveData.value = false
                    if (response.isSuccessful) {
                        weatherData.value = response.body()
                    } else {
                        Utils.showToast(MyApplication.getApplicationContext(), response.message())
                    }
                }

                override fun onFailure(call: Call<WeatherReportResponse>, t: Throwable) {
                    loadingLiveData.value = false
                    t.message?.let { Utils.showToast(MyApplication.getApplicationContext(), it) }
                }

            })
    }
}