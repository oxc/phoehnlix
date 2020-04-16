package de.esotechnik.phoehnlix.apiservice.data

import de.esotechnik.phoehnlix.api.model.ActivityLevel
import de.esotechnik.phoehnlix.api.model.Sex
import de.esotechnik.phoehnlix.apiservice.data.GoogleAccounts.entityId
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.ReferenceOption.*
import org.jetbrains.exposed.sql.SchemaUtils.withDataBaseLock
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.`java-time`.timestamp
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.statements.jdbc.iterate
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * @author Bernhard Frauendienst
 */
object Scales : IntIdTable() {
  val serial = binary("serial", 12).uniqueIndex() // bridge_id || scale_id
}

/**
 * A scale connected to a certain bridge
 */
class Scale(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<Scale>(Scales)

  var serial by Scales.serial

  var connectedProfiles by Profile via ProfileScales
}

object Users : IntIdTable()
class User(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<User>(Users)
}

object GoogleAccounts : IdTable<Int>() {
  override val id = integer("user_id").entityId().references(Users.id, onDelete = CASCADE, onUpdate = CASCADE)
  val googleId = varchar("google_id", 255).uniqueIndex()
  val accessToken = varchar("access_token", 2048)
  val expiresAt = timestamp("expires_at")
  val refreshToken = varchar("refresh_token", 512).nullable()
  val syncWithFit = bool("sync_fit")

  override val primaryKey = PrimaryKey(id)
}
class GoogleAccount(userId: EntityID<Int>) : IntEntity(userId) {
  companion object : IntEntityClass<GoogleAccount>(GoogleAccounts) {
    fun new(user: User, init: GoogleAccount.() -> Unit) = new(user.id.value, init)
  }

  val userId by GoogleAccounts.id
  var googleId by GoogleAccounts.googleId
  var accessToken by GoogleAccounts.accessToken
  var expiresAt by GoogleAccounts.expiresAt
  var refreshToken by GoogleAccounts.refreshToken
  var syncWithFit by GoogleAccounts.syncWithFit

  val user by User referencedOn GoogleAccounts.id
}

object Profiles : IdTable<Int>() {
  override val id = integer("user_id").entityId().references(Users.id, onDelete = RESTRICT, onUpdate = CASCADE)
  val name = varchar("name", 255)
  val sex = enumeration("sex", Sex::class)
  val birthday = date("birthday")
  val height = integer("height")
  val activityLevel = enumeration("activity_level", ActivityLevel::class)
  val targetWeight = double("target_weight").nullable()

  override val primaryKey = PrimaryKey(id)
}

class Profile(userId: EntityID<Int>) : IntEntity(userId) {
  companion object : IntEntityClass<Profile>(Profiles) {
    fun new(user: User, init: Profile.() -> Unit) = new(user.id.value, init)
  }

  val userId by Profiles.id
  var name by Profiles.name
  var sex by Profiles.sex
  var birthday by Profiles.birthday
  var height by Profiles.height
  var activityLevel by Profiles.activityLevel
  var targetWeight by Profiles.targetWeight

  val user by User referencedOn Profiles.id
  var connectedScales by Scale via ProfileScales
}

object ProfileScales : Table() {
  val profile = reference("profile_id", Profiles)
  val scale = reference("scale_id", Scales)
  override val primaryKey = PrimaryKey(profile, scale)
}

object Measurements : LongIdTable() {
  val scale = optReference("scale_id", Scales, onDelete = RESTRICT, onUpdate = CASCADE)

  val timestamp = timestamp("timestamp")

  val weight = double("weight")
  val imp50 = double("imp50").nullable()
  val imp5 = double("imp5").nullable()
  val bodyFatPercent = double("body_fat").nullable()
  val bodyWaterPercent = double("body_water").nullable()
  val muscleMassPercent = double("muscle_mass").nullable()
  val bodyMassIndex = double("bmi").nullable()
  val metabolicRate = double("metabolic_rate").nullable()

  val profile = optReference("profile_id", Profiles, onDelete = CASCADE, onUpdate = CASCADE)

  // these are just for historic consistency. Perhaps we'll delete them in the future
  val sex = enumeration("sex", Sex::class).nullable()
  val age = double("age").nullable()
  val height = integer("height").nullable()
  val activityLevel = enumeration("activity_level", ActivityLevel::class).nullable()

  val notes = text("notes").nullable()

  init {
    uniqueIndex(scale, timestamp, weight)
    index(false, profile, timestamp)
  }
}

class Measurement(id: EntityID<Long>) : LongEntity(id) {
  companion object : LongEntityClass<Measurement>(Measurements)

  var scale by Scale optionalReferencedOn Measurements.scale

  var timestamp by Measurements.timestamp
  var weight by Measurements.weight
  var imp50 by Measurements.imp50
  var imp5 by Measurements.imp5

  var bodyFatPercent by Measurements.bodyFatPercent
  var bodyWaterPercent by Measurements.bodyWaterPercent
  var muscleMassPercent by Measurements.muscleMassPercent
  var bodyMassIndex by Measurements.bodyMassIndex
  var metabolicRate by Measurements.metabolicRate

  var profile by Profile optionalReferencedOn Measurements.profile
  var sex by Measurements.sex
  var age by Measurements.age
  var height by Measurements.height
  var activityLevel by Measurements.activityLevel

  var notes by Measurements.notes
}

internal fun setupSchema() {
  transaction {
    // print sql to std-out
    addLogger(StdOutSqlLogger)

    withDataBaseLock {
      println("Got lock, setting up schema...")
      SchemaUtils.createMissingTablesAndColumns(
        Users, GoogleAccounts, Profiles,
        Scales, ProfileScales,
        Measurements
      )
      println("Done.")
    }
  }
}