package de.esotechnik.phoehnlix.frontend.dashboard

import date_fns.FormatOptions
import date_fns.format
import date_fns.locale.de
import de.esotechnik.phoehnlix.apiservice.model.Profile
import de.esotechnik.phoehnlix.apiservice.model.ProfileMeasurement
import de.esotechnik.phoehnlix.frontend.color
import de.esotechnik.phoehnlix.frontend.parseTimestamp
import de.esotechnik.phoehnlix.frontend.title
import de.esotechnik.phoehnlix.frontend.util.circleDiameter
import de.esotechnik.phoehnlix.frontend.util.isAfter
import de.esotechnik.phoehnlix.frontend.util.isBefore
import de.esotechnik.phoehnlix.frontend.util.measurementColor
import de.esotechnik.phoehnlix.frontend.util.memoizeOne
import de.esotechnik.phoehnlix.frontend.util.styleSets
import de.esotechnik.phoehnlix.model.MeasureType
import kotlinx.atomicfu.atomic
import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.FontWeight
import kotlinx.css.JustifyContent
import kotlinx.css.LinearDimension
import kotlinx.css.TextAlign
import kotlinx.css.TextTransform
import kotlinx.css.alignItems
import kotlinx.css.backgroundColor
import kotlinx.css.color
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.fontWeight
import kotlinx.css.height
import kotlinx.css.justifyContent
import kotlinx.css.letterSpacing
import kotlinx.css.marginRight
import kotlinx.css.marginTop
import kotlinx.css.minHeight
import kotlinx.css.opacity
import kotlinx.css.padding
import kotlinx.css.px
import kotlinx.css.textAlign
import kotlinx.css.textTransform
import kotlinx.css.vw
import kotlinx.css.width
import kotlinx.html.H2
import kotlinx.html.Tag
import kotlinx.html.js.onClickFunction
import materialui.components.icon.icon
import materialui.components.iconbutton.enums.IconButtonStyle
import materialui.components.iconbutton.iconButton
import materialui.components.tab.enums.TabStyle
import materialui.components.tab.tab
import materialui.components.tabs.enums.TabsStyle
import materialui.components.tabs.enums.TabsVariant
import materialui.components.tabs.tabs
import materialui.components.typography.enums.TypographyAlign
import materialui.components.typography.enums.TypographyStyle
import materialui.components.typography.enums.TypographyVariant
import materialui.components.typography.typography
import materialui.styles.withStyles
import react.RBuilder
import react.RComponent
import react.RHandler
import react.RProps
import react.RState
import react.dom.div
import react.setState

/**
 * @author Bernhard Frauendienst
 */

interface GraphProps : RProps {
  var requestMoreData: (dateRange: DateRange, callback: (willUpdate: Boolean) -> Unit) -> Unit
  var profile: Profile?
  var measurements: List<ProfileMeasurement>
  var measureTypes: List<MeasureType>
}

interface GraphState : RState {
  var selectedRange: DateRange
  var targetDatapointCount: Int
  var visibleMeasureTypes: Set<MeasureType>
  var waitingForData: Boolean
}

enum class DashboardDateRange(val dateRange: DateRange, val dataPointCount: Int) {
  Week(Weeks(1), 7), Month(Months(1), 10), HalfYear(Months(6), 13), Year(Years(1), 13), Custom(Everything, 13)
}

fun DateRange.toDashboardDateRange(): DashboardDateRange {
  for (range in DashboardDateRange.values()) {
    if (this == range.dateRange) {
      return range
    }
  }
  return DashboardDateRange.Custom
}

class GraphFragment(props: GraphProps) : RComponent<GraphProps, GraphState>(props) {
  override fun GraphState.init(props: GraphProps) {
    visibleMeasureTypes = props.measureTypes.toSet() - MeasureType.MetabolicRate
    selectedRange = DashboardDateRange.Week.dateRange
    targetDatapointCount = DashboardDateRange.Week.dataPointCount
  }

  private val availableMeasurements by memoizeOne({ it.map(::ChartMeasurement)}) { this.props.measurements }
  private val selectedMeasurements by memoizeOne({ p1, p2 -> p1.select(p2) },
    { availableMeasurements }, { state.selectedRange })

  private val waitingForData by memoizeOne({ atomic(false) }) { availableMeasurements }

