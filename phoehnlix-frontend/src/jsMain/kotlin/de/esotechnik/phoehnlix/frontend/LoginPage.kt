package de.esotechnik.phoehnlix.frontend

import csstype.AlignItems
import csstype.JustifyContent
import csstype.number
import de.esotechnik.phoehnlix.api.google.SCOPE_FITNESS_BODY_WRITE
import de.esotechnik.phoehnlix.api.google.SCOPE_USERINFO_PROFILE
import de.esotechnik.phoehnlix.api.google.SCOPE_USER_BIRTHDAY_READ
import de.esotechnik.phoehnlix.frontend.google.GoogleAuth2Loader
import de.esotechnik.phoehnlix.frontend.google.GoogleSignInButton
import de.esotechnik.phoehnlix.frontend.google.authorize
import emotion.react.css
import gapi.gapi
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.js.jso
import mui.material.Alert
import mui.material.AlertColor
import mui.material.AlertTitle
import mui.material.Grid
import mui.material.GridDirection
import mui.material.Paper
import mui.material.Skeleton
import mui.material.SkeletonVariant
import mui.system.responsive
import react.FC
import react.Props
import react.dom.events.MouseEventHandler
import react.useCallback
import react.useContext
import react.useState

/**
 * @author Bernhard Frauendienst
 */

val LoginPage = FC<Props>("LoginPageComponent") { props ->
  val context = useContext(PhoehnlixContext)

  var auth2loaded: Boolean by useState(false)
  var error: String? by useState(null)
  var errorInfo: String? by useState(null)

  val onGoogleSignInClick: MouseEventHandler<*> = useCallback(context) {
    with (context) {
      val mainScope = MainScope() + CoroutineName("Dashboard")
      mainScope.launch {
        val response = window.gapi.auth2.authorize(jso {
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

  GoogleAuth2Loader {
    onAuthApiLoaded = {
      auth2loaded = true
    }
  }
  LogoMenu {
  }
  Grid {
    container = true
    direction = responsive(GridDirection.column)
    css {
      flexGrow = number(1.0)
      alignItems = AlignItems.center
      justifyContent = JustifyContent.center
    }
    Grid {
      item = true
      Paper {
        if (auth2loaded) {
          GoogleSignInButton {
            onClickFunction = onGoogleSignInClick
          }
        } else {
          Skeleton {
            variant = SkeletonVariant.rectangular
            height = "40px"
            width = "140px"
          }
        }
      }
    }
    error?.let { errorMessage ->
      Grid {
        item = true
        Alert {
          severity = AlertColor.error
          onClose = {
              error = null
              errorInfo = null
          }
          AlertTitle { +"Fehler" }
          +errorMessage
          errorInfo?.let { errorInfo ->
            +" ($errorInfo)"
          }
        }
      }
    }
  }
}