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
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.`java-time`.timestamp

/**
 * @author Bernhard Frauendienst <bernhard.frauendienst@markt.de>
 */
object Scales: IntIdTable() {
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

object Profiles: IntIdTable() {
  val name = varchar("name", 255)
  val sex = enumerationByName("sex", 1, Sex::class)
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
  val profile = reference("profile", Profiles)
  val scale = reference("scale", Scales)
  override val primaryKey = PrimaryKey(profile, scale)
}

object Measurements: LongIdTable() {
  val scale = reference("scale", Scales, onDelete = ReferenceOption.RESTRICT, onUpdate = ReferenceOption.CASCADE)

  val timestamp = timestamp("timestamp")

  val weight = float("weight")
  val resistance = float("resistance").nullable()
  val reactance = float("reactance").nullable()
}

class Measurement(id: EntityID<Long>) : LongEntity(id) {
  companion object : LongEntityClass<Measurement>(Measurements)

  var scale by Scale referencedOn Measurements.scale

  var timestamp by Measurements.timestamp
  var weight by Measurements.weight
  var resistance by Measurements.resistance
  var reactance by Measurements.reactance
}

class ProfileMeasurements: LongIdTable() {
  val timestamp = timestamp("timestamp")

  val weight = Measurements.float("weight")
  val resistance = Measurements.float("resistance").nullable()
  val reactance = Measurements.float("reactance").nullable()

}