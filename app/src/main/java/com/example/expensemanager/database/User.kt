package com.example.expensemanager.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)] //tạo mục lục để tăng tốc độ tìm kiếm khi truy vấn
)

data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val password: String,
    val fullName: String,
    val dateOfBirth: String,
    val address: String,
    val occupation: String,
){

}