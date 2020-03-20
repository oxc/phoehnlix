package de.esotechnik.phoehnlix.apiservice

import de.esotechnik.phoehnlix.data.CsvData
import de.esotechnik.phoehnlix.data.CsvImport
import de.esotechnik.phoehnlix.data.Measurement
import de.esotechnik.phoehnlix.data.Measurements
import de.esotechnik.phoehnlix.data.Profile
import de.esotechnik.phoehnlix.model.ActivityLevel
import de.esotechnik.phoehnlix.util.getValue
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.BadRequestException
import io.ktor.features.MissingRequestParameterException
import io.ktor.features.NotFoundException
import io.ktor.features.ParameterConversionException
import io.ktor.http.Parameters
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.DefaultConversionService
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.enumFromOrdinal
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

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
      get("/measurements") {
        val profileId: Int by call.parameters
        call.respondDbList(MeasurementResponse) {
          Measurement
            .find { Measurements.profile eq profileId }
            .orderBy( Measurements.timestamp to SortOrder.ASC )
        }
      }
      post("/measurements/import") {
        var profileId: Int? = null
        var activityLevel: ActivityLevel? = null

        var csvData: CsvData? = null

        val multipart = call.receiveMultipart()
        multipart.forEachPart { part ->
          when (part) {
            is PartData.FormItem -> {
              when (part.name) {
                "profileId" -> profileId = part.getValue()
                "activityLevel" -> activityLevel = enumFromOrdinal(ActivityLevel::class, part.getValue())
              }
            }
            is PartData.FileItem -> {
              if (csvData != null) {
                throw BadRequestException("Only one multipart file supported.")
              }
              csvData = CsvImport().readData(part.streamProvider())
            }
          }
          part.dispose()
        }

        val pid = profileId ?: throw MissingRequestParameterException("profileId")
        val csv = csvData ?: throw MissingRequestParameterException("csvFile")

        val profile = db {
          // FIXME: check profile permissions
          Profile.findById(pid) ?: throw NotFoundException()
        }
        csv.import(profile, activityLevel)
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