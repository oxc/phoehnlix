package de.esotechnik.phoehnlix.frontend.dashboard

import csstype.AlignItems
import csstype.ClassName
import csstype.Color
import csstype.Display
import csstype.FlexDirection
import csstype.FontWeight
import csstype.JustifyContent
import csstype.LetterSpacing
import csstype.NamedColor
import csstype.None
import csstype.Padding
import csstype.TextAlign
import csstype.div
import csstype.number
import csstype.px
import csstype.vw
import date_fns.FormatOptions
import date_fns.format
import date_fns.locale.de
import de.esotechnik.phoehnlix.api.model.MeasureType
import de.esotechnik.phoehnlix.api.model.Profile
import de.esotechnik.phoehnlix.api.model.ProfileMeasurement
import de.esotechnik.phoehnlix.frontend.color
import de.esotechnik.phoehnlix.frontend.parseTimestamp
import de.esotechnik.phoehnlix.frontend.title
import de.esotechnik.phoehnlix.frontend.util.circleDiameter
import de.esotechnik.phoehnlix.frontend.util.className
import de.esotechnik.phoehnlix.frontend.util.isAfter
import de.esotechnik.phoehnlix.frontend.util.isBefore
import de.esotechnik.phoehnlix.frontend.util.measurementColor
import emotion.react.css
import kotlinx.js.jso
import mui.icons.material.FitnessCenter
import mui.icons.material.ImageAspectRatio
import mui.icons.material.ScatterPlot
import mui.icons.material.Speed
import mui.icons.material.Waves
import mui.icons.material.Whatshot
import mui.material.IconButton
import mui.material.Tab
import mui.material.Tabs
import mui.material.TabsVariant
import mui.material.Typography
import mui.material.TypographyAlign
import mui.material.styles.TypographyVariant
import react.FC
import react.Fragment
import react.PropsWithChildren
import react.create
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.useCallback
import react.useEffect
import react.useInsertionEffect
import react.useLayoutEffect
import react.useMemo
import react.useRef
import react.useState
import kotlin.js.Date

/**
 * @author Bernhard Frauendienst
 */

external interface GraphProps : PropsWithChildren {
    var requestMoreData: (dateRange: DateRange, callback: (willUpdate: Boolean) -> Unit) -> Unit
    var profile: Profile?
    var selectionDate: Date?
    var measurements: List<ProfileMeasurement>
    var measureTypes: List<MeasureType>
}

enum class DashboardDateRange(val dateRange: DateRange, val dataPointCount: Int) {
    Week(Weeks(1), 7), Month(Months(1), 10), HalfYear(Months(6), 13), Year(Years(1), 13), Custom(
        Everything,
        13
    )
}

fun DateRange.toDashboardDateRange(): DashboardDateRange {
    for (range in DashboardDateRange.values()) {
        if (this == range.dateRange) {
            return range
        }
    }
    return DashboardDateRange.Custom
}

