package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.api.model.ProfileDraft
import de.esotechnik.phoehnlix.api.model.toProfileDraft
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

      route<RProps>("/", exact = true) { props ->
        when {
          props.location.hash.startsWith("#!/") -> {
            redirect(to = props.location.hash.substring(2))
          }
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
      if (!ctx.isLoggedIn) {
        redirect(to = "/login")
      }
      route("/myprofile") {
        myProfilePage {
          attrs.profileDraft = ctx.currentProfileDraft ?:
            ctx.currentProfile?.toProfileDraft()
        }
      }
      route("/dashboard") {
        dashboardPage {
          attrs.profile = ctx.currentProfile
        }
      }
    }
  }
}