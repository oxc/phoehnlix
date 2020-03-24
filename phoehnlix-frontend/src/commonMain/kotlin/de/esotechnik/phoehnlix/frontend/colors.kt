package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.model.MeasureType
import de.esotechnik.phoehnlix.model.MeasureType.*

/**
 * @author Bernhard Frauendienst
 */
val MeasureType.color get() = when (this) {
  Weight -> "#cd2129" // "#a32530"
  BodyFatPercent -> "#faa21e" // "#c6a92e"
  BodyWaterPercent -> "#2395cb" // "#44a6ab"
  MuscleMassPercent -> "#a408a4" // "#aa0a7c"
  BodyMassIndex -> "#76b525" // "#5896ce"
  MetabolicRate -> "#51a97e"
}