package de.esotechnik.phoehnlix.frontend.dashboard

import date_fns.FormatOptions
import date_fns.format
import date_fns.locale.de
import de.esotechnik.phoehnlix.apiservice.model.ProfileMeasurement
import de.esotechnik.phoehnlix.frontend.parseTimestamp
import de.esotechnik.phoehnlix.frontend.util.circleDiameter
import de.esotechnik.phoehnlix.frontend.util.styleSets
import de.esotechnik.phoehnlix.model.MeasureType
import kotlinx.css.Color
import kotlinx.css.TextAlign
import kotlinx.css.backgroundColor
import kotlinx.css.padding
import kotlinx.css.paddingLeft
import kotlinx.css.paddingRight
import kotlinx.css.px
import kotlinx.css.textAlign
import materialui.components.icon.icon
import materialui.components.iconbutton.iconButton
import materialui.components.table.TableProps
import materialui.components.table.table
import materialui.components.tablebody.tableBody
import materialui.components.tablecell.tdCell
import materialui.components.tablerow.enums.TableRowStyle
import materialui.components.tablerow.tableRow
import materialui.styles.withStyles
import react.RBuilder
import react.RComponent
import react.RHandler
import react.RState
import react.dom.div
import react.dom.span

/**
 * @author Bernhard Frauendienst
 */

private val MEASURE_TYPES = MeasureType.values().toList()

interface MeasurementListProps : TableProps {
  var measurements: List<ProfileMeasurement>
}

class MeasurementListComponent : RComponent<MeasurementListProps, RState>() {
  override fun RBuilder.render() {
    val root by styleSets
    val bulletRow by styleSets

    val formatOptions = new<FormatOptions> {
      locale = de
    }

    table(root) {
      tableBody {
        props.measurements.forEach { entry ->
          tableRow(TableRowStyle.root to bulletRow) {
            tdCell {
              val timestamp = entry.parseTimestamp()
              +format(timestamp, "EE dd.MM.yyyy", formatOptions)
              +"\n"
              span {
                +(format(timestamp, "HH.mm", formatOptions) + " Uhr")
              }
            }
            bullets(entry, MEASURE_TYPES, props) { _, classes, content ->
              tdCell {
                div(classes) {
                  content()
                }
              }
            }
            tdCell {
              iconButton {
                icon { +"more_vert" }
              }
            }
          }
        }
      }
    }
  }
}

private val styledComponent = withStyles(MeasurementListComponent::class, {
  "root" {
    circleDiameter = 40.px
    backgroundColor = Color("#E6E6E6")
  }
  "bulletRow" {
    children("td") {
      padding(2.px)
      textAlign = TextAlign.center
      firstOfType {
        backgroundColor = Color.white
        padding(horizontal = 10.px)
      }
      nthOfType("2") {
        paddingLeft = 10.px
      }
    }
  }
  makeBulletStyles()
})

fun RBuilder.measurementList(handler: RHandler<MeasurementListProps>) =
  styledComponent(handler)