  override fun RBuilder.render() {
    val root by styleSets
    val graphHeadline by styleSets
    val bulletGroup by styleSets
    val bulletContainer by styleSets
    val bulletCaption by styleSets
    val timeButtonContainer by styleSets
    val timeButton by styleSets
    val timeButtonIndicator by styleSets
    val graphContainer by styleSets
    val toggleButton by styleSets
    val selected by styleSets
    val unchecked by styleSets

    val measureTypes = props.measureTypes
    val visibleMeasureTypes = state.visibleMeasureTypes
    val latest = props.measurements.last()

    div(root) {
      typography(TypographyStyle.root to graphHeadline, factory = { H2(mapOf(), it) }) {
        attrs.variant = TypographyVariant.subtitle1
        attrs.align = TypographyAlign.center

        val timestamp = latest.parseTimestamp()
        val formatOptions = new<FormatOptions> {
          locale = de
        }

        +"Ihre Messwerte vom "
        +format(timestamp, "dd.MM.yyyy", formatOptions)
      }
      div(bulletGroup) {
        bullets(latest, measureTypes, props) { measureType, classes, content ->
          div(bulletContainer) {
            typography(TypographyStyle.root to bulletCaption) {
              attrs.variant = TypographyVariant.caption
              +measureType.title
            }
            div(classes) {
              content()
            }
          }
        }
      }
      tabs(
        TabsStyle.root to timeButtonContainer,
        TabsStyle.indicator to timeButtonIndicator
      ) {
        attrs {
          variant = TabsVariant.fullWidth
          value = state.selectedRange.toDashboardDateRange()
          onChange = { _, i: dynamic -> handleRangeChange(i) }
        }
        fun makeTab(range: DashboardDateRange, label: String) {
          tab(TabStyle.root to timeButton, TabStyle.selected to selected) {
            attrs {
              (this as Tag).value = range
              label { +label }
            }
          }
        }
        makeTab(DashboardDateRange.Week, "Woche")
        makeTab(DashboardDateRange.Month, "Monat")
        makeTab(DashboardDateRange.HalfYear, "6\u00a0Monate")
        makeTab(DashboardDateRange.Year, "Jahr")
        makeTab(DashboardDateRange.Custom, "…")
      }
      div(graphContainer) {
        measurementChart {
          attrs.skipUpdate = waitingForData.value
          attrs.measureTypes = measureTypes
          attrs.visibleMeasureTypes = visibleMeasureTypes
          attrs.measurements = selectedMeasurements
          attrs.targetWeight = props.profile?.targetWeight
          attrs.targetDatapointCount = state.targetDatapointCount
        }
      }
      div(bulletGroup) {
        measureTypes.forEach { measureType ->
          div(bulletContainer) {
            typography(TypographyStyle.root to bulletCaption) {
              attrs.variant = TypographyVariant.caption
              +measureType.title
            }

            val uncheckedClass = if (measureType !in visibleMeasureTypes) unchecked else ""
            iconButton(IconButtonStyle.root to "$toggleButton ${measureType.cssClass} $uncheckedClass") {
              attrs {
                onClickFunction = {
                  toggleVisibleType(measureType)
                }
              }
              icon {
                +when (measureType) {
                  MeasureType.Weight -> "speed"
                  MeasureType.BodyFatPercent -> "scatter_plot"
                  MeasureType.BodyWaterPercent -> "waves"
                  MeasureType.MuscleMassPercent -> "fitness_center"
                  MeasureType.BodyMassIndex -> "image_aspect_ratio"
                  MeasureType.MetabolicRate -> "whatshot"
                }
              }
            }
          }
        }
      }
    }
  }

  private fun handleRangeChange(range: DashboardDateRange) {
    val dateRange = range.dateRange
    props.requestMoreData(dateRange) { willUpdate ->
      if (willUpdate) waitingForData.getAndSet(true)
      setState {
        selectedRange = dateRange
        targetDatapointCount = range.dataPointCount
      }
    }
  }

  private fun toggleVisibleType(measureType: MeasureType) {
    setState {
      visibleMeasureTypes = state.visibleMeasureTypes.withElementToggled(measureType)
    }
  }
}

private fun List<ChartMeasurement>.select(dateRange: DateRange): List<ChartMeasurement> {
  val (from, to) = dateRange.getRange()
  if (this.isNullOrEmpty()) {
    return emptyList()
  }
  val fromIndex = if (from == null) 0 else indexOfFirst {
    from.isBefore(it.timestamp)
  }
  val toIndex = if (to == null) size-1 else indexOfLast {
    to.isAfter(it.timestamp)
  }
  console.log("Selecting %d..%d from %d measurements", fromIndex, toIndex, size)

  if (fromIndex == -1 || toIndex == -1) return emptyList()

  return subList(fromIndex, toIndex+1)
}

private fun <T> Set<T>.withElementToggled(element: T): Set<T> = if (element in this) {
  this - element
} else {
  this + element
}

private val styledComponent = withStyles(GraphFragment::class, {
  "root" {
    circleDiameter = 100.vw.div(MeasureType.values().size + 1)
  }
  // graph
  "graphHeadline" {
    textAlign = TextAlign.center
    marginTop = 5.px
  }
  "bulletGroup" {
    display = Display.flex
    justifyContent = JustifyContent.spaceEvenly
    padding(10.px)
  }
  "bulletContainer" {
    display = Display.flex
    flexDirection = FlexDirection.columnReverse
    alignItems = Align.center
  }
  "bulletCaption" {

  }
  makeBulletStyles()
  val tabHeight = 32.px
  "timeButtonContainer" {
    minHeight = tabHeight
    marginRight = (-1).px // counter the margin of the first button
  }
  "timeButtonIndicator" {
    display = Display.none
  }
  "timeButton" {
    marginRight = 1.px // fake "border"
    minHeight = tabHeight
    padding(vertical = 4.px)
    opacity = 1
    color = Color.white
    fontWeight = FontWeight.normal
    textTransform = TextTransform.none
    letterSpacing = LinearDimension.initial
    backgroundColor = Color("#A6A6A6")
    "&\$selected" {
      backgroundColor = Color("#626262")
    }
  }
  "graphContainer" {
    padding(10.px, 0.px)
    backgroundColor = Color("#E6E6E6")
  }
  "toggleButton" {
    width = circleDiameter
    height = circleDiameter
    backgroundColor = measurementColor
    color = Color.white
    MeasureType.values().forEach { type ->
      +type.cssClass {
        measurementColor = Color(type.color)
      }
    }
    hover {
      backgroundColor = measurementColor
      color = Color.white
    }
    "&\$unchecked" {
      backgroundColor = Color("#EAEAEA")
      color = Color("#A8A8A8")
    }
  }

  "unchecked" {}
  "selected" {}
})

fun RBuilder.graphFragment(handler: RHandler<GraphProps>) = styledComponent(handler)
