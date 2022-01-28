package com.example.zohotask.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class UserDetailsEntity(
    @PrimaryKey
    val userId: String,
    @ColumnInfo(name = "first_name")
    val firstName: String?,
    @ColumnInfo(name = "last_name")
    val lastName: String?,
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String?,
    @ColumnInfo(name = "gender")
    val gender: String?,
    @ColumnInfo(name = "city")
    val city: String?,
    @ColumnInfo(name = "state")
    val state: String?,
    @ColumnInfo(name = "country")
    val country: String?,
    @ColumnInfo(name = "postcode")
    val postcode: String?,
    @ColumnInfo(name = "email")
    val email: String?,
    @ColumnInfo(name = "age")
    val age: String?,
    @ColumnInfo(name = "latitude")
    val latitude: String?,
    @ColumnInfo(name = "longitude")
    val longitude: String?,
    @ColumnInfo(name = "profile_pic")
    val profilePic: String?
)