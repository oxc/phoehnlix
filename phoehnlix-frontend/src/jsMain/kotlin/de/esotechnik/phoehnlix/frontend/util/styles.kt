@file:Suppress("NOTHING_TO_INLINE")
package de.esotechnik.phoehnlix.frontend.util

import kotlinx.css.CSSBuilder
import kotlinx.css.Color
import kotlinx.css.CssValue
import kotlinx.css.LinearDimension
import kotlinx.css.hyphenize
import kotlinx.css.toCustomProperty
import materialui.styles.muitheme.MuiTheme
import react.Component
import react.RProps
import kotlin.reflect.KProperty

/**
 * @author Bernhard Frauendienst
 */
class CustomPropertyDelegate<T : CssValue>(private val ctor: (String) -> T) {
  operator fun getValue(cssBuilder: CSSBuilder, property: KProperty<*>): T {
    return ctor(property.name.hyphenize().toCustomProperty())
  }

  operator fun setValue(cssBuilder: CSSBuilder, property: KProperty<*>, value: T) {
    cssBuilder.setCustomProperty(property.name.hyphenize(), value)
  }
}
private val customColorDelegate = CustomPropertyDelegate(::Color)
val Color.Companion.customProperty get() = customColorDelegate
private val customLinearDimensionDelegate = CustomPropertyDelegate(::LinearDimension)
val LinearDimension.Companion.customProperty get() = customLinearDimensionDelegate

fun RProps.styleSet(name: String): String
  = asDynamic()["classes"][name] as String?
  ?: throw NoSuchElementException("No style $name defined.")
fun Component<*, *>.styleSet(name: String) = props.styleSet(name)

val RProps.styleSets get() = StyleSetDelegateProvider(this)
val Component<*, *>.styleSets get() = StyleSetDelegateProvider(this.props)

class StyleSetDelegateProvider(private val props: RProps) {
  operator fun provideDelegate(thisRef: Any?, property: KProperty<*>) =
    StyleSetDelegate(props.styleSet(property.name))
}
class StyleSetDelegate(private val classNames: String) {
  operator fun getValue(receiver: Any?, property: KProperty<*>) = classNames
}

// for use with withStyles(withTheme=true)
val RProps.theme get() = asDynamic()["theme"].unsafeCast<MuiTheme>()