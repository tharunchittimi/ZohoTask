package com.example.zohotask.room.dao

import androidx.room.*
import com.example.zohotask.room.entity.UserDetailsEntity

@Dao
interface UserDetailsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userDetailsResponse: UserDetailsEntity)

    @Query("SELECT * FROM userdetailsentity")
    fun getAllUserDetails():List<UserDetailsEntity>

    @Query("SELECT * FROM userdetailsentity where userId=:userId")
    fun getUserDetailsById(userId:String):UserDetailsEntity?

    @Query("DELETE FROM userdetailsentity")
    fun deleteTable()
}