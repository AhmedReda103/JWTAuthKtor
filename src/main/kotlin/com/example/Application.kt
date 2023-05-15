package com.example

import com.auth0.jwt.JWT
import com.example.data.models.MongoUserDataSource
import com.example.data.models.User
import io.ktor.server.application.*
import com.example.plugins.*
import com.example.security.hashing.SHA256HashingService
import com.example.security.token.JwtTokenService
import com.example.security.token.TokenConfig
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    val mongoPw = System.getenv("MONGO_PW")
    val dbName = "ktor-auth"
    val db = KMongo.createClient(
        connectionString = "mongodb+srv://ahmed:ahmed@cluster0.yg1wenl.mongodb.net/$dbName?retryWrites=true&w=majority"
    ).coroutine
        .getDatabase(dbName)

    val userDataSource = MongoUserDataSource(db)
    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString() ,
        expireIn = 360L *1000L * 60L *60L *24L ,
        secret = "jwt-secret"
    )

    val hashingService = SHA256HashingService()




//    GlobalScope.launch {
//        val user = User(
//            "ahmed123" ,
//            "ahmed",
//            "salt"
//        )
//        userDataSource.insertUser(user)
//    }

    configureSecurity(tokenConfig)
    configureSerialization()
    configureMonitoring()
    configureRouting(userDataSource, tokenService, hashingService, tokenConfig)
}
