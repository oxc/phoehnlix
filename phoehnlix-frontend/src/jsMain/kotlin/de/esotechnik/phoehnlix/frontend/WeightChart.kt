package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.externals.Chart
import org.w3c.dom.CanvasRenderingContext2D

/**
 * @author Bernhard Frauendienst <bernhard.frauendienst@markt.de>
 */
class WeightChart(
  val ctx: CanvasRenderingContext2D,
  val data: List<ProfileMeasurement>
) {
  val chart = Chart(ctx, buildOptions())

  private fun buildOptions(): Chart.ChartConfiguration {
    return Nothing
  }
}