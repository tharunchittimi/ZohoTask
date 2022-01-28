package com.example.zohotask.room

import androidx.room.Room
import com.example.zohotask.MyApplication
import com.example.zohotask.room.dao.UserDetailsDao

object DatabaseHelper {

    private var appDataBase: AppDataBase? = null

    private const val DataBaseName = "Internal_DataBase.db"

    fun getUserDetailsBaseDao(): UserDetailsDao? {
        if (appDataBase == null) {
            appDataBase = Room.databaseBuilder(
                MyApplication.getApplicationContext(),
                AppDataBase::class.java, DataBaseName
            ).allowMainThreadQueries().build()
        }
        return appDataBase?.usersDao()
    }
}