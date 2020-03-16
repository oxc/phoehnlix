package de.esotechnik.phoehnlix.data

import de.esotechnik.phoehnlix.data.Measurements.nullable
import de.esotechnik.phoehnlix.model.ActivityLevel
import de.esotechnik.phoehnlix.model.Sex
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
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

object Profiles : IntIdTable() {
  val name = varchar("name", 255)
  val sex = enumeration("sex", Sex::class)
  val birthday = date("birthday")
  val height = integer("height")
  val activityLevel = enumeration("activity_level", ActivityLevel::class)
  val targetWeight = float("target_weight")
}

class Profile(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<Profile>(Profiles)

  var name by Profiles.name
  var sex by Profiles.sex
  var birthday by Profiles.birthday
  var height by Profiles.height
  var activityLevel by Profiles.activityLevel
  var targetWeight by Profiles.targetWeight

  var connectedScales by Scale via ProfileScales
}

object ProfileScales : Table() {
  val profile = reference("profile_id", Profiles)
  val scale = reference("scale_id", Scales)
  override val primaryKey = PrimaryKey(profile, scale)
}

object Measurements : LongIdTable() {
  val scale = reference("scale_id", Scales, onDelete = RESTRICT, onUpdate = CASCADE)

  val timestamp = timestamp("timestamp")

  val weight = float("weight")
  val imp50 = float("imp50").nullable()
  val imp5 = float("imp5").nullable()
  val bodyFatPercent = float("body_fat").nullable()
  val bodyWaterPercent = float("body_water").nullable()
  val muscleMassPercent = float("muscle_mass").nullable()
  val bodyMassIndex = float("bmi").nullable()
  val metabolicRate = float("metabolic_rate").nullable()

  val profile = optReference("profile_id", Profiles, onDelete = CASCADE, onUpdate = CASCADE)

  // these are just for historic reasons. Perhaps we'll delete them in the future
  val sex = enumeration("sex", Sex::class).nullable()
  val age = float("age").nullable()
  val height = integer("height").nullable()
  val activityLevel = enumeration("activity_level", ActivityLevel::class).nullable()

  init {
    uniqueIndex(scale, timestamp, weight)
  }
}

class Measurement(id: EntityID<Long>) : LongEntity(id) {
  companion object : LongEntityClass<Measurement>(Measurements)

  var scale by Scale referencedOn Measurements.scale

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
}

fun setupSchema() {
  transaction {
    // print sql to std-out
    addLogger(StdOutSqlLogger)

    withDataBaseLock {
      println("Got lock, setting up schema...")
      SchemaUtils.createMissingTablesAndColumns(
        Scales, Profiles, ProfileScales,
        Measurements
      )
      println("Done.")
    }
  }
}