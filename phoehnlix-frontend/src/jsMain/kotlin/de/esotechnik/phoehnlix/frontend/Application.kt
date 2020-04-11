package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.api.model.Profile
import de.esotechnik.phoehnlix.frontend.dashboard.dashboardPage
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
import materialui.styles.muitheme.options.MuiThemeOptions
import materialui.styles.muitheme.options.mixins
import materialui.styles.muitheme.options.palette
import materialui.styles.palette.main
import materialui.styles.palette.options.background
import materialui.styles.palette.options.contrastText
import materialui.styles.palette.options.dark
import materialui.styles.palette.options.default
import materialui.styles.themeprovider.themeProvider
import materialui.styles.palette.options.main
import materialui.styles.palette.options.paper
import materialui.styles.palette.options.primary
import materialui.styles.palette.options.secondary
import materialui.styles.palette.options.text
import materialui.styles.palette.paper
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.setState

/**
 * @author Bernhard Frauendienst
 */
interface AppState : RState {
  var profile: Profile?
}

class Application : RComponent<RProps, AppState>() {

  override fun RBuilder.render() {
    cssBaseline {
      themeProvider(defaultTheme) {
        dashboardPage {
          attrs.profile = state.profile
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
