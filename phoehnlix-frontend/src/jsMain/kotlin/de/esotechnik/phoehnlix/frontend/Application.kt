package de.esotechnik.phoehnlix.frontend

import csstype.Color
import csstype.Display
import csstype.FlexDirection
import csstype.Margin
import csstype.Padding
import csstype.PropertiesBuilder
import csstype.pct
import csstype.px
import de.esotechnik.phoehnlix.api.client.ApiClient
import de.esotechnik.phoehnlix.api.client.ProfileId
import de.esotechnik.phoehnlix.api.model.PhoehnlixApiToken
import de.esotechnik.phoehnlix.api.model.Profile
import de.esotechnik.phoehnlix.api.model.ProfileDraft
import de.esotechnik.phoehnlix.frontend.util.attribute
import de.esotechnik.phoehnlix.frontend.util.getValue
import de.esotechnik.phoehnlix.frontend.util.setValue
import emotion.react.Global
import io.ktor.client.engine.js.*
import io.ktor.utils.io.core.*
import kotlinx.browser.localStorage
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.js.jso
import mui.material.CssBaseline
import mui.material.styles.ThemeProvider
import mui.material.styles.createTheme
import react.FC
import react.Props
import react.createContext
import react.useEffect
import react.useState

external interface AppProps : Props {
  var apiUrl: String
  var googleClientId: String
}

private var storedApiAccessToken by localStorage.attribute(PhoehnlixApiToken.serializer())

data class PhoehnlixState(
  val apiUrl: String,
  val googleClientId: String,
  val apiToken: PhoehnlixApiToken? = null,
  val currentProfile: Profile? = null,
  val currentProfileDraft: ProfileDraft? = null,
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

  fun logout() {
    if (isLoggedIn) {
      update(apiToken = null)
    }
  }
}

val PhoehnlixState.isLoggedIn get() = apiToken != null

val defaultTheme = createTheme(
  jso {
    palette = jso {
      text = jso {
        primary = Color("#757575")
      }
      primary = jso {
        main = Color("#b52319")
      }
    }
  }
)

val whiteToolbarTheme = createTheme(
  jso {
    palette = jso {
      background = jso {
        default = "#fff"
      }
    }
    mixins = jso<dynamic> {
      toolbar = jso {
        backgroundColor = Color("#fff")
        color = defaultTheme.palette.primary.main
      }
    }
  }
)

val Application = FC<AppProps> { props ->
  val apiToken = storedApiAccessToken
  val loadProfile = apiToken != null

  var stateInitialized by useState(false)
  var phoehnlixState by useState(PhoehnlixState(
    apiUrl = props.apiUrl,
    googleClientId = props.googleClientId,
    profileIsLoading = false,
    updateState = {}
  ))

  useEffect {
    if (stateInitialized) return@useEffect
    stateInitialized = true
    phoehnlixState = PhoehnlixState(
      apiUrl = props.apiUrl,
      googleClientId = props.googleClientId,
      apiToken = apiToken,
      currentProfile = null,
      currentProfileDraft = null,
      profileIsLoading = loadProfile,
      updateState = { newState ->
        val oldState = phoehnlixState
        phoehnlixState = newState
        oldState.close()
      }
    )
  }

  useEffect(stateInitialized) {
    if (stateInitialized && loadProfile) with(phoehnlixState) {
      // load profile if we're already logged in
      val mainScope = MainScope() + CoroutineName("Application")
      mainScope.launch {
        val profile = api.profile[ProfileId.Me]()
        update(profile)
      }
    }
  }

  CssBaseline {}
  ThemeProvider {
    theme = defaultTheme
    PhoehnlixContext.Provider(phoehnlixState) {
      Global {
        styles = jso<PropertiesBuilder>().apply {
          "html" {
            display = Display.table
            height = 100.pct
          }
          "body" {
            display = Display.tableCell
          }
          "html, body" {
            width = 100.pct
            margin = Margin(0.px, 0.px)
            padding = Padding(0.px, 0.px)
          }
          "div#root" {
            minHeight = 100.pct
            display = Display.flex
            flexDirection = FlexDirection.column
          }
        }
      }
      routingRoot {}
    }
  }
}

val PhoehnlixContext = createContext<PhoehnlixState>()