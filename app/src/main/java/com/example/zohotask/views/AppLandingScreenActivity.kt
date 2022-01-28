package com.example.zohotask.views

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.zohotask.BR
import com.example.zohotask.R
import com.example.zohotask.databinding.ActivityAppLandingScreenBinding
import com.example.zohotask.viewmodels.AppLandingScreenViewModel

class AppLandingScreenActivity : AppCompatActivity() {

    private var mViewDataBinding: ActivityAppLandingScreenBinding? = null
    private var mViewModel: AppLandingScreenViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
    }

    private fun initBinding() {
        mViewDataBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_app_landing_screen)
        mViewDataBinding?.lifecycleOwner = this
        this.mViewModel = ViewModelProvider(this)[AppLandingScreenViewModel::class.java]
        mViewDataBinding?.setVariable(BR.appLandingScreenViewModel, mViewModel)
        mViewDataBinding?.executePendingBindings()
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, UsersListActivity::class.java))
            finish()
        }, 2000)
    }
}