val GraphComponent = FC<GraphProps> { props ->
    var selectedRange: DateRange by useState(DashboardDateRange.Week.dateRange)
    var targetDatapointCount: Int by useState(DashboardDateRange.Week.dataPointCount)
    var visibleMeasureTypes: Set<MeasureType> by useState(props.measureTypes.toSet() - MeasureType.MetabolicRate)

    val availableMeasurements = useMemo(props.measurements) {
        props.measurements.map(::ChartMeasurement)
    }
    val selectedMeasurements = useMemo(availableMeasurements, selectedRange) {
        availableMeasurements.select(selectedRange)
    }

    val waitingForData = useRef(false)
    // useEffect would trigger only after the graph has rendered.
    // This feels way to hacky, there's got to be a better way
    val lastMeasurements = useRef(props.measurements)
    if (lastMeasurements.current !== props.measurements) {
        lastMeasurements.current = props.measurements
        waitingForData.current = false
    }

    val measureTypes = props.measureTypes
    val latest = props.measurements.last()

    fun handleRangeChange(range: DashboardDateRange) {
        val dateRange = range.dateRange
        props.requestMoreData(dateRange) { willUpdate ->
            console.log("Got more data, will update: $willUpdate")
            if (willUpdate) {
                waitingForData.current = true
            }
            selectedRange = dateRange
            targetDatapointCount = range.dataPointCount
        }
    }

    fun toggleVisibleType(measureType: MeasureType) {
        visibleMeasureTypes = visibleMeasureTypes.withElementToggled(measureType)
    }

    val graphHeadline by className
    val bulletGroup by className
    val bulletContainer by className
    val bulletCaption by className
    val timeButtonContainer by className
    val timeButtonFlexContainer by className
    val timeButton by className
    val timeButtonIndicator by className
    val graphContainer by className
    val toggleButton by className
    val buttonSelected by className
    val buttonUnchecked by className


    div {
        css {
            circleDiameter = 100.vw / (MeasureType.values().size + 1)
            // graph
            graphHeadline {
                textAlign = TextAlign.center
                marginTop = 5.px
            }
            bulletGroup {
                display = Display.flex
                justifyContent = JustifyContent.spaceEvenly
                padding = 10.px
            }
            bulletContainer {
                display = Display.flex
                flexDirection = FlexDirection.columnReverse
                alignItems = AlignItems.center
            }
            bulletCaption {

            }
            bulletStyles()
            val tabHeight = 32.px
            timeButtonContainer {
                minHeight = tabHeight
            }
            timeButtonFlexContainer {
                marginRight = (-1).px // counter the margin of the first button
            }
            timeButtonIndicator {
                display = None.none
            }
            timeButton {
                marginRight = 1.px // fake "border"
                minHeight = tabHeight
                minWidth = 0.px
                padding = Padding(4.px, 0.px)
                opacity = number(1.0)
                color = NamedColor.white
                fontWeight = FontWeight.normal
                textTransform = None.none
                letterSpacing = LetterSpacing.normal
                backgroundColor = Color("#A6A6A6")
                and(buttonSelected) {
                    backgroundColor = Color("#626262")
                    color = NamedColor.white
                }
            }
            graphContainer {
                padding = Padding(10.px, 0.px)
                backgroundColor = Color("#E6E6E6")
            }
            toggleButton {
                width = circleDiameter
                height = circleDiameter
                backgroundColor = measurementColor
                color = NamedColor.white
                MeasureType.values().forEach { type ->
                    and(type.cssClass) {
                        measurementColor = Color(type.color)
                    }
                }
                hover {
                    backgroundColor = measurementColor
                    color = NamedColor.white
                }
                and(buttonUnchecked) {
                    backgroundColor = Color("#EAEAEA")
                    color = Color("#A8A8A8")
                }
            }
        }
        Typography {
            className = graphHeadline
            component = h2
            variant = TypographyVariant.subtitle1
            align = TypographyAlign.center

            val timestamp = latest.parseTimestamp()
            val formatOptions = jso<FormatOptions> {
                locale = de
            }

            +"Ihre Messwerte vom "
            +format(timestamp, "dd.MM.yyyy", formatOptions)
        }
        div {
            className = bulletGroup
            Bullets {
                measurement = latest
                this.measureTypes = measureTypes
                component = FC { bullet ->
                    div {
                        className = bulletContainer
                        Typography {
                            className = bulletCaption
                            variant = TypographyVariant.caption
                            +bullet.measureType.title
                        }
                        div {
                            className = bullet.classNames
                            +bullet.children
                        }
                    }
                }
            }
        }
        Tabs {
            classes = jso {
                root = timeButtonContainer
                flexContainer = timeButtonFlexContainer
                indicator = timeButtonIndicator
            }
            variant = TabsVariant.fullWidth
            value = selectedRange.toDashboardDateRange()
            onChange = { _, i: dynamic -> handleRangeChange(i) }

            fun makeTab(range: DashboardDateRange, tabLabel: String) {
                Tab {
                    classes = jso {
                        root = timeButton
                        selected = buttonSelected
                    }
                    value = range
                    label = Fragment.create { +tabLabel }
                }
            }
            makeTab(DashboardDateRange.Week, "Woche")
            makeTab(DashboardDateRange.Month, "Monat")
            makeTab(DashboardDateRange.HalfYear, "6\u00a0Monate")
            makeTab(DashboardDateRange.Year, "Jahr")
            makeTab(DashboardDateRange.Custom, "…")
        }
        div {
            className = graphContainer
            MeasurementChart {
                this.skipUpdate = waitingForData.current ?: false
                this.measureTypes = measureTypes
                this.visibleMeasureTypes = visibleMeasureTypes
                this.measurements = selectedMeasurements
                this.showDatesUntil =
                    props.selectionDate?.takeIf { selectedRange.getRange().second == null }
                this.targetWeight = props.profile?.targetWeight
                this.targetDatapointCount = targetDatapointCount
                this.downsampleMethod = DownsampleMethod.Simple
            }
        }
        div {
            className = bulletGroup
            measureTypes.forEach { measureType ->
                div {
                    className = bulletContainer
                    Typography {
                        className = bulletCaption
                        variant = TypographyVariant.caption
                        +measureType.title
                    }

                    IconButton {
                        val buttonClasses = mutableListOf(toggleButton, measureType.cssClass)
                        if (measureType !in visibleMeasureTypes) buttonClasses.add(buttonUnchecked)
                        className = ClassName(buttonClasses.joinToString(" "))
                        onClick = {
                            toggleVisibleType(measureType)
                        }
                        when (measureType) {
                            MeasureType.Weight -> Speed
                            MeasureType.BodyFatPercent -> ScatterPlot
                            MeasureType.BodyWaterPercent -> Waves
                            MeasureType.MuscleMassPercent -> FitnessCenter
                            MeasureType.BodyMassIndex -> ImageAspectRatio
                            MeasureType.MetabolicRate -> Whatshot
                        } {}
                    }
                }
            }
        }
    }
}

private fun List<ChartMeasurement>.select(dateRange: DateRange): List<ChartMeasurement> {
    if (this.isEmpty()) {
        return emptyList()
    }
    val (from, to) = dateRange.getRange()
    val fromIndex = if (from == null) 0 else indexOfFirst {
        from.isBefore(it.timestamp)
    }
    val toIndex = if (to == null) size - 1 else indexOfLast {
        to.isAfter(it.timestamp)
    }
    console.log("Selecting %d..%d from %d measurements", fromIndex, toIndex, size)

    if (fromIndex == -1 || toIndex == -1) return emptyList()

    return subList(fromIndex, toIndex + 1)
}

private fun <T> Set<T>.withElementToggled(element: T): Set<T> = if (element in this) {
    this - element
} else {
    this + element
}
