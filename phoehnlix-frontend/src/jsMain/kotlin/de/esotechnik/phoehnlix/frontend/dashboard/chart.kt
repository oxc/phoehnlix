package de.esotechnik.phoehnlix.frontend.dashboard

import Chart
import date_fns.addSeconds
import date_fns.differenceInSeconds
import date_fns.isBefore
import date_fns.locale.de
import de.esotechnik.phoehnlix.apiservice.model.ProfileMeasurement
import de.esotechnik.phoehnlix.frontend.parseTimestamp
import de.esotechnik.phoehnlix.frontend.util.customProperty
import de.esotechnik.phoehnlix.util.roundToDigits
import kotlinx.css.Color
import kotlinx.css.FontWeight
import kotlinx.css.TextAlign
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
import kotlinx.css.width
import kotlinx.html.id
import materialui.styles.StylesSet
import materialui.styles.childWithStyles
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
  var targetWeight: Double?
  var targetDatapointCount: Int
  var downsampleMethod: DownsampleMethod
}

interface MeasurementChartState : RState {
  var entries: List<ChartMeasurement>
  var chart: Chart
}

/**
 * @author Bernhard Frauendienst
 */
class MeasurementChartComponent(props: MeasurementChartProps) : RComponent<MeasurementChartProps, MeasurementChartState>(props) {
  companion object : RStatics<MeasurementChartProps, MeasurementChartState, MeasurementChartComponent, Nothing>(
    MeasurementChartComponent::class) {
    init {
      defaultProps = new {
        targetDatapointCount = 13
        downsampleMethod = DownsampleMethod.Simple
      }

      // make sure dependencies are loaded
      val adapter = chartsJsAdapterDateFns
      val downsamplePlugin = chartjsPluginDownsample
      Chart.defaults.global.asDynamic().defaultFontSize =
        FONT_SIZE
    }

    private val styleSets: StylesSet.() -> Unit = {
    }

    fun RBuilder.render(handler: RHandler<MeasurementChartProps>): ReactElement =
      childWithStyles(
        MeasurementChartComponent::class,
        MeasurementChartComponent.styleSets
      ) {
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
    state.chart = Chart(ctx, new {
      type = "line"
      data = new {
        val weights = emptyArray<Chart.ChartPoint>()
        val targetWeights = emptyArray<Chart.ChartPoint>()
        val bodyFatPercent = emptyArray<Chart.ChartPoint>()
        val bodyWaterPercent = emptyArray<Chart.ChartPoint>()
        val muscleMassPercent = emptyArray<Chart.ChartPoint>()
        val bodyMassIndex = emptyArray<Chart.ChartPoint>()
        val metabolicRate = emptyArray<Chart.ChartPoint>()

        entries.forEach { entry ->
          val timestamp = entry.timestamp
          weights.add(entry.weight, timestamp)
          bodyFatPercent.add(entry.bodyFatPercent, timestamp)
          bodyWaterPercent.add(entry.bodyWaterPercent, timestamp)
          muscleMassPercent.add(entry.muscleMassPercent, timestamp)
          bodyMassIndex.add(entry.bodyMassIndex, timestamp)
          metabolicRate.add(entry.metabolicRate, timestamp)
        }
        props.targetWeight?.let { targetWeight ->
          targetWeights.add(targetWeight, entries.first().timestamp)
          targetWeights.add(targetWeight, entries.last().timestamp)
        }
        datasets = arrayOf(
          new {
            type = "line"
            label = "Gewicht"
            data = weights
            yAxisID = "weight"
            borderColor = "#cd2129" // "#a32530"
            pointBackgroundColor = borderColor
            fill = false
            pointRadius = POINT_RADIUS
          }, new {
            type = "line"
            label = "Zielgewicht"
            data = targetWeights
            yAxisID = "weight"
            borderColor = "#cd2129" // "#a32530"
            pointBackgroundColor = borderColor
            fill = false
            pointRadius = 0
            pointStyle = "line"
          }, new {
            type = "line"
            label = "Fett%"
            data = bodyFatPercent
            yAxisID = "percent"
            borderColor = "#faa21e" // "#c6a92e"
            pointBackgroundColor = borderColor
            fill = false
            pointRadius = POINT_RADIUS
          },
          new {
            label = "Wasser%"
            data = bodyWaterPercent
            yAxisID = "percent"
            borderColor = "#2395cb" // "#44a6ab"
            pointBackgroundColor = borderColor
            fill = false
            pointRadius = POINT_RADIUS
          },
          new {
            label = "Muskel%"
            data = muscleMassPercent
            yAxisID = "percent"
            borderColor = "#a408a4" // "#aa0a7c"
            pointBackgroundColor = borderColor
            fill = false
            pointRadius = POINT_RADIUS
          },
          new {
            label = "BMI"
            data = bodyMassIndex
            yAxisID = "percent"
            borderColor = "#76b525" // "#5896ce"
            pointBackgroundColor = borderColor
            fill = false
            pointRadius = POINT_RADIUS
          },
          new {
            label = "Kalorien"
            data = metabolicRate
            yAxisID = "calories"
            pointBackgroundColor = borderColor
            fill = false
            pointRadius = POINT_RADIUS
          }
        )
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
                min = 0
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
                suggestedMin = 3000
                suggestedMax = 6000
              }
              gridLines = new {
                display = false
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
  val weight: Double,

  val bodyFatPercent: Double? = null,
  val bodyWaterPercent: Double? = null,
  val muscleMassPercent: Double? = null,
  val bodyMassIndex: Double? = null,
  val metabolicRate: Double? = null
) {
  constructor(measurement: ProfileMeasurement) : this(
    measurement.parseTimestamp(),
    measurement.weight,
    measurement.bodyFatPercent,
    measurement.bodyWaterPercent,
    measurement.muscleMassPercent,
    measurement.bodyMassIndex,
    measurement.metabolicRate
  )
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
      timestamp = addSeconds(
        end,
        2 * bucketSize
      ), weight = Double.NaN
    )
  )
    .forEachIndexed { index, measurement ->
    if (!isBefore(measurement.timestamp, bucketEnd)) {
      if (index > bucketStartIndex) {
        val bucketEntries = subList(bucketStartIndex, index)
        buckets += ChartMeasurement(
          timestamp = bucketTime,
          weight = bucketEntries.averageBy { it.weight }!!,
          bodyFatPercent = bucketEntries.averageNotNullBy { it.bodyFatPercent },
          bodyWaterPercent = bucketEntries.averageNotNullBy { it.bodyWaterPercent },
          muscleMassPercent = bucketEntries.averageNotNullBy { it.muscleMassPercent },
          bodyMassIndex = bucketEntries.averageNotNullBy { it.bodyMassIndex },
          metabolicRate = bucketEntries.averageNotNullBy { it.metabolicRate }
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

fun <T : Any> Iterable<T>.averageBy(selector: (T) -> Double) = map(selector).average().takeUnless { it.isNaN() }?.roundToDigits(1)
fun <T : Any> Iterable<T>.averageNotNullBy(selector: (T) -> Double?) = mapNotNull(selector).average().takeUnless { it.isNaN() }?.roundToDigits(1)

inline fun <T : Any> new(init: T.() -> Unit) = js("{}").unsafeCast<T>().apply(init)