package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.frontend.dashboard.DashboardPage
import react.*
import react.router.*
import react.router.dom.*

/**
 * @author Bernhard Frauendienst
 */

val routingRoot = fc<Props>("routingRoot") {
  val ctx = useContext(PhoehnlixContext)

  BrowserRouter {
    Routes {

      Route {
        attrs.path = "/"
        attrs.element = createElement(IndexPage)
      }
      Route {
        attrs {
          path = "/login"
          element = Fragment.create {
            if (ctx.isLoggedIn) {
              Navigate { to = "/" }
            }
            LoginPage()
          }
        }
      }
      if (!ctx.isLoggedIn) {
        Route {
          attrs.path = "/*"
          attrs.element = Fragment.create {
            Navigate { to = "/login" }
          }
        }
      }
      Route {
        attrs.path = "/myprofile"
        attrs.element = Fragment.create {
          /*
          myProfilePage.create {
            profileDraft = ctx.currentProfileDraft ?: ctx.currentProfile?.toProfileDraft()
          }
           */
        }
      }
      Route {
        attrs.path = "/dashboard"
        attrs.element = Fragment.create {
          DashboardPage {
            profile = ctx.currentProfile
          }
        }
      }
    }
  }
}