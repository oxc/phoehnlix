package de.esotechnik.phoehnlix.dataservice

import de.esotechnik.phoehnlix.data.Database
import de.esotechnik.phoehnlix.data.insertNewMeasurement
import de.esotechnik.phoehnlix.data.setupSchema
import io.ktor.application.*
import io.ktor.features.BadRequestException
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@KtorExperimentalAPI
@JvmOverloads
fun Application.module(testing: Boolean = false) {
  install(Database.Feature) {
    setup = ::setupSchema
  }
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
        insertNewMeasurement(data)
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

    call.respondText(response ?: "", contentType = ContentType.Text.Plain)
  }
}