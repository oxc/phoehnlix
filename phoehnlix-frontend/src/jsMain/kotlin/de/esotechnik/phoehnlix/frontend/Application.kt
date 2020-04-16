package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.api.client.ApiClient
import de.esotechnik.phoehnlix.api.model.PhoehnlixApiToken
import de.esotechnik.phoehnlix.api.model.Profile
import de.esotechnik.phoehnlix.frontend.util.attribute
import de.esotechnik.phoehnlix.frontend.util.getValue
import de.esotechnik.phoehnlix.frontend.util.setValue
import de.esotechnik.phoehnlix.frontend.util.styleSets
import io.ktor.client.engine.js.Js
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.css.CSSBuilder
import kotlinx.css.Color
import kotlinx.css.backgroundColor
import kotlinx.css.color
import kotlinx.css.minHeight
import kotlinx.css.vh
import materialui.components.cssbaseline.cssBaseline
import materialui.components.grid.enums.GridDirection
import materialui.components.grid.enums.GridStyle
import materialui.components.grid.grid
import materialui.styles.createMuiTheme
import materialui.styles.mixins.options.toolbar
import materialui.styles.muitheme.options.mixins
import materialui.styles.muitheme.options.palette
import materialui.styles.palette.main
import materialui.styles.palette.options.background
import materialui.styles.palette.options.default
import materialui.styles.palette.options.main
import materialui.styles.palette.options.paper
import materialui.styles.palette.options.primary
import materialui.styles.palette.options.text
import materialui.styles.palette.paper
import materialui.styles.themeprovider.themeProvider
import materialui.styles.withStyles
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.createContext
import react.setState
import kotlin.browser.localStorage

/**
 * @author Bernhard Frauendienst
 */
interface AppProps : RProps {
  var apiUrl: String
  var googleClientId: String
}

interface AppState : RState {
  var phoehnlixState: PhoehnlixState
  var apiAccessToken: PhoehnlixApiToken?
  var profile: Profile?
}

private var storedApiAccessToken by localStorage.attribute(PhoehnlixApiToken.serializer())

data class PhoehnlixState(
  val apiUrl: String,
  val googleClientId: String,
  val apiToken: PhoehnlixApiToken?,
  val currentProfile: Profile?,
  private val updateState: (PhoehnlixState) -> Unit
) {
  fun updateApiToken(token: PhoehnlixApiToken?) {
    storedApiAccessToken = token
    updateState(copy(apiToken = token))
  }
  fun updateProfile(profile: Profile?) = updateState(copy(currentProfile = profile))

  val api = ApiClient(Js, apiUrl, apiToken)
}

class Application(props: AppProps) : RComponent<AppProps, AppState>(props) {
  override fun AppState.init(props: AppProps) {
    val state = PhoehnlixState(
      apiUrl = props.apiUrl,
      googleClientId = props.googleClientId,
      apiToken = storedApiAccessToken,
      currentProfile = null,
      updateState = { newState -> setState { phoehnlixState = newState } }
    )
    phoehnlixState = state

    with (state) {
      val mainScope = MainScope() + CoroutineName("Application")
      mainScope.launch {
        val profile = api.profile[1]()
        updateProfile(profile)
      }
    }
  }

  override fun RBuilder.render() {
    cssBaseline {
      themeProvider(defaultTheme) {
        PhoehnlixContext.Provider(state.phoehnlixState) {
          flexRoot {
            routingRoot {}
          }
        }
      }
    }
  }

  companion object {
    val defaultTheme = createMuiTheme {
      palette {
        text {
          primary = Color("#757575")

        }
        primary {
          main = Color("#b52319")
        }
      }
    }

    val whiteToolbarTheme = createMuiTheme {
      palette {
        background {
          default = paper
        }
      }
      mixins {
        toolbar = CSSBuilder().apply {
          backgroundColor = defaultTheme.palette.background.paper
          color = defaultTheme.palette.primary.main
        }
      }
    }
  }
}

private val flexRoot = withStyles("phoehnlix-flex-root", {
  "root" {
    minHeight = 100.vh
  }
}) { props: RProps ->
  val root by props.styleSets
  grid(GridStyle.container to root) {
    attrs {
      direction = GridDirection.column
      container = true
    }
    props.children()
  }
}

val PhoehnlixContext = createContext<PhoehnlixState>()

/**
 * Must set contextType = PhoehnlixContext
 */
val RComponent<*,*>.phoehnlix get() = asDynamic().context.unsafeCast<PhoehnlixState>()

inline fun <reified C : RComponent<*, *>> usePhoehnlixContext() {
  C::class.js.asDynamic().contextType = PhoehnlixContext
}