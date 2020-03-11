package de.esotechnik.phoehnlix.dataservice

import io.ktor.application.*
import io.ktor.features.BadRequestException
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.util.getOrFail

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@io.ktor.util.KtorExperimentalAPI
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    routing {
        get("/devicedataservice/dataservice") {
            // http://bridge1.soehnle.de/devicedataservice/dataservice?data=24...601a30c1
            val param = call.request.queryParameters.getOrFail("data")

            if (param.length != 68) {
                throw BadRequestException("Invalid data length ${param.length} (must be 68)")
            }
            val actualChecksum = param.substring(0, param.length - 8).hexStringChecksum()
            val expectedChecksum = param.substring(param.length - 8).toLong(16)
            if (actualChecksum != expectedChecksum) {
                throw BadRequestException("Checksum mismatch (expected ${expectedChecksum.toString(16)}, was ${actualChecksum.toString(16)}).")
            }

            val response = when (val request = param.substring(0, 2)) {
                "24" -> {
                    val data = DataParser.parseData(param)
                    val profile = ProfileData(191.toFloat(), 35, Sex.Male)
                    log.info("""
                        
Result: 
${data.toLogString().replaceIndent("  ")}
Bernhard BIA: 
${data.calculateBIAResults(profile).toString().replaceIndent("  ")}
                        """.trimIndent())
                    "A00000000000000001000000000000000000000000000000"
                }
                "22" -> "A20000000000000000000000000000000000000000000000"
                "29" -> null
                else -> throw BadRequestException("Unknown request type 0x$request")
            }?.signHexString()

            call.respondText(response ?: "", contentType = ContentType.Text.Plain)
        }
    }
}

