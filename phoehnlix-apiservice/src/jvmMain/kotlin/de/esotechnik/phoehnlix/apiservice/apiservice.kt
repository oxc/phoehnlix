package de.esotechnik.phoehnlix.apiservice

import de.esotechnik.phoehnlix.data.CsvImport
import de.esotechnik.phoehnlix.data.Measurement
import de.esotechnik.phoehnlix.data.Measurements
import de.esotechnik.phoehnlix.data.Profile
import de.esotechnik.phoehnlix.data.setBIAResults
import de.esotechnik.phoehnlix.model.ActivityLevel
import de.esotechnik.phoehnlix.model.calculateBIAResults
import de.esotechnik.phoehnlix.util.getValue
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.BadRequestException
import io.ktor.features.MissingRequestParameterException
import io.ktor.features.NotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getValue
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.enumFromOrdinal
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * @author Bernhard Frauendienst <bernhard.frauendienst@markt.de>
 */
@KtorExperimentalAPI
fun Route.apiservice() {
  route("/profile") {
    get {
      call.respondDbList(ProfileResponse) {
        // TOOD: list all *accessible* profiles
        Profile.all()
      }
    }
    route("/{profileId}") {
      get {
        val profileId: Int by call.parameters
        call.respondDb(ProfileResponse) {
          Profile.findById(profileId)
        }
      }
      route("/measurements") {
        get {
          val profileId: Int by call.parameters
          call.respondDbList(MeasurementResponse) {
            Measurement
              .find { Measurements.profile eq profileId }
              .orderBy(Measurements.timestamp to SortOrder.ASC)
          }
        }
        post("/import") {
          val profileId: Int by call.parameters
          var activityLevel: ActivityLevel? = call.parameters["activityLevel"]
            ?.let { it.toIntOrNull() }
            ?.let { enumFromOrdinal(ActivityLevel::class, it) }

          var csvPart: PartData.FileItem? = null

          val multipart = call.receiveMultipart()
          multipart.forEachPart { part ->
            when (part) {
              is PartData.FormItem -> {
                when (part.name) {
                  //"profileId" -> profileId = part.getValue()
                  "activityLevel" -> activityLevel = enumFromOrdinal(ActivityLevel::class, part.getValue())
                }
              }
              is PartData.FileItem -> {
                if (csvPart != null) {
                  throw BadRequestException("Only one multipart file supported.")
                }
                csvPart = part
                // don't dispose this part
                return@forEachPart
              }
            }
            part.dispose()
          }

          val csv = csvPart ?: throw MissingRequestParameterException("csvFile")

          val profile = db {
            // FIXME: check profile permissions
            Profile.findById(profileId) ?: throw NotFoundException()
          }
          val count = CsvImport.import(csv.streamProvider(), profile, activityLevel)
          call.respond(HttpStatusCode.Created, "Created $count entries.")
        }
        post("/recalculate") {
          val profileId: Int by call.parameters

          newSuspendedTransaction {
            val profile = Profile.findById(profileId) ?: throw NotFoundException()
            val count = atomic(0)
            Measurement.find {
              Measurements.profile eq profileId and
                Measurements.imp50.isNotNull() and
                Measurements.imp5.isNotNull()
            }.forUpdate().forEach { measurement ->
              measurement.calculateBIAResults(profile)?.let {
                measurement.setBIAResults(it)
                count.incrementAndGet()
              }
            }
            call.respond(HttpStatusCode.OK, "Recalculated BIA data for $count entries.")
          }
        }
      }
    }
  }
}

suspend fun <T, A> ApplicationCall.respondDb(converter: (T) -> A, statement: suspend Transaction.() -> T?) {
  respond(db { statement() }?.let {
    converter(it)
  } ?: throw NotFoundException())
}

suspend fun <T, A> ApplicationCall.respondDbList(converter: (T) -> A, statement: suspend Transaction.() -> Iterable<T>) {
  respond(db { statement() }.map {
    converter(it)
  })
}

suspend inline fun <T> db(crossinline statement: suspend Transaction.() -> T) =
  newSuspendedTransaction(Dispatchers.IO) {
    statement()
  }