package de.esotechnik.phoehnlix.frontend.dashboard

import date_fns.FormatOptions
import date_fns.format
import date_fns.locale.de
import de.esotechnik.phoehnlix.api.model.MeasureType
import de.esotechnik.phoehnlix.api.model.ProfileMeasurement
import de.esotechnik.phoehnlix.frontend.parseTimestamp
import de.esotechnik.phoehnlix.frontend.util.circleDiameter
import de.esotechnik.phoehnlix.frontend.util.styleSets
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.Position
import kotlinx.css.TextAlign
import kotlinx.css.backgroundColor
import kotlinx.css.display
import kotlinx.css.left
import kotlinx.css.margin
import kotlinx.css.padding
import kotlinx.css.paddingLeft
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.px
import kotlinx.css.textAlign
import kotlinx.css.top
import kotlinx.html.js.onClickFunction
import materialui.components.button.button
import materialui.components.circularprogress.circularProgress
import materialui.components.circularprogress.enums.CircularProgressStyle
import materialui.components.icon.icon
import materialui.components.iconbutton.iconButton
import materialui.components.table.TableProps
import materialui.components.table.table
import materialui.components.tablebody.tableBody
import materialui.components.tablecell.tdCell
import materialui.components.tablefooter.tableFooter
import materialui.components.tablerow.enums.TableRowStyle
import materialui.components.tablerow.tableRow
import materialui.styles.withStyles
import org.w3c.dom.HTMLElement
import react.RBuilder
import react.RComponent
import react.RHandler
import react.RState
import react.createRef
import react.dom.div
import react.dom.span

/**
 * @author Bernhard Frauendienst
 */

private val MEASURE_TYPES = MeasureType.values().toList()

interface MeasurementListProps : TableProps {
  var requestMoreData: (() -> Unit)?
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
      props.requestMoreData?.let { requestMoreData ->
        val loadMoreProgress by styleSets

        tableFooter {
          tableRow {
            tdCell {
              attrs {
                colSpan = (MEASURE_TYPES.size + 2).toString()
              }
              button {
                val progressRef = createRef<HTMLElement>()
                attrs {
                  fullWidth = true
                  onClickFunction = {
                    this.disabled = true
                    progressRef.current!!.style.display = "block"
                    requestMoreData()
                  }
                }
                circularProgress(CircularProgressStyle.root to loadMoreProgress) {
                  ref = progressRef
                  attrs {
                    size(24)
                  }
                }
                +"Alle Einträge anzeigen"
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
      firstOfType {
        backgroundColor = Color.white
        padding(horizontal = 10.px)
      }
      !firstOfType {
        textAlign = TextAlign.center
      }
      nthOfType("2") {
        paddingLeft = 10.px
      }
    }
  }
  makeBulletStyles()
  "loadMoreProgress" {
    display = Display.none
    position = Position.absolute
    top = 50.pct
    left = 50.pct
    margin(top = (-12).px, left = (-12).px)
  }
})

fun RBuilder.measurementList(handler: RHandler<MeasurementListProps>) =
  styledComponent(handler)