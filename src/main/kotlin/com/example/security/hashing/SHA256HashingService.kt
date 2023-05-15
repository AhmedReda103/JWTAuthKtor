package com.example.security.hashing

import com.mongodb.internal.HexUtils
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.security.SecureRandom

class SHA256HashingService : HashingService {

    override fun generateSaltedHash(value: String, saltLength: Int): SaltedHash {
        val salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLength)
        val saltedAsHex = Hex.encodeHexString(salt)
        val hash = DigestUtils.sha256Hex("$saltedAsHex$value")

        return SaltedHash(
            hash = hash ,
            salt = saltedAsHex
        )
    }

    override fun verify(value: String, saltedHash: SaltedHash): Boolean {
        return DigestUtils.sha256Hex(saltedHash.salt+value) == saltedHash.hash
    }
}