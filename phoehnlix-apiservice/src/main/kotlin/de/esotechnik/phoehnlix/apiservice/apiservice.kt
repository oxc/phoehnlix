package de.esotechnik.phoehnlix.apiservice

import de.esotechnik.phoehnlix.api.model.ActivityLevel
import de.esotechnik.phoehnlix.api.model.MeasurementData
import de.esotechnik.phoehnlix.api.model.OAuth2Token
import de.esotechnik.phoehnlix.api.model.PhoehnlixApiToken
import de.esotechnik.phoehnlix.apiservice.data.CsvImport
import de.esotechnik.phoehnlix.apiservice.data.Measurement
import de.esotechnik.phoehnlix.apiservice.data.Measurements
import de.esotechnik.phoehnlix.apiservice.data.Profile
import de.esotechnik.phoehnlix.apiservice.data.insertNewMeasurement
import de.esotechnik.phoehnlix.apiservice.data.setBIAResults
import de.esotechnik.phoehnlix.apiservice.util.toInstant
import de.esotechnik.phoehnlix.apiservice.util.getValue
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.features.BadRequestException
import io.ktor.features.MissingRequestParameterException
import io.ktor.features.NotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.receive
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.enumFromOrdinal
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.compoundAnd
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * @author Bernhard Frauendienst
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
        val profileId = call.profileId()
        call.respondDb(ProfileResponse) {
          loadProfile(profileId)
        }
      }
      route("/measurements") {
        get {
          val profileId = call.profileId()
          val from = call.request.queryParameters["from"]?.toInstant()
          val to = call.request.queryParameters["to"]?.toInstant()
          call.respondDbList(MeasurementResponse) {
            Measurement
              .find {
                listOfNotNull(
                  Measurements.profile eq profileId,
                  from?.let { Measurements.timestamp greaterEq it },
                  to?.let { Measurements.timestamp lessEq it }
                ).compoundAnd()
              }
              .orderBy(Measurements.timestamp to SortOrder.ASC)
          }
        }
        post("/import") {
          val profileId = call.profileId()
          var activityLevel: ActivityLevel? = call.parameters["activityLevel"]
            ?.toIntOrNull()
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
            loadProfile(profileId)
          }
          val count = CsvImport.import(csv.streamProvider(), profile, activityLevel)
          call.respond(HttpStatusCode.Created, "Created $count entries.")
        }
        post("/recalculate") {
          val profileId = call.profileId()
          newSuspendedTransaction {
            val profile = loadProfile(profileId)
            val count = atomic(0)
            Measurement.find {
              Measurements.profile eq profile.id and
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
  post("measurement") {
    // TODO: check api key
    val data = call.receive<MeasurementData>()
    insertNewMeasurement(data)
    call.respond(HttpStatusCode.Created, "New measurement created")
  }
  post("login/google") {
    val oAuth2Token = call.receive<OAuth2Token>()

    val json = HttpClient(Apache).get<JsonElement>("https://www.googleapis.com/userinfo/v2/me") {
      header("Authorization", "Bearer ${oAuth2Token.accessToken}")
    }

    call.respond(PhoehnlixApiToken())
  }
}

@KtorExperimentalAPI
suspend fun ApplicationCall.profileId(): Int {
  val rawId = parameters.getOrFail("profileId")
  if (rawId == "me") {
    TODO("Get current logged in user")
  }
  val profileId = rawId.toIntOrNull() ?: throw BadRequestException("Invalid profileId $rawId")
  // TODO: check permission
  return profileId
}

suspend fun loadProfile(profileId: Int): Profile {
  return Profile.findById(profileId) ?: throw NotFoundException()
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