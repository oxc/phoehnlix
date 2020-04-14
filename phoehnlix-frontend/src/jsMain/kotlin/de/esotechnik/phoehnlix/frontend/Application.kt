package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.api.model.PhoehnlixApiToken
import de.esotechnik.phoehnlix.api.model.Profile
import de.esotechnik.phoehnlix.frontend.dashboard.dashboardPage
import de.esotechnik.phoehnlix.frontend.util.attribute
import de.esotechnik.phoehnlix.frontend.util.getValue
import de.esotechnik.phoehnlix.frontend.util.setValue
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.css.CSSBuilder
import kotlinx.css.Color
import kotlinx.css.backgroundColor
import kotlinx.css.color
import materialui.components.cssbaseline.cssBaseline
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
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.setState
import kotlin.browser.localStorage

/**
 * @author Bernhard Frauendienst
 */
interface AppProps : RProps {
  var googleClientId: String
}

interface AppState : RState {
  var apiAccessToken: PhoehnlixApiToken?
  var profile: Profile?
}

private val KEY_API_TOKEN = "apiAccessToken"

var storedApiAccessToken by localStorage.attribute(PhoehnlixApiToken.serializer())

class Application(props: AppProps) : RComponent<AppProps, AppState>(props) {
  override fun AppState.init(props: AppProps) {
    apiAccessToken = storedApiAccessToken
  }

  override fun RBuilder.render() {
    cssBaseline {
      themeProvider(defaultTheme) {
        if (state.apiAccessToken == null) {
          loginPage {
            attrs {
              googleClientId = props.googleClientId
              onAccessTokenReceived = { token ->
                storedApiAccessToken = token
                setState {
                  apiAccessToken = token
                }
              }
            }
          }
        } else {
          dashboardPage {
            attrs.profile = state.profile
          }
        }
      }
    }
  }

  override fun AppState.init() {
    val mainScope = MainScope() + CoroutineName("Application")
    mainScope.launch {
      val profile = api.profile[1]()
      setState {
        this.profile = profile
      }
    }
  }

  companion object {
    init {
      useApiContext<Application>()
    }

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
