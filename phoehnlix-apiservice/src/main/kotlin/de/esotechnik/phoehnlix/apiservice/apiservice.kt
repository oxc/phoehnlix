package de.esotechnik.phoehnlix.apiservice

import de.esotechnik.phoehnlix.api.google.LoginRequest
import de.esotechnik.phoehnlix.api.model.ActivityLevel
import de.esotechnik.phoehnlix.api.model.MeasurementData
import de.esotechnik.phoehnlix.api.model.PhoehnlixApiToken
import de.esotechnik.phoehnlix.apiservice.auth.createGoogleOAuth2Provider
import de.esotechnik.phoehnlix.apiservice.auth.oauth2RequestAccessToken
import de.esotechnik.phoehnlix.apiservice.data.CsvImport
import de.esotechnik.phoehnlix.apiservice.data.Measurement
import de.esotechnik.phoehnlix.apiservice.data.Measurements
import de.esotechnik.phoehnlix.apiservice.data.Profile
import de.esotechnik.phoehnlix.apiservice.data.insertNewMeasurement
import de.esotechnik.phoehnlix.apiservice.data.setBIAResults
import de.esotechnik.phoehnlix.apiservice.util.getValue
import de.esotechnik.phoehnlix.apiservice.util.toInstant
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.Json
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.features.BadRequestException
import io.ktor.features.MissingRequestParameterException
import io.ktor.features.NotFoundException
import io.ktor.features.origin
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.ApplicationRequest
import io.ktor.request.receive
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.application
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.enumFromOrdinal
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.compoundAnd
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory
import java.time.LocalDate

private val log = LoggerFactory.getLogger("de.esotechnik.phoehnlix.apiservice")

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
  val googleOAuthProvider = application.createGoogleOAuth2Provider()
  post("login/google") {
    val code = call.receive<LoginRequest>().code

    val http = HttpClient(Apache) {
      install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
      }
      Json {
        serializer = KotlinxSerializer()
      }
    }

    oauth2RequestAccessToken(code, http, Dispatchers.IO, googleOAuthProvider, "https://phoehnlix.obeliks.de".also {
      log.info("Redirect_uri: $it")
    }) { response ->
      val accessToken = response.accessToken
      val accessTokenInfo = http.get<JsonObject>("https://oauth2.googleapis.com/tokeninfo") {
        parameter("access_token", accessToken)
      }
      log.info("Access token info: $accessTokenInfo")

      val userInfo = http.get<String>("https://www.googleapis.com/oauth2/v3/userinfo") {
        header("Authorization", "Bearer $accessToken")
      }
      log.info("Got userinfo from Google: $userInfo")

      val birthdayInfo = http.get<JsonObject>("https://people.googleapis.com/v1/people/me") {
        header("Authorization", "Bearer $accessToken")
        parameter("personFields", "birthdays")
      }
      val birthday = birthdayInfo["birthdays"]?.let { birthdays ->
        birthdays.jsonArray.filterIsInstance<JsonObject>().firstOrNull {
          it["date"]?.jsonObject?.let { date ->
            "day" in date && "month" in date && "year" in date
          } ?: false
        }?.let {
          val date = it["date"]!!.jsonObject
          val year = date["year"]!!.primitive.int
          val month = date["month"]!!.primitive.int
          val day = date["day"]!!.primitive.int
          LocalDate.of(year, month, day)
        }
      }
      log.info("User birthday is $birthday")
    }

    // TODO: load/create user, create JWT token

    call.respond(PhoehnlixApiToken("helloWorld"))
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

suspend fun <T, A> ApplicationCall.respondDbList(
  converter: (T) -> A,
  statement: suspend Transaction.() -> Iterable<T>
) {
  respond(db { statement() }.map {
    converter(it)
  })
}

suspend inline fun <T> db(crossinline statement: suspend Transaction.() -> T) =
  newSuspendedTransaction(Dispatchers.IO) {
    statement()
  }

private val ApplicationRequest.fullUri: String
  get() = with (origin) {
    val defaultPort = URLProtocol.byName[scheme]?.defaultPort ?: 443
    val port = port.let { port -> if (port == defaultPort) "" else ":$port" }
    "$scheme://$host$port$uri"
  }
