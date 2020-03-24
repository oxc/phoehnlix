package de.esotechnik.phoehnlix.frontend.util

import kotlinx.css.CSSBuilder
import kotlinx.css.CssValue
import kotlinx.css.LinearDimension
import kotlinx.css.RuleSet
import kotlinx.css.toCustomProperty
import kotlinx.html.Tag
import react.Component
import react.RProps
import react.dom.jsStyle
import kotlin.reflect.KProperty

/**
 * @author Bernhard Frauendienst
 */
// copied from https://github.com/JetBrains/kotlin-wrappers/blob/master/kotlin-react/README.md
var Tag.style: RuleSet
  get() = error("style cannot be read from props")
  set(value) = jsStyle {
    CSSBuilder().apply(value).declarations.forEach {
      this[it.key] = when (it.value) {
        !is String, !is Number -> it.value.toString()
        else -> it.value
      }
    }
  }

fun Tag.style(handler: RuleSet) {
  style = handler
}
// </copied>

fun <T: CssValue> CSSBuilder.customProperty(name: String? = null, ctor: (String) -> T, initialValue: T? = null)
  = CustomPropertyDelegateProvider(this, name, ctor, initialValue)
fun CSSBuilder.customProperty(name: String? = null, initialValue: LinearDimension? = null) = customProperty(name, ::LinearDimension, initialValue)

class CustomPropertyDelegateProvider<T : CssValue>(
  private val cssBuilder: CSSBuilder,
  private val varName: String?,
  private val varCtor: (String) -> T,
  private val initialValue: T?
){
  operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): CustomPropertyDelegate<T> {
    val delegate = CustomPropertyDelegate(cssBuilder, varName ?: property.name, varCtor)
    if (initialValue != null) {
      delegate.setValue(thisRef, property, initialValue)
    }
    return delegate
  }
}

class CustomPropertyDelegate<T : CssValue>(
  private val cssBuilder: CSSBuilder,
  private val varName: String,
  private val varCtor: (String) -> T
) {
  operator fun getValue(receiver: Any?, property: KProperty<*>) =
    varCtor(varName.toCustomProperty())
  operator fun setValue(receiver: Any?, property: KProperty<*>, value: CssValue) =
    cssBuilder.setCustomProperty(varName, value)
}

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
