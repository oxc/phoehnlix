package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.api.model.MeasureType
import de.esotechnik.phoehnlix.api.model.MeasureType.*
import de.esotechnik.phoehnlix.api.model.ProfileMeasurement

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