package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.apiservice.client.ApiClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.features.json.Json
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.JsonSerializer
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.json.serializersStore
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document

/**
 * @author Bernhard Frauendienst
 */
object Application {

  val httpClient = HttpClient(Js) {
    Json {
      serializer = KotlinxSerializer()
    }
  }

  val api = ApiClient(httpClient, "https://phoehnlix.obeliks.de/api/")

  fun renderGraph() = GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
    // Start coroutine UNDISPATCHED, so that the handler is called immediately. Otherwise the handler would be
    // called in the next event loop, which would prevent it to use some of the event object's functions.

    val canvas = document.getElementById("graph-canvas") as HTMLCanvasElement
    val ctx = canvas.getContext("2d") as CanvasRenderingContext2D

    val profile = api.profile[1]()
    val measurements = api.profile[1].measurements().sortedBy { it.timestamp }
    console.log("Loaded data: ", measurements)
    val chart = WeightChart(ctx, measurements, profile.targetWeight)
  }
}
