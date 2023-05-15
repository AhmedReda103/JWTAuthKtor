 package com.example.plugins

import com.example.authenticate
import com.example.data.models.MongoUserDataSource
import com.example.getSecretInfo
import com.example.security.hashing.HashingService
import com.example.security.token.TokenClaim
import com.example.security.token.TokenConfig
import com.example.security.token.TokenService
import com.example.signIn
import com.example.signUp
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*

fun Application.configureRouting(
    userDataSource: MongoUserDataSource ,
    tokenService: TokenService ,
    hashingService: HashingService ,
    tokenConfig: TokenConfig
) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        signIn(userDataSource, hashingService, tokenService, tokenConfig)

        signUp(hashingService, userDataSource)

        authenticate()

        getSecretInfo()




    }
}
