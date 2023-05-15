package com.example

import com.example.data.models.MongoUserDataSource
import com.example.data.models.User
import com.example.data.requests.AuthRequest
import com.example.data.responses.AuthResponse
import com.example.security.hashing.HashingService
import com.example.security.hashing.SaltedHash
import com.example.security.token.TokenClaim
import com.example.security.token.TokenConfig
import com.example.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.apache.commons.codec.digest.DigestUtils

fun Route.signUp(
    hashingService : HashingService ,
    userDataSource: MongoUserDataSource
){
    post("signup") {
        val request = call.receiveOrNull<AuthRequest>() ?: run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val areFieldsBlank = request.username.isBlank() || request.password.isBlank()
        val isPwIsTooShort = request.password.length <6

        if(areFieldsBlank || isPwIsTooShort){
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = User(username = request.username , password = saltedHash.hash , salt = saltedHash.salt )

        val wasAcknowledge = userDataSource.insertUser(user)
        if(!wasAcknowledge){
            call.respond(HttpStatusCode.Conflict)
            return@post
        }
        call.respond(HttpStatusCode.OK)
    }
}


fun Route.signIn(
    userDataSource: MongoUserDataSource ,
    hashingService: HashingService ,
    tokenService: TokenService,
    tokenConfig: TokenConfig
){

    post("signin") {
        val request = call.receiveOrNull<AuthRequest>() ?: run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = userDataSource.getUserByName(request.username)
        if(user==null){
            call.respond(HttpStatusCode.Conflict , "Incorrect username or password ")
            return@post
        }

        val isValidPassword = hashingService.verify(
            value = request.password ,
            saltedHash = SaltedHash(
                salt = user.salt ,
                hash = user.password
            )
        )

        if(!isValidPassword){
            println("Entered hash: ${DigestUtils.sha256Hex("${user.salt}${request.password}")}, Hashed PW: ${user.password}")
            call.respond(HttpStatusCode.Conflict , "Incorrect username or password ")
            return@post
        }

        val token = tokenService.generate(config = tokenConfig , TokenClaim(
            name = "userId" ,
            value = user.id.toString()
        ))

        call.respond(HttpStatusCode.OK , message = AuthResponse(
            token = token
        ))
    }


}



fun Route.authenticate(){
    authenticate {
        get("authenticate"){
            call.respond(HttpStatusCode.OK)
        }
    }
}


fun Route.getSecretInfo(){
    authenticate {
        get("secret"){
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId" , String::class)
            call.respond(HttpStatusCode.OK , "your user id is $userId")
        }
    }
}





