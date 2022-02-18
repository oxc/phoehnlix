package de.esotechnik.phoehnlix.frontend.dashboard

import Chart
import date_fns.addSeconds
import date_fns.differenceInSeconds
import date_fns.isBefore
import date_fns.locale.de
import de.esotechnik.phoehnlix.api.model.MeasureType
import de.esotechnik.phoehnlix.api.model.MeasureType.MetabolicRate
import de.esotechnik.phoehnlix.api.model.MeasureType.Weight
import de.esotechnik.phoehnlix.api.model.MeasureType.values
import de.esotechnik.phoehnlix.api.model.ProfileMeasurement
import de.esotechnik.phoehnlix.api.model.get
import de.esotechnik.phoehnlix.frontend.color
import de.esotechnik.phoehnlix.frontend.parseTimestamp
import de.esotechnik.phoehnlix.frontend.title
import de.esotechnik.phoehnlix.frontend.util.entrySequence
import de.esotechnik.phoehnlix.frontend.util.theme
import de.esotechnik.phoehnlix.util.roundToDigits
import kotlinx.js.jso
import mui.material.styles.Theme
import mui.material.styles.useTheme
import org.w3c.dom.BOTTOM
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.CanvasTextAlign
import org.w3c.dom.CanvasTextBaseline
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.LEFT
import org.w3c.dom.RIGHT
import react.FC
import react.PropsWithChildren
import react.RefObject
import react.dom.html.ReactHTML.canvas
import react.dom.html.ReactHTML.div
import react.useEffect
import react.useEffectOnce
import react.useRef
import react.useState
import kotlin.js.Date
import kotlin.math.ceil
import kotlin.math.floor

@JsModule("chartjs-adapter-date-fns")
external val chartsJsAdapterDateFns: dynamic

@JsModule("chartjs-plugin-downsample")
external val chartjsPluginDownsample: dynamic

external interface MeasurementChartProps : PropsWithChildren {
  var skipUpdate: Boolean
  var measurements: List<ChartMeasurement>
  var measureTypes: List<MeasureType>
  var visibleMeasureTypes: Set<MeasureType>
  var showDatesUntil: Date?
  var targetWeight: Double?
  var targetDatapointCount: Int
  var downsampleMethod: DownsampleMethod
}

private val MEASURE_TYPES = values().toList()
private val MIN_WEIGHT_RANGE = 4.5

// make sure dependencies are loaded
@Suppress("unused")
private val adapter = chartsJsAdapterDateFns
@Suppress("unused")
private val downsamplePlugin = chartjsPluginDownsample


fun RefObject<HTMLCanvasElement>.get2DContext() = this.current!!.getContext("2d") as CanvasRenderingContext2D

/**
 * @author Bernhard Frauendienst
 */
