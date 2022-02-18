package de.esotechnik.phoehnlix.frontend.util

import csstype.Color
import csstype.Length
import csstype.PropertiesBuilder

/**
 * @author Bernhard Frauendienst
 */

/**
 * DslMarker for custom properties to mark them different from
 * regular properties
 */
@DslMarker
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class CustomProperty

@CustomProperty
var PropertiesBuilder.measurementColor by Color.customProperty

@CustomProperty
var PropertiesBuilder.circleDiameter by Length.customProperty
