package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.api.google.SCOPE_FITNESS_BODY_WRITE
import de.esotechnik.phoehnlix.api.google.SCOPE_USERINFO_PROFILE
import de.esotechnik.phoehnlix.api.google.SCOPE_USER_BIRTHDAY_READ
import de.esotechnik.phoehnlix.frontend.dashboard.new
import de.esotechnik.phoehnlix.frontend.google.loadGoogleAuth2
import de.esotechnik.phoehnlix.frontend.google.authorize
import de.esotechnik.phoehnlix.frontend.google.googleSignInButton
import de.esotechnik.phoehnlix.frontend.util.styleSets
import gapi.auth2.GapiAuth2
import gapi.gapi
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.css.flexGrow
import materialui.components.grid.enums.GridAlignItems
import materialui.components.grid.enums.GridDirection
import materialui.components.grid.enums.GridJustify
import materialui.components.grid.enums.GridStyle
import materialui.components.grid.grid
import materialui.components.paper.paper
import materialui.lab.components.alert.alert
import materialui.lab.components.alert.enums.AlertSeverity
import materialui.lab.components.alerttitle.alertTitle
import materialui.lab.components.skeleton.enums.SkeletonVariant
import materialui.lab.components.skeleton.skeleton
import materialui.styles.withStyles
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.setState
import kotlin.browser.window

/**
 * @author Bernhard Frauendienst
 */

interface LoginPageState : RState {
  var auth2loaded: Boolean
  var error: String?
  var errorInfo: String?
}

class LoginPageComponent : RComponent<RProps, LoginPageState>() {
  companion object {
    init {
      usePhoehnlixContext<LoginPageComponent>()
    }
  }

  override fun RBuilder.render() {
    loadGoogleAuth2 {
      setState {
        auth2loaded = true
      }
    }
    logoMenu {
    }
    val fullPage by styleSets
    grid(GridStyle.container to fullPage) {
      attrs {
        container = true
        direction = GridDirection.column
        alignItems = GridAlignItems.center
        justify = GridJustify.center
      }
      grid {
        attrs {
          item = true
        }
        paper {
          if (state.auth2loaded) {
            googleSignInButton {
              attrs {
                onClickFunction = {
                  window.gapi.auth2.startLogin()
                }
              }
            }
          } else {
            skeleton {
              attrs {
                variant = SkeletonVariant.rect
                height = "40px"
                width = "140px"
              }
            }
          }
        }
      }
      state.error?.let { error ->
        grid {
          attrs {
            item = true
          }
          alert {
            attrs {
              severity = AlertSeverity.error
              onClose = {
                setState {
                  this.error = null
                  this.errorInfo = null
                }
              }
            }
            alertTitle { +"Fehler" }
            +error
            state.errorInfo?.let { errorInfo ->
              +" ($errorInfo)"
            }
          }
        }
      }
    }
  }

  private fun GapiAuth2.startLogin(): Unit = with(phoehnlix) {
    val mainScope = MainScope() + CoroutineName("Dashboard")
    mainScope.launch {
      val response = authorize(new {
        client_id = googleClientId
        scope = listOf(
          SCOPE_USERINFO_PROFILE,
          SCOPE_USER_BIRTHDAY_READ,
          SCOPE_FITNESS_BODY_WRITE
        ).joinToString(" ")
        response_type = "code"
      })
      if (response.error != null) {
        setState {
          error = response.error
          errorInfo = response.error_subtype
        }
        return@launch
      }
      val login = api.login.google(response.code!!)
      update(apiToken = login.apiToken, profile = login.profile, profileDraft = login.profileDraft)
    }
  }
}

private val styledComponent = withStyles(LoginPageComponent::class, {
  "fullPage" {
    flexGrow = 1.0
  }
})

fun RBuilder.loginPage() = styledComponent {}