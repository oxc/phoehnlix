package de.esotechnik.phoehnlix.frontend.util

import kotlinx.css.CSSBuilder
import kotlinx.css.Color
import kotlinx.css.LinearDimension

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
var CSSBuilder.measurementColor by Color.customProperty

@CustomProperty
var CSSBuilder.circleDiameter by LinearDimension.customProperty
