package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.api.client.ApiClient
import de.esotechnik.phoehnlix.api.client.ProfileId
import de.esotechnik.phoehnlix.api.model.LoginResponse
import de.esotechnik.phoehnlix.api.model.PhoehnlixApiToken
import de.esotechnik.phoehnlix.api.model.Profile
import de.esotechnik.phoehnlix.api.model.ProfileDraft
import de.esotechnik.phoehnlix.frontend.util.attribute
import de.esotechnik.phoehnlix.frontend.util.getValue
import de.esotechnik.phoehnlix.frontend.util.setValue
import de.esotechnik.phoehnlix.frontend.util.styleSets
import io.ktor.client.engine.js.Js
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.css.CSSBuilder
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.backgroundColor
import kotlinx.css.body
import kotlinx.css.color
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.height
import kotlinx.css.html
import kotlinx.css.margin
import kotlinx.css.minHeight
import kotlinx.css.padding
import kotlinx.css.pct
import kotlinx.css.px
import kotlinx.css.vh
import kotlinx.css.width
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
import react.Fragment
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
  val currentProfileDraft: ProfileDraft?,
  val profileIsLoading: Boolean,
  private val updateState: (PhoehnlixState) -> Unit
) : Closeable {

  fun update(apiToken: PhoehnlixApiToken?) {
    storedApiAccessToken = apiToken
    updateState(copy(apiToken = apiToken))
  }
  fun update(profile: Profile?) = updateState(copy(currentProfile = profile, currentProfileDraft = null))
  fun update(apiToken: PhoehnlixApiToken?, profile: Profile?, profileDraft: ProfileDraft?) {
    storedApiAccessToken = apiToken
    updateState(copy(apiToken = apiToken, currentProfile = profile, currentProfileDraft = profileDraft))
  }

  val api = ApiClient(Js, apiUrl, apiToken)

  override fun close() {
    api.close()
  }
}

val PhoehnlixState.isLoggedIn get() = apiToken != null

class Application(props: AppProps) : RComponent<AppProps, AppState>(props) {
  override fun AppState.init(props: AppProps) {
    val apiToken = storedApiAccessToken
    val loadProfile = apiToken != null
    val state = PhoehnlixState(
      apiUrl = props.apiUrl,
      googleClientId = props.googleClientId,
      apiToken = apiToken,
      currentProfile = null,
      currentProfileDraft = null,
      profileIsLoading = loadProfile,
      updateState = { newState ->
        val oldState = state.phoehnlixState
        setState { phoehnlixState = newState }
        oldState.close()
      }
    )
    phoehnlixState = state

    if (loadProfile) with(state) {
      // load profile if we're already logged in
      val mainScope = MainScope() + CoroutineName("Application")
      mainScope.launch {
        val profile = api.profile[ProfileId.Me]()
        update(profile)
      }
    }
  }

  override fun RBuilder.render() {
    Fragment {
      cssBaseline { }
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
          default = Color("#fff")
        }
      }
      mixins {
        toolbar = CSSBuilder().apply {
          backgroundColor = Color("#fff")
          color = defaultTheme.palette.primary.main
        }
      }
    }
  }
}

private val flexRoot = withStyles("phoehnlix-flex-root", {
  global {
    html {
      display = Display.table
      height = 100.pct
    }
    body {
      display = Display.tableCell
    }
    "html, body" {
      width = 100.pct
      margin(0.px)
      padding(0.px)
    }
    "div#root" {
      minHeight = 100.pct
      display = Display.flex
      flexDirection = FlexDirection.column
    }
  }
}) { props: RProps ->
  Fragment {
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