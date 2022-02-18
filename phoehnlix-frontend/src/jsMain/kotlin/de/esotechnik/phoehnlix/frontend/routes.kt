package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.api.model.toProfileDraft
import de.esotechnik.phoehnlix.frontend.dashboard.dashboardPage
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
          element = createElement {
            if (ctx.isLoggedIn) {
              Navigate { attrs.to = "/" }
            }
            LoginPage()
          }
        }
      }
      if (!ctx.isLoggedIn) {
        Route {
          attrs.path = "/*"
          attrs.element = createElement {
            Navigate { attrs.to = "/login" }
          }
        }
      }
      Route {
        attrs.path = "/myprofile"
        attrs.element = createElement {
          myProfilePage {
            attrs.profileDraft = ctx.currentProfileDraft ?: ctx.currentProfile?.toProfileDraft()
          }
        }
      }
      Route {
        attrs.path = "/dashboard"
        attrs.element = createElement {
          dashboardPage {
            attrs.profile = ctx.currentProfile
          }
        }
      }
    }
  }
}