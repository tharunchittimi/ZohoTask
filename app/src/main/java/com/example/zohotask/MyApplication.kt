package com.example.zohotask

import android.app.Application
import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner


class MyApplication : Application(), LifecycleObserver {
    init {
        myApplication = this
    }

    companion object {
        private lateinit var myApplication: Application
        fun getApplicationContext(): Context {
            return myApplication
        }
    }

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

}