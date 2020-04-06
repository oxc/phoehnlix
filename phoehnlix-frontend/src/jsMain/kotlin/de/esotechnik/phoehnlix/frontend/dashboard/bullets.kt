package de.esotechnik.phoehnlix.frontend.dashboard

import de.esotechnik.phoehnlix.apiservice.model.ProfileMeasurement
import de.esotechnik.phoehnlix.apiservice.model.get
import de.esotechnik.phoehnlix.frontend.color
import de.esotechnik.phoehnlix.frontend.unit
import de.esotechnik.phoehnlix.frontend.util.circleDiameter
import de.esotechnik.phoehnlix.frontend.util.measurementColor
import de.esotechnik.phoehnlix.frontend.util.styleSets
import de.esotechnik.phoehnlix.model.MeasureType
import de.esotechnik.phoehnlix.util.formatDecimalDigits
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.FontWeight
import kotlinx.css.LinearDimension
import kotlinx.css.Overflow
import kotlinx.css.TextAlign
import kotlinx.css.VerticalAlign
import kotlinx.css.backgroundColor
import kotlinx.css.borderRadius
import kotlinx.css.color
import kotlinx.css.display
import kotlinx.css.fontSize
import kotlinx.css.fontWeight
import kotlinx.css.height
import kotlinx.css.hyphenize
import kotlinx.css.lineHeight
import kotlinx.css.marginLeft
import kotlinx.css.marginRight
import kotlinx.css.marginTop
import kotlinx.css.overflow
import kotlinx.css.pct
import kotlinx.css.properties.LineHeight
import kotlinx.css.properties.lh
import kotlinx.css.px
import kotlinx.css.textAlign
import kotlinx.css.top
import kotlinx.css.verticalAlign
import kotlinx.css.width
import materialui.styles.StylesSet
import react.RBuilder
import react.RElementBuilder
import react.RHandler
import react.RProps
import react.ReactElement
import react.dom.span
import react.dom.sup

/**
 * @author Bernhard Frauendienst
 */
fun RElementBuilder<RProps>.bullets(
  entry: ProfileMeasurement, measureTypes: Iterable<MeasureType>,
  component: RBuilder.(measureType: MeasureType, classNames: String, content: RBuilder.() -> Unit) -> ReactElement
) = bullets(entry, measureTypes, attrs, component)

fun RBuilder.bullets(
  entry: ProfileMeasurement, measureTypes: Iterable<MeasureType>, props: RProps,
  component: RBuilder.(measureType: MeasureType, classNames: String, content: RBuilder.() -> Unit) -> ReactElement
) {
  val measurementBullet by props.styleSets
  measureTypes.forEach { type ->
    component(type, "$measurementBullet ${type.cssClass}") {
      span {
        val value = entry[type]
        if (value != null) {
          +value.formatDecimalDigits(1)
          type.unit?.let {
            sup { +it }
          }
        } else {
          +"—"
        }
      }
    }
  }
}

fun StylesSet.makeBulletStyles(diameter: LinearDimension, fontSize: LinearDimension) {
  "measurementBullet" {
    circleDiameter = diameter

    overflow = Overflow.hidden
    color = Color.white
    backgroundColor = measurementColor
    width = circleDiameter
    height = circleDiameter
    borderRadius = 50.pct
    lineHeight = circleDiameter.lh
    this.fontSize = fontSize
    textAlign = TextAlign.center
    verticalAlign = VerticalAlign.baseline
    fontWeight = FontWeight.bold
    descendants("sup") {
      fontWeight = FontWeight.normal
      lineHeight = 0.px.lh
    }
    MeasureType.values().forEach { type ->
      +type.cssClass {
        measurementColor = Color(type.color)
        if ((type.unit?.length ?: 0) > 2) {
          descendants("sup") {
            display = Display.block
            marginTop = -fontSize
            verticalAlign = VerticalAlign.top
            textAlign = TextAlign.center
          }
        }
      }
    }
  }
}


val MeasureType.cssClass get() = name.decapitalize().hyphenize()