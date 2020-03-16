package de.esotechnik.phoehnlix.dataservice

import de.esotechnik.phoehnlix.data.Database
import de.esotechnik.phoehnlix.data.insertNewMeasurement
import de.esotechnik.phoehnlix.data.setupSchema
import de.esotechnik.phoehnlix.model.ActivityLevel
import de.esotechnik.phoehnlix.model.ProfileData
import de.esotechnik.phoehnlix.model.Sex
import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.features.BadRequestException
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail
import io.ktor.util.pipeline.PipelinePhase

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@KtorExperimentalAPI
@JvmOverloads
fun Application.module(testing: Boolean = false) {
  connectDatabase() //.setupSchema()
  routing {
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

    val response = when (val request = param.substring(0, 2)) {
      "24" -> {
        val data = DataParser.parseData(param)
        insertNewMeasurement(data)
        "A00000000000000001000000000000000000000000000000"
      }
      "22" -> "A20000000000000000000000000000000000000000000000"
      "29" -> null
      else -> throw BadRequestException("Unknown request type 0x$request")
    }?.let {
      it.signHexString() + "\n"
    }

    call.respondText(response ?: "", contentType = ContentType.Text.Plain)
  }
}

@KtorExperimentalAPI
fun Application.connectDatabase(): Database {
  val config = environment.config.config("database")
  return Database(
    url = config.property("url").getString(),
    user = config.propertyOrNull("user")?.getString() ?: "phoehnlix",
    password = config.property("password").getString()
  ).connect()
}