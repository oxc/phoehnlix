package de.esotechnik.phoehnlix.frontend.dashboard

import Chart
import date_fns.addSeconds
import date_fns.differenceInSeconds
import date_fns.isBefore
import date_fns.locale.de
import de.esotechnik.phoehnlix.apiservice.model.ProfileMeasurement
import de.esotechnik.phoehnlix.apiservice.model.get
import de.esotechnik.phoehnlix.frontend.color
import de.esotechnik.phoehnlix.frontend.parseTimestamp
import de.esotechnik.phoehnlix.frontend.title
import de.esotechnik.phoehnlix.frontend.util.theme
import de.esotechnik.phoehnlix.model.MeasureType
import de.esotechnik.phoehnlix.model.MeasureType.*
import de.esotechnik.phoehnlix.util.roundToDigits
import kotlinx.html.id
import materialui.styles.StylesSet
import materialui.styles.childWithStyles
import materialui.styles.muitheme.MuiTheme
import materialui.styles.palette.primary
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import react.RBuilder
import react.RComponent
import react.RHandler
import react.RProps
import react.RState
import react.RStatics
import react.ReactElement
import react.createRef
import react.dom.canvas
import react.dom.div
import kotlin.js.Date

@JsModule("chartjs-adapter-date-fns")
external val chartsJsAdapterDateFns: dynamic

@JsModule("chartjs-plugin-downsample")
external val chartjsPluginDownsample: dynamic

private const val POINT_RADIUS = 6
private const val FONT_SIZE = 20

interface MeasurementChartProps : RProps {
  var measurements: List<ProfileMeasurement>
  var measureTypes: List<MeasureType>
  var targetWeight: Double?
  var targetDatapointCount: Int
  var downsampleMethod: DownsampleMethod
}

interface MeasurementChartState : RState {
  var entries: List<ChartMeasurement>
}

private val MEASURE_TYPES = values().toList()

/**
 * @author Bernhard Frauendienst
 */
class MeasurementChartComponent(props: MeasurementChartProps) : RComponent<MeasurementChartProps, MeasurementChartState>(props) {
  companion object : RStatics<MeasurementChartProps, MeasurementChartState, MeasurementChartComponent, Nothing>(
    MeasurementChartComponent::class) {
    init {
      defaultProps = new {
        measureTypes = MEASURE_TYPES
        targetDatapointCount = 13
        downsampleMethod = DownsampleMethod.Simple
      }

      // make sure dependencies are loaded
      val adapter = chartsJsAdapterDateFns
      val downsamplePlugin = chartjsPluginDownsample
    }

    private val styleSets: StylesSet.() -> Unit = {
    }

    fun RBuilder.render(handler: RHandler<MeasurementChartProps>): ReactElement =
      childWithStyles(MeasurementChartComponent::class, styleSets, withTheme = true) {
        this.handler()
      }
  }

  private val canvasRef = createRef<HTMLCanvasElement>()

  override fun RBuilder.render() {
    div {
      canvas {
        attrs.id = "chart-canvas"
        ref = canvasRef
      }
    }
  }

  override fun MeasurementChartState.init(props: MeasurementChartProps) {
    val theme = props.theme
    Chart.defaults.global.asDynamic().defaultFontFamily = theme.typography.fontFamily
    Chart.defaults.global.asDynamic().defaultFontColor = theme.palette.text.primary

    entries = props.measurements.map(::ChartMeasurement).run {
      when (props.downsampleMethod) {
        DownsampleMethod.Simple -> downsampleSimple(props.targetDatapointCount)
        else -> this
      }
    }
  }

  override fun componentDidMount() {
    createChart()
  }

  override fun componentDidUpdate(prevProps: MeasurementChartProps, prevState: MeasurementChartState, snapshot: Any) {
    createChart()
  }

  private val ctx get() = canvasRef.current!!.getContext("2d") as CanvasRenderingContext2D

