package de.esotechnik.phoehnlix.frontend

import Chart
import dateFns
import de.esotechnik.phoehnlix.apiservice.model.ProfileMeasurement
import org.w3c.dom.CanvasRenderingContext2D
import kotlin.js.Date

@JsModule("date-fns")
external val dateFns: dateFns

@JsModule("chartjs-adapter-date-fns")
external val chartsJsAdapterDateFns: dynamic

/**
 * @author Bernhard Frauendienst <bernhard.frauendienst@markt.de>
 */
class WeightChart(
  ctx: CanvasRenderingContext2D,
  val measurements: List<ProfileMeasurement>
) {
  // make sure adapter is loaded
  val adapter = chartsJsAdapterDateFns

  val chart = Chart(ctx, new {
    type = "line"
    data = new {
      val count = measurements.size
      val timestamps = arrayOfNulls<Date>(count)
      val weights = arrayOfNulls<Double>(count)
      val bodyFatPercent = arrayOfNulls<Double?>(count)
      val bodyWaterPercent = arrayOfNulls<Double?>(count)
      val muscleMassPercent = arrayOfNulls<Double?>(count)
      val bodyMassIndex = arrayOfNulls<Double?>(count)
      val metabolicRate = arrayOfNulls<Double?>(count)

      measurements.forEachIndexed { i, entry: ProfileMeasurement ->
        timestamps[i] = dateFns.parseISO(entry.timestamp)
        weights[i] = entry.weight
        bodyFatPercent[i] = entry.bodyFatPercent
        bodyWaterPercent[i] = entry.bodyWaterPercent
        muscleMassPercent[i] = entry.muscleMassPercent
        bodyMassIndex[i] = entry.bodyMassIndex
        metabolicRate[i] = entry.metabolicRate
      }
      labels = timestamps
      datasets = arrayOf(
        new {
          label = "Gewicht"
          data = weights
          yAxisID = "weight"
        },
        new {
          label = "Fett%"
          data = bodyFatPercent
          yAxisID = "percent"
        },
        new {
          label = "Wasser%"
          data = bodyWaterPercent
          yAxisID = "percent"
        },
        new {
          label = "Muskel%"
          data = muscleMassPercent
          yAxisID = "percent"
        },
        new {
          label = "BMI"
          data = bodyMassIndex
          yAxisID = "bmi"
        },
        new {
          label = "Kalorien"
          data = metabolicRate
          yAxisID = "calories"
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
          o.adapters.date.locale = date_fns.locale.de
        })
        yAxes = arrayOf(
          new {
            id = "weight"
            type = "linear"
            position = "left"
          }, new {
            id = "percent"
            type = "linear"
            position = "right"
            ticks = new {
              min = 0
              max = 100
            }
          }, new {
            id = "bmi"
            type = "linear"
            position = "right"
            ticks = new {
              suggestedMin = 0
              suggestedMax = 35
            }
          }, new {
            id = "calories"
            type = "linear"
            position = "right"
            ticks = new {
              suggestedMin = 3000
              suggestedMax = 6000
            }
          }
        )
      }
    }
  })
}

inline fun <T : Any> new(init: T.() -> Unit) = js("{}").unsafeCast<T>().apply(init)