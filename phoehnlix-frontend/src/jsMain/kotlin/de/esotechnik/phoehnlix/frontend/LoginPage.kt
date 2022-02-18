package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.api.google.SCOPE_FITNESS_BODY_WRITE
import de.esotechnik.phoehnlix.api.google.SCOPE_USERINFO_PROFILE
import de.esotechnik.phoehnlix.api.google.SCOPE_USER_BIRTHDAY_READ
import de.esotechnik.phoehnlix.frontend.dashboard.new
import de.esotechnik.phoehnlix.frontend.google.loadGoogleAuth2
import de.esotechnik.phoehnlix.frontend.google.authorize
import de.esotechnik.phoehnlix.frontend.google.googleSignInButton
import de.esotechnik.phoehnlix.frontend.util.styleSets
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
import react.dom.attrs
import kotlinx.browser.window
import org.w3c.dom.events.Event
import react.*

/**
 * @author Bernhard Frauendienst
 */

val LoginPage = withStyles(fc("LoginPageComponent") { props ->
  val context = useContext(PhoehnlixContext)

  var auth2loaded: Boolean by useState(false)
  var error: String? by useState(null)
  var errorInfo: String? by useState(null)

  val onGoogleSignInClick: (Event) -> Unit = useCallback(context) {
    with (context) {
      val mainScope = MainScope() + CoroutineName("Dashboard")
      mainScope.launch {
        val response = window.gapi.auth2.authorize(new {
          client_id = googleClientId
          scope = listOf(
            SCOPE_USERINFO_PROFILE,
            SCOPE_USER_BIRTHDAY_READ,
            SCOPE_FITNESS_BODY_WRITE
          ).joinToString(" ")
          response_type = "code"
        })
        if (response.error != null) {
          error = response.error
          errorInfo = response.error_subtype
          return@launch
        }
        val login = api.login.google(response.code!!)
        update(apiToken = login.apiToken, profile = login.profile, profileDraft = login.profileDraft)
      }
    }
  }

  loadGoogleAuth2 {
    auth2loaded = true
  }
  logoMenu {
  }
  val fullPage by props.styleSets
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
        if (auth2loaded) {
          googleSignInButton {
            attrs {
              onClickFunction = onGoogleSignInClick
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
    error?.let { errorMessage ->
      grid {
        attrs {
          item = true
        }
        alert {
          attrs {
            severity = AlertSeverity.error
            onClose = {
                error = null
                errorInfo = null
            }
          }
          alertTitle { +"Fehler" }
          +errorMessage
          errorInfo?.let { errorInfo ->
            +" ($errorInfo)"
          }
        }
      }
    }
  }
}, {
  "fullPage" {
    flexGrow = 1.0
  }
})