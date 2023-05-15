package com.example.security.token

data class TokenConfig (
    val issuer : String ,
    val audience :String ,
    val expireIn :Long  ,
    val secret : String
)