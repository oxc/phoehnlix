package de.esotechnik.phoehnlix.frontend.dashboard

import date_fns.FormatOptions
import date_fns.format
import date_fns.locale.de
import de.esotechnik.phoehnlix.apiservice.model.ProfileMeasurement
import de.esotechnik.phoehnlix.apiservice.model.get
import de.esotechnik.phoehnlix.frontend.color
import de.esotechnik.phoehnlix.frontend.parseTimestamp
import de.esotechnik.phoehnlix.frontend.unit
import de.esotechnik.phoehnlix.frontend.util.customProperty
import de.esotechnik.phoehnlix.model.MeasureType
import de.esotechnik.phoehnlix.util.formatDecimalDigits
import de.esotechnik.phoehnlix.frontend.util.style
import de.esotechnik.phoehnlix.frontend.util.styleSets
import kotlinx.css.CSSBuilder
import kotlinx.css.Color
import kotlinx.css.FontWeight.Companion.bold
import kotlinx.css.FontWeight.Companion.normal
import kotlinx.css.TextAlign.center
import kotlinx.css.backgroundColor
import kotlinx.css.borderRadius
import kotlinx.css.color
import kotlinx.css.fontWeight
import kotlinx.css.height
import kotlinx.css.lineHeight
import kotlinx.css.pct
import kotlinx.css.properties.lh
import kotlinx.css.px
import kotlinx.css.textAlign
import kotlinx.css.toCustomProperty
import kotlinx.css.width
import materialui.components.table.TableProps
import materialui.components.table.table
import materialui.components.tablebody.tableBody
import materialui.components.tablecell.enums.TableCellStyle
import materialui.components.tablecell.tdCell
import materialui.components.tablerow.tableRow
import materialui.styles.StylesSet
import materialui.styles.childWithStyles
import react.RBuilder
import react.RComponent
import react.RHandler
import react.RState
import react.ReactElement
import react.dom.span
import react.dom.sup

/**
 * @author Bernhard Frauendienst
 */
private inline var CSSBuilder.measurementColor: Color
  get() = Color("measurement-color".toCustomProperty())
  set(value) = setCustomProperty("measurement-color", value)


private val MEASURE_TYPES = MeasureType.values().toList()

interface MeasurementListProps : TableProps {
  var measurements: List<ProfileMeasurement>
}

class MeasurementListComponent : RComponent<MeasurementListProps, RState>() {
  override fun RBuilder.render() {
    val measurementBullet by props.styleSets

    table {
      tableBody {
        props.measurements.forEach { entry ->
          tableRow {
            tdCell {
              val timestamp = entry.parseTimestamp()
              val formatOptions = new<FormatOptions> {
                locale = de
              }
              +format(timestamp, "EE dd.MM.yyyy", formatOptions)
              +"\n"
              span {
                +(format(timestamp, "HH.mm", formatOptions) + " Uhr")
              }
            }
            MEASURE_TYPES.forEach { type ->
              tdCell(TableCellStyle.root to measurementBullet) {
                attrs.style {
                  measurementColor = Color(type.color)
                }
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
        }
      }
    }
  }

  companion object {
    private val styleSets: StylesSet.() -> Unit = {
      "measurementBullet" {
        color = Color.white
        backgroundColor = measurementColor
        val circleDiameter by customProperty("circle-diameter", 40.px)
        width = circleDiameter
        height = circleDiameter
        borderRadius = 50.pct
        lineHeight = 30.px.lh
        textAlign = center
        fontWeight = bold
        "& sup" {
          fontWeight = normal
        }
      }
    }

    fun RBuilder.render(handler: RHandler<MeasurementListProps>): ReactElement =
      childWithStyles(
        MeasurementListComponent::class,
        styleSets
      ) {
        this.handler()
      }
  }
}

fun RBuilder.measurementList(handler: RHandler<MeasurementListProps>) = with(
  MeasurementListComponent
) {
  render(handler)
}