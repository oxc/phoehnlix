package de.esotechnik.phoehnlix.frontend

import react.Props
import react.fc
import react.router.Navigate
import react.router.useLocation
import react.useContext

val IndexPage = fc<Props>("IndexPage") {
    val ctx = useContext(PhoehnlixContext)
    val location = useLocation()

    when {
        location.hash.startsWith("#!/") -> {
            Navigate { attrs.to = location.hash.substring(2) }
        }
        !ctx.isLoggedIn -> {
            Navigate { attrs.to = "/login" }
        }
        ctx.currentProfileDraft != null -> {
            Navigate { attrs.to = "/myprofile" }
        }
        else -> {
            Navigate { attrs.to = "/dashboard" }
        }
    }
}