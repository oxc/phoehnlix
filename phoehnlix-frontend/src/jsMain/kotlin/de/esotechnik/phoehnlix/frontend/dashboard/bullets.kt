package de.esotechnik.phoehnlix.frontend.dashboard

import csstype.ClassName
import csstype.Color
import csstype.Display
import csstype.FontWeight
import csstype.NamedColor
import csstype.Overflow
import csstype.PropertiesBuilder
import csstype.TextAlign
import csstype.VerticalAlign
import csstype.div
import csstype.pct
import csstype.plus
import csstype.px
import csstype.unaryMinus
import de.esotechnik.phoehnlix.api.model.MeasureType
import de.esotechnik.phoehnlix.api.model.ProfileMeasurement
import de.esotechnik.phoehnlix.api.model.get
import de.esotechnik.phoehnlix.frontend.color
import de.esotechnik.phoehnlix.frontend.unit
import de.esotechnik.phoehnlix.frontend.util.circleDiameter
import de.esotechnik.phoehnlix.frontend.util.measurementColor
import de.esotechnik.phoehnlix.util.formatDecimalDigits
import kotlinx.css.hyphenize
import react.ComponentType
import react.FC
import react.Props
import react.PropsWithChildren
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.sup

external interface BulletComponentProps : PropsWithChildren {
  var measureType: MeasureType
  var classNames: ClassName
}
external interface BulletProps : Props {
  var measurement: ProfileMeasurement
  var measureTypes: Iterable<MeasureType>
  var component: ComponentType<in BulletComponentProps>
}

val measurementBullet = ClassName("measurementBullet")

fun PropertiesBuilder.bulletStyles() {
  measurementBullet {
    display = Display.inlineBlock
    overflow = Overflow.hidden
    color = NamedColor.white
    backgroundColor = measurementColor
    width = circleDiameter
    height = circleDiameter
    borderRadius = 50.pct
    lineHeight = circleDiameter
    val fontSize = circleDiameter / 5 + 4.px
    this.fontSize = fontSize
    textAlign = TextAlign.center
    verticalAlign = VerticalAlign.baseline
    fontWeight = FontWeight.bold

    "> sup" {
      fontWeight = FontWeight.normal
      lineHeight = 0.px
    }
    MeasureType.values().forEach { type ->
      type.cssClass {
        measurementColor = Color(type.color)
        if ((type.unit?.length ?: 0) > 2) {
          "> sup" {
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

/**
 * @author Bernhard Frauendienst
 */
val Bullets = FC<BulletProps> { props ->
    props.measureTypes.forEach { type ->
      props.component {
        measureType = type
        classNames = ClassName("$measurementBullet ${type.cssClass}")
        span {
          val value = props.measurement[type]
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



val MeasureType.cssClass get() = ClassName(name.replaceFirstChar { it.lowercase() }.hyphenize())