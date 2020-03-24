package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.apiservice.client.ApiClient
import de.esotechnik.phoehnlix.apiservice.model.Profile
import de.esotechnik.phoehnlix.apiservice.model.ProfileMeasurement
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.features.json.Json
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.css.Color
import materialui.styles.createMuiTheme
import materialui.styles.muitheme.MuiTheme
import materialui.styles.muitheme.options.palette
import materialui.styles.themeprovider.themeProvider
import materialui.styles.palette.options.main
import materialui.styles.palette.options.primary
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.h1
import react.setState
import kotlin.browser.document

/**
 * @author Bernhard Frauendienst
 */
interface AppState : RState {
  var profile: Profile?
  var measurements: List<ProfileMeasurement>
}

class Application : RComponent<RProps, AppState>() {

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

    //val chart = WeightChart(ctx, measurements, profile.targetWeight)
  }

  override fun RBuilder.render() {
      h1 {
        +"Phoehnlix"
      }
      measurementList {
        measurements = state.measurements.asReversed()
      }
  }

  override fun AppState.init() {
    measurements = listOf()

    val mainScope = MainScope()
    mainScope.launch {
      val profile = api.profile[1]()
      val measurements = api.profile[1].measurements().sortedBy { it.timestamp }
      setState {
        this.profile = profile
        this.measurements = measurements
      }
    }
  }

  companion object {
    private val theme: MuiTheme = createMuiTheme {
      palette {
        primary {
          main = Color("#2196f3")
        }
      }
    }
  }
}
