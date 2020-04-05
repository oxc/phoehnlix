package de.esotechnik.phoehnlix.frontend.dashboard

import date_fns.FormatOptions
import date_fns.format
import date_fns.locale.de
import de.esotechnik.phoehnlix.apiservice.model.ProfileMeasurement
import de.esotechnik.phoehnlix.apiservice.model.get
import de.esotechnik.phoehnlix.frontend.color
import de.esotechnik.phoehnlix.frontend.parseTimestamp
import de.esotechnik.phoehnlix.frontend.unit
import de.esotechnik.phoehnlix.frontend.util.circleDiameter
import de.esotechnik.phoehnlix.frontend.util.measurementColor
import de.esotechnik.phoehnlix.frontend.util.style
import de.esotechnik.phoehnlix.frontend.util.styleSets
import de.esotechnik.phoehnlix.model.MeasureType
import de.esotechnik.phoehnlix.util.formatDecimalDigits
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
import kotlinx.css.padding
import kotlinx.css.pct
import kotlinx.css.properties.lh
import kotlinx.css.px
import kotlinx.css.textAlign
import kotlinx.css.width
import materialui.components.table.TableProps
import materialui.components.table.table
import materialui.components.tablebody.tableBody
import materialui.components.tablecell.enums.TableCellStyle
import materialui.components.tablecell.tdCell
import materialui.components.tablerow.tableRow
import materialui.components.typography.typography
import materialui.styles.StylesSet
import materialui.styles.childWithStyles
import react.RBuilder
import react.RComponent
import react.RHandler
import react.RState
import react.ReactElement
import react.dom.div
import react.dom.span
import react.dom.sup

/**
 * @author Bernhard Frauendienst
 */

private val MEASURE_TYPES = MeasureType.values().toList()

interface MeasurementListProps : TableProps {
  var measurements: List<ProfileMeasurement>
}

class MeasurementListComponent : RComponent<MeasurementListProps, RState>() {
  override fun RBuilder.render() {
    val bulletCell by styleSets
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
            bullets(entry, MEASURE_TYPES, props) { _, classes, content ->
              tdCell(TableCellStyle.root to bulletCell) {
                div(classes) {
                  content()
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
      "bulletCell" {
        padding(2.px)
      }
      makeBulletStyles(diameter = 50.px, fontSize = 14.px)
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