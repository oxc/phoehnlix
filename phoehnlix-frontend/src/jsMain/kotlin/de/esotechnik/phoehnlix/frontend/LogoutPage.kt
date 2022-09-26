package de.esotechnik.phoehnlix.frontend

import csstype.AlignItems
import csstype.JustifyContent
import csstype.number
import emotion.react.css
import kotlinx.browser.window
import kotlinx.js.timers.setTimeout
import mui.material.Grid
import mui.material.GridDirection
import mui.material.Typography
import mui.system.responsive
import react.FC
import react.Props
import react.router.Navigate
import react.useContext
import react.useEffect
import react.useState
import kotlin.time.Duration.Companion.seconds

/**
 * @author Bernhard Frauendienst
 */

val LogoutPage = FC<Props> { props ->
  val context = useContext(PhoehnlixContext)
  var redirectToHome by useState(false)

  useEffect(context) {
    if (context.isLoggedIn) {
      context.logout()
    }
  }

  useEffect {
    setTimeout(3.seconds) {
      redirectToHome = true
    }
  }

  if (redirectToHome) {
    Navigate {
      to = "/"
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
      Typography {
        +"You are logged out."
      }
    }
  }
}