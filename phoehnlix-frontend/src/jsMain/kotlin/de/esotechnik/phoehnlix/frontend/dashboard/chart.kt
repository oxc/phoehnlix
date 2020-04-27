package de.esotechnik.phoehnlix.frontend.dashboard

import Chart
import date_fns.addSeconds
import date_fns.differenceInSeconds
import date_fns.isBefore
import date_fns.locale.de
import de.esotechnik.phoehnlix.frontend.color
import de.esotechnik.phoehnlix.frontend.parseTimestamp
import de.esotechnik.phoehnlix.frontend.title
import de.esotechnik.phoehnlix.frontend.util.entrySequence
import de.esotechnik.phoehnlix.frontend.util.theme
import de.esotechnik.phoehnlix.api.model.MeasureType
import de.esotechnik.phoehnlix.api.model.MeasureType.*
import de.esotechnik.phoehnlix.api.model.ProfileMeasurement
import de.esotechnik.phoehnlix.api.model.get
import de.esotechnik.phoehnlix.util.roundToDigits
import kotlinx.html.id
import materialui.styles.palette.primary
import materialui.styles.withStyles
import org.w3c.dom.BOTTOM
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.CanvasTextAlign
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.CanvasTextBaseline
import org.w3c.dom.LEFT
import org.w3c.dom.RIGHT
import react.RBuilder
import react.RHandler
import react.RProps
import react.RPureComponent
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

interface MeasurementChartProps : RProps {
  var skipUpdate: Boolean
  var measurements: List<ChartMeasurement>
  var measureTypes: List<MeasureType>
  var visibleMeasureTypes: Set<MeasureType>
  var showDatesUntil: Date?
  var targetWeight: Double?
  var targetDatapointCount: Int
  var downsampleMethod: DownsampleMethod
}

interface MeasurementChartState : RState {
}

private val MEASURE_TYPES = values().toList()

/**
 * @author Bernhard Frauendienst
 */
class MeasurementChartComponent(props: MeasurementChartProps) : RPureComponent<MeasurementChartProps, MeasurementChartState>(props) {
  companion object : RStatics<MeasurementChartProps, MeasurementChartState, MeasurementChartComponent, Nothing>(
    MeasurementChartComponent::class) {
    init {
      defaultProps = new {
        measureTypes = MEASURE_TYPES
        visibleMeasureTypes = MEASURE_TYPES.toSet()
        targetDatapointCount = 13
        downsampleMethod = DownsampleMethod.Simple
      }

      // make sure dependencies are loaded
      @Suppress("UNUSED_VARIABLE")
      val adapter = chartsJsAdapterDateFns
      @Suppress("UNUSED_VARIABLE")
      val downsamplePlugin = chartjsPluginDownsample
    }

    private val styledComponent = withStyles(MeasurementChartComponent::class, {}, withTheme = true)

    fun RBuilder.render(handler: RHandler<MeasurementChartProps>): ReactElement =
      styledComponent(handler)
  }

  private val canvasRef = createRef<HTMLCanvasElement>()

