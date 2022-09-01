package de.esotechnik.phoehnlix.apiservice

import de.esotechnik.phoehnlix.apiservice.auth.SimpleJWT
import de.esotechnik.phoehnlix.apiservice.data.setupDatabase
import de.esotechnik.phoehnlix.ktor.setupForwardedFor
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.apache.commons.codec.binary.Base64
import java.security.SecureRandom

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@JvmOverloads
fun Application.module(testing: Boolean = false) {
  setupForwardedFor()
  setupDatabase()

  install(CORS) {
    allowMethod(HttpMethod.Options)
    allowMethod(HttpMethod.Get)
    allowMethod(HttpMethod.Post)
    allowMethod(HttpMethod.Put)
    allowMethod(HttpMethod.Delete)
    allowMethod(HttpMethod.Patch)
    allowHeader(HttpHeaders.Authorization)
    allowCredentials = true

    val corsHosts = this@module.environment.config.propertyOrNull("phoehnlix.corsHosts")?.getList() ?: run {
      error("No CORS hosts configured. Please specify your domains as phoehnlix.corsHosts")
    }
    corsHosts.forEach {
      if (it.startsWith("http://") || it.startsWith("https://")) {
        val (scheme, host) = it.split("://")
        allowHost(host, schemes = listOf(scheme))
      } else {
        allowHost(it)
      }
    }
  }
  install(ContentNegotiation) {
    json(Json {
        prettyPrint = true
    })
  }
  install(StatusPages) {
    exception<UnauthorizedException> { call, exception ->
      call.respond(HttpStatusCode.Unauthorized, mapOf("error" to exception.message))
    }
  }

  val secret = environment.config.propertyOrNull("phoehnlix.jwtSecret")?.getString() ?: run {
    val suggestedSecret = with(SecureRandom.getInstanceStrong()) {
      val secretBytes = ByteArray(32)
      nextBytes(secretBytes)
      Base64.encodeBase64URLSafeString(secretBytes)
    }

    error("No JWT secret configured. Here is a random value you might use: $suggestedSecret")
  }
  val simpleJwt = SimpleJWT(secret)
  install(Authentication) {
    jwt("phoehnlix-jwt") {
      verifier(simpleJwt.verifier)
      validate {
        with(simpleJwt) {
          it.getUserId()?.let { userId -> UserIdPrincipal(userId) }
        }
      }
    }
  }

  routing {
    route("/api") {
      apiservice(simpleJwt)
    }
  }
}


