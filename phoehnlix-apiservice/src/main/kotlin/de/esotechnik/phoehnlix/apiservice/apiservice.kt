package de.esotechnik.phoehnlix.apiservice

import de.esotechnik.phoehnlix.api.model.ActivityLevel
import de.esotechnik.phoehnlix.api.model.LoginResponse
import de.esotechnik.phoehnlix.api.model.MeasurementData
import de.esotechnik.phoehnlix.api.model.PhoehnlixApiToken
import de.esotechnik.phoehnlix.api.model.ProfileDraft
import de.esotechnik.phoehnlix.api.model.Sex
import de.esotechnik.phoehnlix.apiservice.auth.SimpleJWT
import de.esotechnik.phoehnlix.apiservice.auth.TokenInfo
import de.esotechnik.phoehnlix.apiservice.auth.Userinfo
import de.esotechnik.phoehnlix.apiservice.auth.hasFitWriteScope
import de.esotechnik.phoehnlix.apiservice.data.CsvImport
import de.esotechnik.phoehnlix.apiservice.data.GoogleAccount
import de.esotechnik.phoehnlix.apiservice.data.GoogleAccounts
import de.esotechnik.phoehnlix.apiservice.data.Measurement
import de.esotechnik.phoehnlix.apiservice.data.Measurements
import de.esotechnik.phoehnlix.apiservice.data.Profile
import de.esotechnik.phoehnlix.apiservice.data.Profiles
import de.esotechnik.phoehnlix.apiservice.data.User
import de.esotechnik.phoehnlix.apiservice.data.Users
import de.esotechnik.phoehnlix.apiservice.data.insertNewMeasurement
import de.esotechnik.phoehnlix.apiservice.data.setBIAResults
import de.esotechnik.phoehnlix.apiservice.util.getValue
import de.esotechnik.phoehnlix.apiservice.util.toInstant
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.compoundAnd
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt

private val log = LoggerFactory.getLogger("de.esotechnik.phoehnlix.apiservice")

/**
 * @author Bernhard Frauendienst
 */