  private var chartInstance: Chart? = null

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
  }

  override fun componentDidMount() = createChart()

  override fun componentDidUpdate(prevProps: MeasurementChartProps, prevState: MeasurementChartState, snapshot: Any) {
    if (!applyInplaceUpdate(prevProps, prevState) && !props.skipUpdate) {
      destroyChart()
      createChart()
    }
  }

  private fun applyInplaceUpdate(prevProps: MeasurementChartProps, prevState: MeasurementChartState): Boolean {
    val chartInstance = this.chartInstance ?: return false.also { console.log("no chart instance.") }
    if (state !== prevState) return false.also { console.log("state changed.") }
    if (props === prevProps) return true // shouldn't happen
    if (props.downsampleMethod != prevProps.downsampleMethod) return false.also { console.log("downsample changed.") }
    if (props.measureTypes != prevProps.measureTypes) return false.also { console.log("measureTypes changed.") }
    if (props.targetDatapointCount != prevProps.targetDatapointCount) return false.also { console.log("targetDatapointCount changed.") }
    if (props.measurements != prevProps.measurements) return false.also { console.log("measurements changed.") }
    if (props.targetWeight != prevProps.targetWeight) return false.also { console.log("targetWeight changed.") }
    if (props.visibleMeasureTypes != prevProps.visibleMeasureTypes) {
      chartInstance.data.datasets?.forEach { dataset ->
        dataset.hidden = dataset.measureType !in props.visibleMeasureTypes
      }
    }
    chartInstance.update(new { duration = 0 })
    return true
  }

  override fun componentWillUnmount() = destroyChart()

  private val ctx get() = canvasRef.current!!.getContext("2d") as CanvasRenderingContext2D

  private fun destroyChart() {
    chartInstance?.destroy()
    chartInstance = null
  }

  private fun createChart() {
    val entries = props.measurements.run {
      when (props.downsampleMethod) {
        DownsampleMethod.Simple -> downsampleSimple(props.targetDatapointCount)
        else -> this
      }
    }
    if (entries.isEmpty()) {
      return
    }
    val measureTypes = props.measureTypes
    val visibleMeasureTypes = props.visibleMeasureTypes
    val datasetPoints = measureTypes.associateWith { emptyArray<Chart.ChartPoint>() }
    entries.forEach { entry ->
      val timestamp = entry.timestamp
      measureTypes.forEach { type ->
        datasetPoints[type]!!.add(entry[type], timestamp)
      }
    }
    val datasetConfigs = measureTypes.mapTo(mutableListOf()) { measureType ->
      new<Chart.ChartDataSets> {
        this.measureType = measureType
        hidden = measureType !in visibleMeasureTypes
        type = "line"
        label = measureType.title
        data = datasetPoints[measureType]
        yAxisID = measureType.yAxisId
        borderColor = measureType.color
        pointBackgroundColor = borderColor
        fill = false
        borderWidth = 2
        pointRadius = 2
      }
    }
    if (Weight in measureTypes) {
      props.targetWeight?.let { targetWeight ->
        val targetWeights = emptyArray<Chart.ChartPoint>()
        targetWeights.add(targetWeight, entries.first().timestamp)
        targetWeights.add(targetWeight, props.showDatesUntil ?: entries.last().timestamp)
        datasetConfigs.add(new {
          measureType = Weight
          hidden = Weight !in visibleMeasureTypes
          type = "line"
          label = "Zielgewicht"
          data = targetWeights
          yAxisID = "weight"
          borderColor = Weight.color
          pointBackgroundColor = borderColor
          fill = false
          borderWidth = 2
          pointRadius = 0
          pointStyle = "line"
        })
      }
    }

    this.chartInstance = Chart(ctx, new {
      type = "line"
      data = new {
        datasets = datasetConfigs.toTypedArray()
      }
      options = new {
        layout = new {
          padding = new<Chart.ChartLayoutPaddingObject> {
            top = 30
          }
        }
        scales = new<Chart.ChartScales<*>> {
          xAxes = arrayOf(new<Chart.ChartXAxe> {
            type = "time"
            time = new {
              minUnit = "day"
              max = props.showDatesUntil.asDynamic()
            }
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
              display = "auto"
              scaleLabel = new {
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
              display = "auto"
              scaleLabel = new {
                labelString = "%"
              }
              ticks = new {
                suggestedMin = 10
                suggestedMax = 60
              }
              gridLines = new {
                display = false
              }
            }, new {
              id = "calories"
              type = "linear"
              position = "right"
              display = "auto"
              scaleLabel = new {
                labelString = "kcal"
              }
              ticks = new {
                suggestedMin = 1500
                suggestedMax = 3000
              }
              gridLines = new {
                drawBorder = false
                drawOnChartArea = false
              }
            }
          )
        }
        legend = new {
          display = false
        }
        if (props.downsampleMethod == DownsampleMethod.LTTB) {
          asDynamic().downsample = new<dynamic> {
            enabled = true
            threshold = targetDatapointCount
          }
        }
      }
      plugins = arrayOf(
        TopYAxisLabelPlugin.plugin
      )
    })
  }
}

private val MeasureType.yAxisId get() = when (this) {
  Weight -> "weight"
  MetabolicRate -> "calories"
  else -> "percent"
}

object TopYAxisLabelPlugin {
  @Suppress("MemberVisibilityCanBePrivate", "UNUSED_PARAMETER")
  fun afterDatasetsDraw(chart: dynamic, options: dynamic) {
    val helpers = Chart.helpers.asDynamic()
    val defaults = Chart.defaults.asDynamic()

    for ((_, scale) in entrySequence(chart.scales)) {
      val scaleLabel = scale.options.scaleLabel
      if (!scale._isVisible() || scale.isHorizontal() || scaleLabel.display) {
        continue
      }

      val scaleLabelFontColor = helpers.valueOrDefault(scaleLabel.fontColor, defaults.global.defaultFontColor)
      val scaleLabelFont = helpers.options._parseFont(scaleLabel)
      val scaleLabelPadding = helpers.options.toPadding(scaleLabel.padding)

      val y = scale.top - scale.paddingTop - scaleLabelPadding.bottom
      val x: dynamic
      val align: CanvasTextAlign

      if (scale.position == "left") {
        x = (scale.right + scale._labelItems[0].x) / 2
        align = CanvasTextAlign.RIGHT
      } else {
        x = (scale.left + scale._labelItems[0].x) / 2
        align = CanvasTextAlign.LEFT
      }

      val ctx = scale.ctx.unsafeCast<CanvasRenderingContext2D>()
      ctx.save()
      ctx.textAlign = align
      ctx.textBaseline = CanvasTextBaseline.BOTTOM
      ctx.fillStyle = scaleLabelFontColor // render in correct colour
      ctx.font = scaleLabelFont.string
      ctx.fillText(scaleLabel.labelString, x, y)
      ctx.restore()
    }
  }

  val plugin: Chart.PluginServiceRegistrationOptions = new<dynamic> {
    afterDatasetsDraw = ::afterDatasetsDraw
  }.unsafeCast<Chart.PluginServiceRegistrationOptions>()
}

var Chart.ChartDataSets.measureType: MeasureType
  get() = asDynamic().measureType
  set(value) { asDynamic().measureType = value }


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