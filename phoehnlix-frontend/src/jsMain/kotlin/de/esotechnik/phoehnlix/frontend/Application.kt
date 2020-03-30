package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.apiservice.model.Profile
import de.esotechnik.phoehnlix.frontend.dashboard.dashboardPage
import de.esotechnik.phoehnlix.frontend.dashboard.measurementList
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.css.Color
import kotlinx.html.js.onClickFunction
import materialui.components.appbar.appBar
import materialui.components.appbar.enums.AppBarPosition.*
import materialui.components.button.enums.ButtonColor.inherit
import materialui.components.icon.icon
import materialui.components.iconbutton.iconButton
import materialui.components.toolbar.toolbar
import materialui.components.typography.enums.TypographyVariant.h6
import materialui.components.typography.typography
import materialui.styles.createMuiTheme
import materialui.styles.muitheme.MuiTheme
import materialui.styles.muitheme.options.palette
import materialui.styles.themeprovider.themeProvider
import materialui.styles.palette.options.main
import materialui.styles.palette.options.primary
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
    themeProvider(theme) {
      appBar {
        attrs.position = static
        toolbar {
          iconButton {
            attrs["edge"] = "start"
            attrs.color = inherit
            attrs["aria-label"] = "menu"
            icon {
              +"menu"
            }
          }
          typography {

            attrs.variant = h6

          }
          iconButton {
            icon {
              +"eye"
            }
            attrs {
              onClickFunction = { event ->
              }
            }
          }
        }
      }

      dashboardPage {
        attrs.profile = state.profile
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

    private val theme: MuiTheme = createMuiTheme {
      palette {
        primary {
          main = Color("#2196f3")
        }
      }
    }
  }
}
