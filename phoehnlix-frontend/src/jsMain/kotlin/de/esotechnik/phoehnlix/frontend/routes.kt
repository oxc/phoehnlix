package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.frontend.dashboard.dashboardPage
import react.RProps
import react.rFunction
import react.router.dom.browserRouter
import react.router.dom.redirect
import react.router.dom.route
import react.router.dom.switch
import react.useContext

/**
 * @author Bernhard Frauendienst
 */

val routingRoot = rFunction<RProps>("routingRoot") {
  val ctx = useContext(PhoehnlixContext)

  browserRouter {
    switch {
      route("/", exact = true) {
        when {
          !ctx.isLoggedIn -> {
            redirect(to = "/login")
          }
          ctx.currentProfileDraft != null -> {
            redirect(to = "/myprofile")
          }
          else -> {
            redirect(to = "/dashboard")
          }
        }
      }
      route("/login") {
        if (ctx.isLoggedIn) {
          redirect(to = "/")
        }
        loginPage()
      }
      route("/dashboard") {
        dashboardPage {
          attrs.profile = ctx.currentProfile
        }
      }
    }
  }
}