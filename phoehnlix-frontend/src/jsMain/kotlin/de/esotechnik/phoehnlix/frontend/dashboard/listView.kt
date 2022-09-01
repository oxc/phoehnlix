package de.esotechnik.phoehnlix.frontend.dashboard

import csstype.ClassName
import csstype.Color
import csstype.NamedColor
import csstype.None.none
import csstype.Position
import csstype.TextAlign
import csstype.pct
import csstype.px
import date_fns.FormatOptions
import date_fns.format
import date_fns.locale.de
import de.esotechnik.phoehnlix.api.model.MeasureType
import de.esotechnik.phoehnlix.api.model.ProfileMeasurement
import de.esotechnik.phoehnlix.frontend.parseTimestamp
import de.esotechnik.phoehnlix.frontend.util.circleDiameter
import emotion.react.css
import kotlinx.js.jso
import mui.icons.material.MoreVert
import mui.material.Button
import mui.material.CircularProgress
import mui.material.IconButton
import mui.material.Table
import mui.material.TableBody
import mui.material.TableCell
import mui.material.TableFooter
import mui.material.TableProps
import mui.material.TableRow
import org.w3c.dom.HTMLElement
import react.FC
import react.createRef
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span

/**
 * @author Bernhard Frauendienst
 */

private val MEASURE_TYPES = MeasureType.values().toList()

external interface MeasurementListProps : TableProps {
  var requestMoreData: (() -> Unit)?
  var measurements: List<ProfileMeasurement>
}

val MeasurementList = FC<MeasurementListProps> { props ->

  val formatOptions = jso<FormatOptions> {
    locale = de
  }

  val bulletRow = ClassName("bulletRow")
  val loadMoreProgress = ClassName("loadMoreProgress")
  Table {
    css {
      circleDiameter = 40.px
      backgroundColor = Color("#E6E6E6")
      bulletRow {
        "> td" {
          padding = 2.px
          firstOfType {
            backgroundColor = NamedColor.white
            paddingLeft = 10.px
            paddingRight = 10.px
          }
          not(":first-of-type") {
            textAlign = TextAlign.center
          }
          nthOfType("2") {
            paddingLeft = 10.px
          }
        }
      }
      bulletStyles()
      loadMoreProgress {
        display = none
        position = Position.absolute
        top = 50.pct
        left = 50.pct
        marginTop = (-12).px
        marginLeft = (-12).px
      }
    }

    TableBody {
      props.measurements.forEach { entry ->
        TableRow {
          className = bulletRow
          TableCell {
            val timestamp = entry.parseTimestamp()
            +format(timestamp, "EE dd.MM.yyyy", formatOptions)
            +"\n"
            span {
              +(format(timestamp, "HH.mm", formatOptions) + " Uhr")
            }
          }
          Bullets {
            measurement = entry
            measureTypes = MEASURE_TYPES
            component = FC { bullet ->
              TableCell {
                div {
                  className = bullet.classNames
                  +bullet.children
                }
              }
            }
          }
          TableCell {
            IconButton {
              MoreVert {}
            }
          }
        }
      }
    }
    props.requestMoreData?.let { requestMoreData ->
      TableFooter {
        TableRow {
          TableCell {
            colSpan = (MEASURE_TYPES.size + 2)
            Button {
              val progressRef = createRef<HTMLElement>()
                fullWidth = true
                onClick = {
                  this.disabled = true
                  progressRef.current!!.style.display = "block"
                  requestMoreData()
              }
              CircularProgress {
                className = loadMoreProgress
                ref = progressRef
                size = 24
              }
              +"Alle Einträge anzeigen"
            }
          }
        }
      }
    }
  }
}