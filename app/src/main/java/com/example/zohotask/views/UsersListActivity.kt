package com.example.zohotask.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.zohotask.BR
import com.example.zohotask.BuildConfig
import com.example.zohotask.R
import com.example.zohotask.adapter.UsersListAdapter
import com.example.zohotask.databinding.ActivityUsersListBinding
import com.example.zohotask.room.entity.UserDetailsEntity
import com.example.zohotask.util.Utils
import com.example.zohotask.util.Utils.UnixTime
import com.example.zohotask.util.Utils.getUnit
import com.example.zohotask.viewmodels.UsersListViewModel
import com.google.android.gms.location.*
import java.lang.String.valueOf
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*


class UsersListActivity : AppCompatActivity() {

    private var mViewDataBinding: ActivityUsersListBinding? = null
    private var mViewModel: UsersListViewModel? = null
    private var usersListAdapter: UsersListAdapter? = null
    private var myFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationCallback: LocationCallback? = null
    private var isSeeMoreOrLess: Boolean? = false

    companion object {
        const val USER_ID = "USER_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpFusedLocation()
        initBinding()
        setUpRecyclerView()
        setUpToolbar()
        addObserver()
        initSearchView()
        setViewsClickListeners()
        checkLocationPermission()
    }

    private fun setUpFusedLocation() {
        myFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
        } else {
            callLocation(true)
        }
    }

    private fun requestPermission() {
        locationPermissionRequestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private val locationPermissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                callLocation(true)
            } else {
                callLocation(false)
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    showSettingsDialog()
                }
            }
        }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setMessage("Please enable location permission")
            .setCancelable(false)
            .setPositiveButton("Settings") { p0, p1 ->
                val i = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                )
                onActivityResultForAccessLocationPermission.launch(i)
            }
            .setNegativeButton("Close") { p0, p1 ->
                p0.dismiss()
            }
            .create()
            .show()
    }

    private var onActivityResultForAccessLocationPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )

                != PackageManager.PERMISSION_GRANTED
            ) {
                callLocation(false)
            } else {
                callLocation(true)
            }
        }

    private fun getLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        return locationRequest
    }

    @SuppressLint("MissingPermission")
    private fun callLocation(locationStatus: Boolean) {
        if (locationStatus) {
            myFusedLocationClient?.lastLocation?.addOnSuccessListener {
                setCurrentLocation(
                    it?.latitude,
                    it?.longitude
                )
            }
            myFusedLocationClient?.lastLocation?.addOnFailureListener {
                setCurrentLocation(0.0, 0.0)
            }
            mLocationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    setCurrentLocation(
                        locationResult.lastLocation.latitude,
                        locationResult.lastLocation.longitude
                    )
                    Log.e(
                        "locations",
                        "${locationResult.lastLocation.latitude}, ${locationResult.lastLocation.longitude}"
                    )
                }
            }
            myFusedLocationClient?.requestLocationUpdates(
                getLocationRequest(),
                mLocationCallback as LocationCallback,
                Looper.getMainLooper()
            )
        } else {
            setCurrentLocation(null, null)
        }
    }

    private fun setCurrentLocation(latitude: Double?, longitude: Double?) {
        if (Utils.isNetworkAvailable(this)) {
            mViewModel?.callWeatherApi(latitude, longitude)
        }
    }

    private fun setUpToolbar() {
        mViewDataBinding?.toolbarUsersList?.txtToolbarHeading?.visibility = View.VISIBLE
        mViewDataBinding?.toolbarUsersList?.txtToolbarHeading?.text = "USERS LIST"
    }

    private fun initBinding() {
        mViewDataBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_users_list)
        mViewDataBinding?.lifecycleOwner = this
        this.mViewModel = ViewModelProvider(this)[UsersListViewModel::class.java]
        mViewDataBinding?.setVariable(BR.usersListViewModel, mViewModel)
    }

    private fun setUpRecyclerView() {
        usersListAdapter = UsersListAdapter()
        mViewDataBinding?.rvUserDetails?.apply {
            layoutManager = LinearLayoutManager(this@UsersListActivity)
            adapter = usersListAdapter
        }

        usersListAdapter?.setOnItemCommunicatorListener(object : UsersListAdapter.Communicator {
            override fun showNoDataMessage(isDataNotAvailable: Boolean) {
                if (isDataNotAvailable) {
                    Utils.showToast(this@UsersListActivity, "No Data Available")
                }
            }

            override fun onItemClick(userDetailsResponse: UserDetailsEntity?) {
                startActivity(
                    Intent(this@UsersListActivity, UserDetailsActivity::class.java).putExtra(
                        USER_ID, userDetailsResponse?.userId
                    )
                )
            }
        })
    }

    private fun addObserver() {
        mViewModel?.getErrorMessageData()?.observe(this, {
            Utils.showToast(this, it ?: "")
        })
        mViewModel?.loadingObservable()?.observe(this, {
            if (it == true) {
                startLoading()
            } else {
                getDataFromDataBase()
            }
        })
        mViewModel?.getUserDetailsFromDB()?.observe(this, {
            finishLoading()
            usersListAdapter?.addUserList(it)
            mViewDataBinding?.rvUserDetails?.visibility = View.VISIBLE
            mViewDataBinding?.searchView?.toolbarRootLayoutSearchFilter?.visibility = View.VISIBLE
        })
        mViewModel?.getWeatherData()?.observe(this, {
            mViewDataBinding?.weatherReport?.tvCloudsHeading?.text =
                it?.weather?.get(0)?.let { weather ->
                    return@let "${weather.main} : ${weather.description}"
                }
            mViewDataBinding?.weatherReport?.tvTemp?.text =
                it?.main?.temp?.let { temperature ->
                    return@let "Temperature : $temperature ${getUnit(Locale.getDefault().country)}"
                }
            mViewDataBinding?.weatherReport?.tvHumidity?.text =
                it?.main?.humidity?.let { humidity ->
                    return@let "Humidity : $humidity per cent"
                }
            mViewDataBinding?.weatherReport?.tvWindMin?.text =
                it?.main?.tempMin?.let { tempMin ->
                    return@let "Wind : $tempMin min"
                }
            mViewDataBinding?.weatherReport?.tvWindMax?.text =
                it?.main?.tempMax?.let { tempMax ->
                    return@let "Wind : $tempMax max"
                }
            mViewDataBinding?.weatherReport?.tvWindSpeed?.text =
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
            mViewDataBinding?.weatherReport?.tvLocationName?.text = "Location : $data"

            mViewDataBinding?.weatherReport?.tvSunRise?.text =
                it?.sys?.sunrise?.let { sunrise ->
                    return@let "Sunrise : ${UnixTime(valueOf(sunrise).toLong())}"
                }
            mViewDataBinding?.weatherReport?.tvSunSet?.text =
                it?.sys?.sunset?.let { sunset ->
                    return@let "Sunset : ${UnixTime(valueOf(sunset).toLong())}"
                }
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
            mViewDataBinding?.weatherReport?.imgClouds?.setImageResource(weatherImage)

            mViewDataBinding?.weatherReport?.rootLayoutWeather?.visibility = View.VISIBLE
        })

        mViewModel?.getSeeMoreOrLessValue()?.observe(this, {
            showOrHideWeatherDetails(it)
        })
    }

    private fun getDataFromDataBase() {
        mViewModel?.getAllUserDetailsFromDB()
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

    private fun initSearchView() {
        mViewDataBinding?.searchView?.editTextSearchView?.addTextChangedListener(object :
            TextWatcher {
            private val handler = Handler(Looper.getMainLooper())
            private var runnable: Runnable? = null
            override fun afterTextChanged(newText: Editable) {
                runnable?.let {
                    handler.removeCallbacks(it)
                }
                runnable = Runnable {
                    if (newText.trim().length <= 0) {
                        usersListAdapter?.showSearchedList("")
                    } else {
                        usersListAdapter?.showSearchedList(newText.toString())
                    }
                }
                handler.postDelayed(runnable!!, 400L)
            }

            override fun beforeTextChanged(
                newText: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(newText: CharSequence, start: Int, before: Int, count: Int) {
                if (newText.length <= 0) {
                    mViewDataBinding?.searchView?.tvSearchCloseIcon?.visibility = View.GONE
                } else {
                    mViewDataBinding?.searchView?.tvSearchCloseIcon?.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setViewsClickListeners() {
        mViewDataBinding?.searchView?.tvSearchCloseIcon?.setOnClickListener {
            mViewDataBinding?.searchView?.editTextSearchView?.setText("")
        }
        mViewDataBinding?.weatherReport?.imgMoreLess?.setOnClickListener {
            isSeeMoreOrLess = isSeeMoreOrLess == false
            mViewModel?.setSeeMoreOrLessValue(isSeeMoreOrLess)
        }
    }

    private fun showOrHideWeatherDetails(seeMoreOrLess: Boolean?) {
        if (seeMoreOrLess == true) {
            mViewDataBinding?.weatherReport?.grpWeather?.visibility = View.VISIBLE
            mViewDataBinding?.weatherReport?.tvWeatherReportHeading?.visibility = View.GONE
            mViewDataBinding?.weatherReport?.imgMoreLess?.setImageResource(R.drawable.ic_up)
        } else {
            mViewDataBinding?.weatherReport?.grpWeather?.visibility = View.GONE
            mViewDataBinding?.weatherReport?.tvWeatherReportHeading?.visibility = View.VISIBLE
            mViewDataBinding?.weatherReport?.imgMoreLess?.setImageResource(R.drawable.ic_down)
        }
    }

}