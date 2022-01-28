package com.example.zohotask.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zohotask.MyApplication
import com.example.zohotask.model.UsersListResponse
import com.example.zohotask.model.WeatherReportResponse
import com.example.zohotask.network.ApiHelper
import com.example.zohotask.room.DatabaseHelper
import com.example.zohotask.room.entity.UserDetailsEntity
import com.example.zohotask.util.SingleLiveEvent
import com.example.zohotask.util.Utils
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UsersListViewModel : ViewModel() {

    private var usersList = MutableLiveData<UsersListResponse>()
    private var weatherData = MutableLiveData<WeatherReportResponse>()
    private var loadingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private var errorMessage: SingleLiveEvent<String?> = SingleLiveEvent()
    private var isSeeMoreOrLess = MutableLiveData<Boolean>()

    init {
        if (Utils.isNetworkAvailable(MyApplication.getApplicationContext())) {
            DatabaseHelper.getUserDetailsBaseDao()?.deleteTable()
            getUsersListApi()
        } else {
            errorMessage.value = "No Internet Connection, Please try again!"
        }
    }

    fun setSeeMoreOrLessValue(isOpened: Boolean?) {
        this.isSeeMoreOrLess.value = isOpened
    }

    fun getSeeMoreOrLessValue(): LiveData<Boolean> {
        return isSeeMoreOrLess
    }

    fun getUsersListData(): LiveData<UsersListResponse> {
        return usersList
    }

    fun getWeatherData(): LiveData<WeatherReportResponse> {
        return weatherData
    }

    fun getErrorMessageData(): LiveData<String?> {
        return errorMessage
    }

    fun loadingObservable(): LiveData<Boolean> {
        return loadingLiveData
    }

    fun getUsersListApi() {
        loadingLiveData.value = true
        ApiHelper.getApi()
            .getUserDetails(
                "https://randomuser.me/api/",
                "10"
            )
            .enqueue(object : Callback<UsersListResponse> {
                override fun onResponse(
                    call: Call<UsersListResponse>,
                    response: Response<UsersListResponse>
                ) {
                    if (response.isSuccessful) {
                        usersList.value = response.body()
                        insertDataToDataBase(response.body()?.results)
                    } else {
                        loadingLiveData.value = false
                        Utils.showToast(MyApplication.getApplicationContext(), response.message())
                    }
                }

                override fun onFailure(call: Call<UsersListResponse>, t: Throwable) {
                    loadingLiveData.value = false
                    t.message?.let { Utils.showToast(MyApplication.getApplicationContext(), it) }
                }

            })
    }

    private fun insertDataToDataBase(userDetailsResponse: ArrayList<UsersListResponse.Result?>?) {
        viewModelScope.launch {
            for (i in userDetailsResponse ?: ArrayList()) {
                withContext(Dispatchers.IO) {
                    DatabaseHelper.getUserDetailsBaseDao()?.insert(
                        userDetailsResponse = UserDetailsEntity(
                            userId = i?.login?.uuid ?: "",
                            firstName = i?.name?.first,
                            lastName = i?.name?.last,
                            phoneNumber = i?.cell,
                            profilePic = i?.picture?.large,
                            gender = i?.gender,
                            city = i?.location?.city,
                            state = i?.location?.state,
                            country = i?.location?.country,
                            postcode = i?.location?.postcode,
                            email = i?.email,
                            age = i?.dob?.age,
                            latitude = i?.location?.coordinates?.latitude,
                            longitude = i?.location?.coordinates?.longitude
                        )
                    )
                }
            }
            loadingLiveData.value = false
        }
    }

    private var userDetails = MutableLiveData<ArrayList<UserDetailsEntity?>>()

    fun getUserDetailsFromDB(): LiveData<ArrayList<UserDetailsEntity?>> {
        return userDetails
    }

    fun getAllUserDetailsFromDB() {
        CoroutineScope(Dispatchers.Main).launch {
            val userList = async {
                DatabaseHelper.getUserDetailsBaseDao()?.getAllUserDetails()
            }
            updateDetails(userList.await())
        }
    }

    private fun updateDetails(await: List<UserDetailsEntity?>?) {
        userDetails.value = await as ArrayList<UserDetailsEntity?>
    }

    fun callWeatherApi(latitude: Double?, longitude: Double?) {
        ApiHelper.getApi()
            .getLocationBasedWeatherDetails(
                "https://api.openweathermap.org/data/2.5/weather",
                lat = latitude?.toString() ?: "",
                lon = longitude?.toString() ?: "",
                appid = "2edda5e65e6104ccd13ddb4c708ee19b"
            )
            .enqueue(object : Callback<WeatherReportResponse> {
                override fun onResponse(
                    call: Call<WeatherReportResponse>,
                    response: Response<WeatherReportResponse>
                ) {
                    if (response.isSuccessful) {
                        weatherData.value = response.body()
                    } else {
                        Utils.showToast(MyApplication.getApplicationContext(), response.message())
                    }
                }

                override fun onFailure(call: Call<WeatherReportResponse>, t: Throwable) {
                    t.message?.let { Utils.showToast(MyApplication.getApplicationContext(), it) }
                }

            })
    }

}