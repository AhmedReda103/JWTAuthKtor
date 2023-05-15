package com.example.data.models

interface UserDataSource {

    suspend fun getUserByName(name:String) :User?

    suspend fun insertUser(user:User):Boolean

}