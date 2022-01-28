package com.example.zohotask.views

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.zohotask.BR
import com.example.zohotask.R
import com.example.zohotask.databinding.ActivityUserDetailsBinding
import com.example.zohotask.util.Utils
import com.example.zohotask.util.Utils.UnixTime
import com.example.zohotask.util.Utils.getUnit
import com.example.zohotask.viewmodels.UserDetailsViewModel
import okhttp3.internal.Util
import java.lang.StringBuilder
import java.util.*

class UserDetailsActivity : AppCompatActivity() {

    private var mViewDataBinding: ActivityUserDetailsBinding? = null
    private var mViewModel: UserDetailsViewModel? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getIntentData()
        initBinding()
        setUpToolbar()
        getDataFromDataBase()
        addObserver()
    }

    private fun setCurrentLocation(latitude: String?, longitude: String?) {
        if (Utils.isNetworkAvailable(this)) {
            mViewModel?.callWeatherApi(latitude, longitude)
        } else {
            mViewDataBinding?.llvLoadingLayout?.visibility = View.GONE
            mViewDataBinding?.grpWeather?.visibility = View.GONE
            Utils.showToast(this, "No Internet Connection, Please try again!")
        }
    }

    private fun setUpToolbar() {
        mViewDataBinding?.toolbarUserDetails?.txtToolbarBack?.visibility = View.VISIBLE
        mViewDataBinding?.toolbarUserDetails?.txtToolbarHeading?.visibility = View.VISIBLE
        mViewDataBinding?.toolbarUserDetails?.txtToolbarHeading?.text = "USER PROFILE"
        mViewDataBinding?.toolbarUserDetails?.txtToolbarBack?.setOnClickListener {
            onBackPressed()
        }
    }

    private fun addObserver() {
        mViewModel?.getUserData()?.observe(this, { data ->
            mViewDataBinding?.imgProfilePic?.let {
                Glide.with(it.context).load(data?.profilePic)
                    .apply(RequestOptions.circleCropTransform()).placeholder(R.drawable.no_img)
                    .error(R.drawable.no_img).into(it)
            }
            mViewDataBinding?.txtProfileName?.text = "${data?.firstName} ${data?.lastName}"
            mViewDataBinding?.txtUserEmail?.text = "Email  : ${data?.email}"
            mViewDataBinding?.txtUserPhone?.text = "Phone Number  : ${data?.phoneNumber}"
            mViewDataBinding?.txtUserAge?.text = "Age  : ${data?.age}"
            mViewDataBinding?.txtUserAddress?.text =
                "Address  : ${data?.city}, ${data?.state}, ${data?.country}, ${data?.postcode}"
            setCurrentLocation(latitude = data?.latitude, longitude = data?.longitude)
        })
        mViewModel?.loadingObservable()?.observe(this, {
            if (it == true) {
                startLoading()
            } else {
                finishLoading()
            }
        })

        mViewModel?.getWeatherData()?.observe(this, {
            mViewDataBinding?.weatherReportDetails?.tvCloudsHeading?.text =
                it?.weather?.get(0)?.let { weather ->
                    return@let "${weather.main} : ${weather.description}"
                }
            mViewDataBinding?.weatherReportDetails?.tvTemp?.text =
                it?.main?.temp?.let { temperature ->
                    return@let "Temperature : $temperature ${getUnit(Locale.getDefault().country)}"
                }
            mViewDataBinding?.weatherReportDetails?.tvHumidity?.text =
                it?.main?.humidity?.let { humidity ->
                    return@let "Humidity : $humidity per cent"
                }
            mViewDataBinding?.weatherReportDetails?.tvWindMin?.text =
                it?.main?.tempMin?.let { tempMin ->
                    return@let "Wind : $tempMin min"
                }
            mViewDataBinding?.weatherReportDetails?.tvWindMax?.text =
                it?.main?.tempMax?.let { tempMax ->
                    return@let "Wind : $tempMax max"
                }
            mViewDataBinding?.weatherReportDetails?.tvWindSpeed?.text =
                it?.wind?.speed?.let { speed ->
                    return@let "Wind Speed : $speed miles/hour"
                }

            val data = StringBuilder()
            if (it?.name?.length ?: 0 > 0) {
                data.append(it.name)
            }
            if (it.sys?.country?.length ?: 0 > 0) {
                data.append(", ${it.sys?.country}")
            }
            mViewDataBinding?.weatherReportDetails?.tvLocationName?.text = "Location : $data"

            mViewDataBinding?.weatherReportDetails?.tvSunRise?.text =
                it?.sys?.sunrise?.let { sunrise ->
                    return@let "Sunrise : ${UnixTime(java.lang.String.valueOf(sunrise).toLong())}"
                }
            mViewDataBinding?.weatherReportDetails?.tvSunSet?.text =
                it?.sys?.sunset?.let { sunset ->
                    return@let "Sunset : ${UnixTime(java.lang.String.valueOf(sunset).toLong())}"
                }
            Log.e("a", "${it?.weather?.get(0)?.icon}")

            val weatherImage = when (it?.weather?.get(0)?.icon) {
                "01d" -> R.drawable.sunny
                "02d" -> R.drawable.cloud
                "03d" -> R.drawable.cloud
                "04d" -> R.drawable.cloud
                "04n" -> R.drawable.cloud
                "10d" -> R.drawable.rain
                "11d" -> R.drawable.storm
                "13d" -> R.drawable.snowflake
                "01n" -> R.drawable.cloud
                "02n" -> R.drawable.cloud
                "03n" -> R.drawable.cloud
                "10n" -> R.drawable.cloud
                "11n" -> R.drawable.rain
                "13n" -> R.drawable.snowflake
                else -> {
                    R.drawable.sunny
                }
            }
            mViewDataBinding?.weatherReportDetails?.imgClouds?.setImageResource(weatherImage)


            mViewDataBinding?.grpWeather?.visibility = View.VISIBLE
            mViewDataBinding?.weatherReportDetails?.rootLayoutWeather?.visibility = View.VISIBLE
            mViewDataBinding?.weatherReportDetails?.tvWeatherReportHeading?.visibility = View.GONE
            mViewDataBinding?.weatherReportDetails?.grpWeather?.visibility = View.VISIBLE
            mViewDataBinding?.weatherReportDetails?.imgMoreLess?.visibility = View.GONE
        })
    }

    private fun startLoading() {
        mViewDataBinding?.llvLoadingLayout?.visibility = View.VISIBLE
        mViewDataBinding?.imgLoading?.let {
            Glide.with(it.context).asGif()
                .load(R.drawable.loading_gif)
                .into(it)
        }
    }

    private fun finishLoading() {
        mViewDataBinding?.llvLoadingLayout?.visibility = View.GONE
    }

    private fun getIntentData() {
        if (intent?.hasExtra(UsersListActivity.USER_ID) == true) {
            userId = intent?.getStringExtra(UsersListActivity.USER_ID)
        }
    }

    private fun getDataFromDataBase() {
        mViewModel?.getUserDataFromDb(userId)
    }

    private fun initBinding() {
        mViewDataBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_user_details)
        mViewDataBinding?.lifecycleOwner = this
        this.mViewModel = ViewModelProvider(this)[UserDetailsViewModel::class.java]
        mViewDataBinding?.setVariable(BR.userDetailsViewModel, mViewModel)
    }

}