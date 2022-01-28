package com.example.zohotask.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.zohotask.room.dao.UserDetailsDao
import com.example.zohotask.room.entity.UserDetailsEntity

@Database(
    entities = [UserDetailsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun usersDao(): UserDetailsDao
}