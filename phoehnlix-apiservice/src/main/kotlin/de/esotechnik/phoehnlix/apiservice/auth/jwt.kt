package de.esotechnik.phoehnlix.apiservice.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.auth.jwt.JWTCredential
import org.apache.commons.codec.binary.Base64

/**
 * @author Bernhard Frauendienst
 */
class SimpleJWT(base64secret: String) {
  private val algorithm = Algorithm.HMAC256(Base64.decodeBase64(base64secret))
  val verifier = JWT.require(algorithm).build()
  fun sign(userId: String): String = JWT.create().withClaim("userId", userId).sign(algorithm)
  fun JWTCredential.getUserId(): String? = payload.getClaim("userId").asString()
}

