package de.esotechnik.phoehnlix.dataservice

import de.esotechnik.phoehnlix.api.client.ApiClient
import de.esotechnik.phoehnlix.api.model.PhoehnlixApiToken
import de.esotechnik.phoehnlix.ktor.setupForwardedFor
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.get
import io.ktor.features.BadRequestException
import io.ktor.http.ContentType
import io.ktor.http.takeFrom
import io.ktor.request.ApplicationRequest
import io.ktor.request.uri
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@KtorExperimentalAPI
@JvmOverloads
fun Application.module(testing: Boolean = false) {
  setupForwardedFor()

  routing {
    // the scale makes GET requests to absolute urls, like to a proxy
    route("http://bridge1.soehnle.de") {
      dataservice()
    }
    dataservice()
  }
}

@KtorExperimentalAPI
private fun Route.dataservice() {
  get("/devicedataservice/dataservice") {
    // http://bridge1.soehnle.de/devicedataservice/dataservice?data=24...601a30c1
    val param = call.request.queryParameters.getOrFail("data")

    if (param.length != 68) {
      throw BadRequestException("Invalid data length ${param.length} (must be 68)")
    }
    val actualChecksum = param.substring(0, param.length - 8).hexStringChecksum()
    val expectedChecksum = param.substring(param.length - 8).toLong(16)
    if (actualChecksum != expectedChecksum) {
      throw BadRequestException(
        "Checksum mismatch (expected ${expectedChecksum.toString(16)}, was ${actualChecksum.toString(
          16
        )})."
      )
    }

    val response = when (val request = param.substring(0, 2)) {
      "24" -> {
        val data = WebConnectProtocol.parseMeasurementData(param)
        call.apiClient().measurement.post(data)
        "A00000000000000001000000000000000000000000000000"
      }
      "22" -> "A20000000000000000000000000000000000000000000000"
      "25" -> "A00000000000000001000000000000000000000000000000"
      "28" -> "A50000000000000001000000000000000000000000000000"
      "21" -> {
        val time = WebConnectProtocol.serializeTimestamp()
        "A100000000000000${time}000000000000000000000000"
      }
      "29" -> null
      else -> throw BadRequestException("Unknown request type 0x$request")
    }?.let {
      it.signHexString() + "\n"
    }

    val upstreamResponse = upstreamRequest(call.request)
    application.log.info("""Handled WebConnect request:
      |  Bridge Request:     $param
      |  Our Response:       ${response?.trim() ?: ""}
      |  Upstream Response:  ${upstreamResponse.trim()}
    """.replaceIndentByMargin())

    call.respondText(response ?: "", contentType = ContentType.Text.Plain)
  }
}

suspend fun upstreamRequest(request: ApplicationRequest): String {
  val httpClient = HttpClient {
    install(Logging) {
      logger = Logger.DEFAULT
      level = LogLevel.ALL
    }
  }
  return httpClient.get {
    url.takeFrom(request.uri)
    url {
      // We have to use the IP, because the hostname should resolve to us in this net
      host = "213.174.39.77"
    }
    headers.clear()
    headers.appendAll(request.headers)
  }
}

@KtorExperimentalAPI
fun ApplicationCall.apiClient(): ApiClient {
  val config = application.environment.config
  val apiUrl = config.property("phoehnlix.apiUrl").getString()
  val apiKey = config.property("phoehnlix.apiKey").getString()
  return ApiClient(Apache, apiUrl, PhoehnlixApiToken(apiKey))
}