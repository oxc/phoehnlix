@file:Suppress("NOTHING_TO_INLINE")
package de.esotechnik.phoehnlix.frontend.util

import csstype.Color
import csstype.Length
import csstype.PropertiesBuilder
import kotlinx.css.hyphenize
import mui.material.styles.Theme
import react.Props
import kotlin.reflect.KProperty

/**
 * @author Bernhard Frauendienst
 */
class CustomPropertyDelegate<T>(private val ctor: (String) -> T) {
  operator fun getValue(cssBuilder: PropertiesBuilder, property: KProperty<*>): T {
    return ctor("var(--${property.name.hyphenize()})")
  }

  operator fun setValue(cssBuilder: PropertiesBuilder, property: KProperty<*>, value: T) {
    cssBuilder.asDynamic()["--${property.name.hyphenize()}"] = value
  }
}
private val customColorDelegate = CustomPropertyDelegate(::Color)
val Color.Companion.customProperty get() = customColorDelegate
private val customLengthDelegate = CustomPropertyDelegate { it.unsafeCast<Length>() }
val Length.Companion.customProperty get() = customLengthDelegate

// for use with withStyles(withTheme=true)
val Props.theme get() = asDynamic()["theme"].unsafeCast<Theme>()