val MeasurementChart = FC<MeasurementChartProps> { props ->
  val canvasRef = useRef<HTMLCanvasElement>()

  var chartInstance: Chart? by useState()

  val theme = useTheme<Theme>()

  useEffect(theme) {
    Chart.defaults.global.asDynamic().defaultFontFamily = theme.typography.fontFamily
    Chart.defaults.global.asDynamic().defaultFontColor = theme.palette.text.primary
  }
  console.log("Rendering chart component")

  fun destroyChart() {
    console.log("Destroying chart...")
    chartInstance?.destroy()
    chartInstance = null
  }

  fun createChart() {
    console.log("Creating chart...")
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
      jso<Chart.ChartDataSets> {
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
    var weightScaleMin: Double? = undefined
    var weightScaleMax: Double? = undefined
    if (Weight in measureTypes) {
      // make sure we have at least 5kg
      val weights = entries.mapTo(mutableListOf()) { it[Weight]!! }
      props.targetWeight?.let { weights.add(it) }
      val min = weights.minOrNull()!!
      val max = weights.maxOrNull()!!
      if (max-min < MIN_WEIGHT_RANGE) {
        val halfRange = MIN_WEIGHT_RANGE / 2.0
        val avg = weights.average()
        weightScaleMin = floor((avg - halfRange).coerceAtLeast(max - MIN_WEIGHT_RANGE))
        weightScaleMax = ceil((avg + halfRange).coerceAtMost(min + MIN_WEIGHT_RANGE))
      }

      props.targetWeight?.let { targetWeight ->
        val targetWeights = emptyArray<Chart.ChartPoint>()
        targetWeights.add(targetWeight, entries.first().timestamp)
        targetWeights.add(targetWeight, props.showDatesUntil ?: entries.last().timestamp)
        datasetConfigs.add(jso {
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

    chartInstance = Chart(canvasRef.get2DContext(), jso {
      type = "line"
      data = jso {
        datasets = datasetConfigs.toTypedArray()
      }
      options = jso {
        layout = jso {
          padding = jso<Chart.ChartLayoutPaddingObject> {
            top = 30
          }
        }
        scales = jso<Chart.ChartScales<*>> {
          xAxes = arrayOf(jso<Chart.ChartXAxe> {
            type = "time"
            time = jso {
              minUnit = "day"
            }
            ticks = jso {
              max = props.showDatesUntil.asDynamic()
            }
          }.also { o: dynamic ->
            o.adapters = jso()
            o.adapters.date = jso()
            o.adapters.date.locale = de
          })
          yAxes = arrayOf(
            jso {
              id = "weight"
              type = "linear"
              position = "left"
              display = "auto"
              scaleLabel = jso {
                labelString = "kg"
              }
              ticks = jso {
                suggestedMin = weightScaleMin
                suggestedMax = weightScaleMax
              }
            }, jso {
              id = "percent"
              type = "linear"
              position = "right"
              display = "auto"
              scaleLabel = jso {
                labelString = "%"
              }
              ticks = jso {
                suggestedMin = 10
                suggestedMax = 60
              }
              gridLines = jso {
                display = false
              }
            }, jso {
              id = "calories"
              type = "linear"
              position = "right"
              display = "auto"
              scaleLabel = jso {
                labelString = "kcal"
              }
              ticks = jso {
                suggestedMin = 1500
                suggestedMax = 3000
              }
              gridLines = jso {
                drawBorder = false
                drawOnChartArea = false
              }
            }
          )
        }
        legend = jso {
          display = false
        }
        if (props.downsampleMethod == DownsampleMethod.LTTB) {
          asDynamic().downsample = jso<dynamic> {
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

  // re-create the chart for any of those props
  useEffect(props.downsampleMethod, props.measureTypes, props.targetDatapointCount,
    props.measurements, props.targetWeight) {
    if (props.skipUpdate) return@useEffect
    destroyChart()
    createChart()
  }

  // these props can be applied in-place
  useEffect(props.visibleMeasureTypes) {
    val chart = chartInstance ?: return@useEffect
    chart.data.datasets?.forEach { dataset ->
      dataset.hidden = dataset.measureType !in props.visibleMeasureTypes
    }
    chart.update(jso { duration = 0 })
  }

  useEffect(*arrayOf()) {
    cleanup {
      destroyChart()
    }
  }

  div {
    canvas {
      id = "chart-canvas"
      ref = canvasRef
    }
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

  val plugin: Chart.PluginServiceRegistrationOptions = jso<dynamic> {
    afterDatasetsDraw = ::afterDatasetsDraw
  }.unsafeCast<Chart.PluginServiceRegistrationOptions>()
}

var Chart.ChartDataSets.measureType: MeasureType
  get() = asDynamic().measureType
  set(value) { asDynamic().measureType = value }


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
  if (value != null) asDynamic().push(jso<Chart.ChartPoint> {
    y = value
    x = timestamp // would use t, but does not work with downsample plugin
  })
}

fun <T : Any> Iterable<T>.averageNotNullBy(selector: (T) -> Double?) = mapNotNull(selector).average().takeUnless { it.isNaN() }?.roundToDigits(1)