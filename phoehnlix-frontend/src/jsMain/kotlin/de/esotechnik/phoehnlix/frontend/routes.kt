package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.frontend.dashboard.dashboardPage
import materialui.components.typography.typography
import react.RProps
import react.rFunction
import react.useContext

/**
 * @author Bernhard Frauendienst
 */

val routingRoot = rFunction<RProps>("routingRoot") {
  val ctx = useContext(PhoehnlixContext)

  if (ctx.apiToken == null) {
    loginPage()
  } else {
    dashboardPage {
      attrs.profile = ctx.currentProfile
    }
  }

}