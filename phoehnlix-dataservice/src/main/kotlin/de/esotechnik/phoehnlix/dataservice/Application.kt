package de.esotechnik.phoehnlix.dataservice

import de.esotechnik.phoehnlix.api.client.ApiClient
import de.esotechnik.phoehnlix.api.model.PhoehnlixApiToken
import de.esotechnik.phoehnlix.ktor.setupForwardedFor
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
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

    val result = Result.runCatching {
      when (val request = param.substring(0, 2)) {
        "24" -> {
          if (param.substring(26, 46) == "fa072011060741014101") {
            // new scale registration
            "A00000000000000001000000000000000000000000000000"
          } else {
            val data = WebConnectProtocol.parseMeasurementData(param)
            call.apiClient().use {
              it.measurement.post(data)
            }
            "A00000000000000001000000000000000000000000000000"
          }
        }
        "22" -> "A20000000000000000000000000000000000000000000000"
        "25" -> "A00000000000000001000000000000000000000000000000"
        "28" -> "A50000000000000001000000000000000000000000000000"
        "21" -> {
          val time = WebConnectProtocol.serializeTimestamp()
          "A100000000000000${time}000000000000000000000000"
        }
        "23" -> "A00000000000000001000000000000000000000000000000"
        "20" -> "A00000000000000001000000000000000000000000000000"
        "29" -> null
        else -> throw BadRequestException("Unknown request type 0x$request: $param")
      }?.let {
        it.signHexString() + "\n"
      }
    }

    application.log.info("""Handled WebConnect request:
      |  Bridge Request:     $param
      |  Our Response:       ${result.getOrElse { it.message }?.trim() ?: ""}
    """.replaceIndentByMargin())

    val response = result.getOrThrow()
    call.respondText(response ?: "", contentType = ContentType.Text.Plain)
  }
}

fun ApplicationCall.apiClient(): ApiClient {
  val config = application.environment.config
  val apiUrl = config.property("phoehnlix.apiUrl").getString()
  val apiKey = config.property("phoehnlix.apiKey").getString()
  return ApiClient(Apache, apiUrl, PhoehnlixApiToken(apiKey))
}