fun Route.apiservice(jwt: SimpleJWT, googleHttpClient: HttpClient) {
  authenticate("phoehnlix-jwt") {
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
        post {
          val profileId = call.profileId()
          val values = call.receive<ProfileDraft>()
          call.respondDb(ProfileResponse) {
            val user = User.findById(profileId) ?: throw NotFoundException()
            val dataSetter: Profile.() -> Unit = {
              values.name?.let { name = it }
              values.sex?.let { sex = it }
              values.birthday?.let { birthday = LocalDate.parse(it, BIRTHDAY_FORMATTER) }
              values.height?.let { height = it }
              values.activityLevel?.let { activityLevel = it }
              values.targetWeight?.let { targetWeight = it.takeIf { it > 0 } }
            }
            user.profile?.apply(dataSetter) ?: Profile.new(user, dataSetter)
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
              ?.let { ActivityLevel.fromIndex(it) }

            var csvPart: PartData.FileItem? = null

            val multipart = call.receiveMultipart()
            multipart.forEachPart { part ->
              when (part) {
                is PartData.FormItem -> {
                  when (part.name) {
                    //"profileId" -> profileId = part.getValue()
                    "activityLevel" -> activityLevel = ActivityLevel.fromIndex(part.getValue())
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
                is PartData.BinaryChannelItem,
                is PartData.BinaryItem -> {
                  throw BadRequestException("Binary parts are currently not supported.")
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
              val count = AtomicInteger(0)
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
  }
  post("measurement") {
    // TODO: check api key
    val data = call.receive<MeasurementData>()
    insertNewMeasurement(data)
    call.respond(HttpStatusCode.Created, "New measurement created")
  }
  authenticate("auth-oauth-google") {
    post("login/google") call@{
      val principal: OAuthAccessTokenResponse.OAuth2 = call.principal() ?: throw OAuth2Exception.MissingAccessToken()
      val accessToken = principal.accessToken

      val accessTokenInfo: TokenInfo = googleHttpClient.get("https://oauth2.googleapis.com/tokeninfo") {
        parameter("access_token", accessToken)
      }.body()
      log.info("Access token info: $accessTokenInfo")

      newSuspendedTransaction {
        Users.innerJoin(GoogleAccounts).leftJoin(Profiles)
          .select {
            GoogleAccounts.googleId eq accessTokenInfo.subject
          }.firstOrNull()?.let { row ->
            val user = User.wrapRow(row)
            val profile = row.getOrNull(Profiles.id)?.let { Profile.wrapRow(row) }
            val googleAccount = GoogleAccount.wrapRow(row).apply {
              updateFrom(principal, accessTokenInfo)
              if (syncWithFit && !accessTokenInfo.hasFitWriteScope()) {
                syncWithFit = false
              }
            }
            user to profile
          }
      }?.let { (user, profile) ->
        val apiToken = PhoehnlixApiToken(jwt.sign(user.id.toString()))
        val profileDraft = if (profile == null) {
          createProfileDraft(googleHttpClient, accessToken)
        } else null
        call.respond(
          LoginResponse(
            apiToken,
            profile = profile?.toProfile(),
            profileDraft = profileDraft
          )
        )
        return@call
      }

      val user = newSuspendedTransaction {
        User.new {}
      }
      val googleAccount = newSuspendedTransaction {
        GoogleAccount.new(user) {
          googleId = accessTokenInfo.subject
          updateFrom(principal, accessTokenInfo)
          syncWithFit = accessTokenInfo.hasFitWriteScope()
        }
      }

      val apiToken = PhoehnlixApiToken(jwt.sign(user.id.toString()))
      val profileDraft = createProfileDraft(googleHttpClient, accessToken)
      call.respond(LoginResponse(apiToken, profileDraft = profileDraft))
    }
  }
}

private suspend fun createProfileDraft(http: HttpClient, accessToken: String): ProfileDraft {
  val userInfo: Userinfo = http.get("https://www.googleapis.com/oauth2/v3/userinfo") {
    header("Authorization", "Bearer $accessToken")
  }.body()
  log.info("Got userinfo from Google: $userInfo")

  val personInfo: JsonObject = http.get("https://people.googleapis.com/v1/people/me") {
    header("Authorization", "Bearer $accessToken")
    parameter("personFields", "birthdays,genders")
  }.body()
  val birthday = personInfo["birthdays"]?.let { birthdays ->
    birthdays.jsonArray.filterIsInstance<JsonObject>().firstOrNull {
      it["date"]?.jsonObject?.let { date ->
        "day" in date && "month" in date && "year" in date
      } ?: false
    }?.let {
      val date = it["date"]!!.jsonObject
      val year = date["year"]!!.jsonPrimitive.int
      val month = date["month"]!!.jsonPrimitive.int
      val day = date["day"]!!.jsonPrimitive.int
      LocalDate.of(year, month, day)
    }
  }
  log.info("User birthday is $birthday")
  val gender = personInfo["genders"]?.let { genders ->
    genders.jsonArray.asSequence()
      .filterIsInstance<JsonObject>()
      .mapNotNull { it["value"]?.jsonPrimitive?.contentOrNull }
      .firstOrNull { it != "unspecified" }
      .let {
        when (it) {
          "female" -> Sex.Female
          "male" -> Sex.Male
          else -> null
        }
      }
  }
  log.info("User gender is $gender")

  val heightInfo: JsonObject= http.get("https://www.googleapis.com/") {
    url.path(
      "fitness", "v1", "users", "me", "dataSources",
      "derived:com.google.height:com.google.android.gms:merge_height",
      "datasets",
      "0-${Instant.now().toEpochMilli()}000000"
    )
    parameter("limit", 1)
    header("Authorization", "Bearer $accessToken")
  }.body()
  val height = heightInfo["point"]?.jsonArray?.let {
    it.getOrNull(0)?.jsonObject
      ?.get("value")?.jsonArray
      ?.get(0)?.jsonObject
      ?.get("fpVal")?.jsonPrimitive
      ?.doubleOrNull?.times(100)?.roundToInt()
  }
  return ProfileDraft(
    name = userInfo.givenName,
    sex = gender,
    height = height,
    birthday = birthday?.let { BIRTHDAY_FORMATTER.format(it) }
  )
}

private fun GoogleAccount.updateFrom(tokenResponse: OAuthAccessTokenResponse.OAuth2, accessTokenInfo: TokenInfo) {
  accessToken = tokenResponse.accessToken
  expiresAt = accessTokenInfo.expires
  tokenResponse.refreshToken?.let {
    refreshToken = it
  }
  // TODO: save scopes?
}

suspend fun ApplicationCall.profileId(): Int {
  val principal = principal<UserIdPrincipal>() ?: error("Missing principal, should have been caught by authenticate")

  val rawId = parameters.getOrFail("profileId")
  if (rawId == "me") {
    // TODO: This assumes userId ~= profileId
    return principal.name.toInt()
  }
  val profileId = rawId.toIntOrNull() ?: throw BadRequestException("Invalid profileId $rawId")
  // TODO: This assumes userId ~= profileId
  if (profileId != principal.name.toInt()) {
    throw UnauthorizedException("You don't have permission to view profile $profileId")
  }
  return profileId
}

suspend fun loadProfile(profileId: Int): Profile {
  return Profile.findById(profileId) ?: throw NotFoundException()
}


suspend inline fun <T, reified A> ApplicationCall.respondDb(converter: (T) -> A, crossinline statement: suspend Transaction.() -> T?) {
  respond(db { statement() }?.let {
    converter(it)
  } ?: throw NotFoundException())
}

suspend inline fun <T, reified A> ApplicationCall.respondDbList(
  crossinline converter: (T) -> A,
  crossinline statement: suspend Transaction.() -> Iterable<T>
) {
  respond(db { statement().map(converter) })
}

suspend inline fun <T> db(crossinline statement: suspend Transaction.() -> T) =
  newSuspendedTransaction(Dispatchers.IO) {
    statement()
  }

private val ApplicationRequest.fullUri: String
  get() = with(origin) {
    val defaultPort = URLProtocol.byName[scheme]?.defaultPort ?: 443
    val port = port.let { port -> if (port == defaultPort) "" else ":$port" }
    "$scheme://$host$port$uri"
  }