  private fun createChart() {
    val entries = state.entries.takeIf { it.isNotEmpty() } ?: return
    val measureTypes = props.measureTypes
    val datasetPoints = measureTypes.associateWith { emptyArray<Chart.ChartPoint>() }
    entries.forEach { entry ->
      val timestamp = entry.timestamp
      measureTypes.forEach { type ->
        datasetPoints[type]!!.add(entry[type], timestamp)
      }
    }
    val datasetConfigs = measureTypes.mapTo(mutableListOf()) { measureType ->
      new<Chart.ChartDataSets> {
        type = "line"
        label = measureType.title
        data = datasetPoints[measureType]
        yAxisID = when (measureType) {
          Weight -> "weight"
          MetabolicRate -> "calories"
          else -> "percent"
        }
        borderColor = measureType.color
        pointBackgroundColor = borderColor
        fill = false
        pointRadius = POINT_RADIUS
      }
    }
    props.targetWeight?.let { targetWeight ->
      val datasetPos = datasetConfigs.indexOfFirst { it.yAxisID == "weight" } + 1
      if (datasetPos == 0) return@let

      val targetWeights = emptyArray<Chart.ChartPoint>()
      targetWeights.add(targetWeight, entries.first().timestamp)
      targetWeights.add(targetWeight, entries.last().timestamp)
      datasetConfigs.add(datasetPos, new {
        type = "line"
        label = "Zielgewicht"
        data = targetWeights
        yAxisID = "weight"
        borderColor = Weight.color
        pointBackgroundColor = borderColor
        fill = false
        pointRadius = 0
        pointStyle = "line"
      })
    }

    val chart = Chart(ctx, new {
      type = "line"
      data = new {
        datasets = datasetConfigs.toTypedArray()
      }
      options = new {
        scales = new<Chart.ChartScales<*>> {
          xAxes = arrayOf(new<Chart.ChartXAxe> {
            type = "time"
          }.also { o: dynamic ->
            o.adapters = js("{}")
            o.adapters.date = js("{}")
            o.adapters.date.locale = de
          })
          yAxes = arrayOf(
            new {
              id = "weight"
              type = "linear"
              position = "left"
              scaleLabel = new {
                display = true
                labelString = "kg"
              }
              props.targetWeight?.let { targetWeight ->
                ticks = new {
                  suggestedMin = targetWeight
                  suggestedMax = targetWeight
                }
              }
            }, new {
              id = "percent"
              type = "linear"
              position = "right"
              scaleLabel = new {
                display = true
                labelString = "%"
              }
              ticks = new {
                min = 10
                suggestedMax = 60
              }
              gridLines = new {
                display = false
              }
            }, new {
              id = "calories"
              type = "linear"
              position = "right"
              scaleLabel = new {
                display = true
                labelString = "kcal"
              }
              ticks = new {
                suggestedMin = 1500
                suggestedMax = 3000
              }
              gridLines = new {
                drawOnChartArea = false
              }
            }
          )
        }
        legend = new {
          position = "bottom"
          labels = new {
            usePointStyle = true
          }
        }
        if (props.downsampleMethod == DownsampleMethod.LTTB) {
          asDynamic().downsample = new<dynamic> {
            enabled = true
            threshold = targetDatapointCount
          }
        }
      }
    })
  }
}

fun RBuilder.measurementChart(handler: RHandler<MeasurementChartProps>) = with(
  MeasurementChartComponent
) {
  render(handler)
}

data class ChartMeasurement(
  val timestamp: Date,
  val values: Map<MeasureType, Double?> = mapOf()
) {
  constructor(measurement: ProfileMeasurement) : this(
    measurement.parseTimestamp(),
    MEASURE_TYPES.associateWith { measurement[it] }
  )

  operator fun get(measureType: MeasureType): Double? = values[measureType]
}

enum class DownsampleMethod {
  None, Simple, LTTB;
}

private fun List<ChartMeasurement>.downsampleSimple(targetDatapointCount: Int): List<ChartMeasurement> {
  require(targetDatapointCount > 2)
  if (size <= targetDatapointCount) {
    return this
  }

  val first = first()
  val last = last()
  val start = first.timestamp
  val end = last.timestamp

  val fullSeconds = differenceInSeconds(end, start).toLong()

  val bucketSize = fullSeconds / (targetDatapointCount-1)
  val halfSize = bucketSize / 2
  val buckets = mutableListOf<ChartMeasurement>()
  var bucketStartIndex = 0
  var bucketTime = start
  var bucketEnd = addSeconds(bucketTime, halfSize)
  // lazy hack: add a "terminal" entry that will be finalize the last bucket and then be ignored
  asSequence().plus(
    ChartMeasurement(
      timestamp = addSeconds(end, 2 * bucketSize),
      values = mapOf(Weight to Double.NaN)
    )
  )
    .forEachIndexed { index, measurement ->
    if (!isBefore(measurement.timestamp, bucketEnd)) {
      if (index > bucketStartIndex) {
        val bucketEntries = subList(bucketStartIndex, index)
        buckets += ChartMeasurement(
          timestamp = bucketTime,
          values = MEASURE_TYPES.associateWith { type ->
            bucketEntries.averageNotNullBy { it[type] }
          }
        )
        // setup next bucket
        bucketTime = addSeconds(bucketTime, bucketSize)
        bucketEnd = addSeconds(bucketEnd, bucketSize)
        bucketStartIndex = index
      }
    }
  }
  return buckets
}

private fun Array<Chart.ChartPoint>.add(value: Any?, timestamp: Date) {
  if (value != null) asDynamic().push(new<Chart.ChartPoint> {
    y = value
    x = timestamp // would use t, but does not work with downsample plugin
  })
}

fun <T : Any> Iterable<T>.averageNotNullBy(selector: (T) -> Double?) = mapNotNull(selector).average().takeUnless { it.isNaN() }?.roundToDigits(1)

inline fun <T : Any> new(init: T.() -> Unit) = js("{}").unsafeCast<T>().apply(init)