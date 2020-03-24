package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.apiservice.model.ProfileMeasurement
import de.esotechnik.phoehnlix.model.MeasureType
import de.esotechnik.phoehnlix.model.MeasureType.*

/**
 * @author Bernhard Frauendienst
 */
fun ProfileMeasurement.parseTimestamp() = date_fns.parseISO(timestamp)

val MeasureType.unit get() = when (this) {
  Weight -> "kg"
  BodyFatPercent -> "%"
  BodyWaterPercent -> "%"
  MuscleMassPercent -> "%"
  BodyMassIndex -> null
  MetabolicRate -> "kcal"
}