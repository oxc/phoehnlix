package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.api.model.toProfileDraft
import de.esotechnik.phoehnlix.frontend.dashboard.DashboardPage
import react.FC
import react.Fragment
import react.Props
import react.create
import react.createElement
import react.router.Navigate
import react.router.Route
import react.router.Routes
import react.router.dom.BrowserRouter
import react.useContext

/**
 * @author Bernhard Frauendienst
 */

val routingRoot = FC<Props>("routingRoot") {
  val ctx = useContext(PhoehnlixContext)

  BrowserRouter {
    Routes {

      Route {
        path = "/"
        element = createElement(IndexPage)
      }
      Route {
        path = "/login"
        element = Fragment.create {
          if (ctx.isLoggedIn) {
            Navigate { to = "/" }
          }
          LoginPage()
        }
      }
      if (!ctx.isLoggedIn) {
        Route {
          path = "/*"
          element = Fragment.create {
            Navigate { to = "/login" }
          }
        }
      }
      Route {
        path = "/myprofile"
        element = Fragment.create {
          MyProfilePage {
            profileDraft = ctx.currentProfileDraft ?: ctx.currentProfile?.toProfileDraft()
          }
        }
      }
      Route {
        path = "/dashboard"
        element = Fragment.create {
          DashboardPage {
            profile = ctx.currentProfile
          }
        }
      }
    }
  }
}