package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.apiservice.model.ProfileMeasurement
import de.esotechnik.phoehnlix.model.MeasureType
import de.esotechnik.phoehnlix.model.MeasureType.*

/**
 * @author Bernhard Frauendienst
 */
fun ProfileMeasurement.parseTimestamp() = date_fns.parseISO(timestamp)

val MeasureType.title get() = when (this) {
  Weight -> "Gewicht"
  BodyFatPercent -> "Fett"
  BodyWaterPercent -> "Wasser"
  MuscleMassPercent -> "Muskel"
  BodyMassIndex -> "BMI"
  MetabolicRate -> "Kalorien"
}

val MeasureType.unit get() = when (this) {
  Weight -> "kg"
  BodyFatPercent -> "%"
  BodyWaterPercent -> "%"
  MuscleMassPercent -> "%"
  BodyMassIndex -> null
  MetabolicRate -> "kcal"
}