package de.esotechnik.phoehnlix.frontend

import react.FC
import react.Props
import react.router.Navigate
import react.router.useLocation
import react.useContext

val IndexPage = FC<Props>("IndexPage") {
    val ctx = useContext(PhoehnlixContext)
    val location = useLocation()

    when {
        location.hash.startsWith("#!/") -> {
            Navigate { to = location.hash.substring(2) }
        }
        !ctx.isLoggedIn -> {
            Navigate { to = "/login" }
        }
        ctx.currentProfileDraft != null -> {
            Navigate { to = "/myprofile" }
        }
        else -> {
            Navigate { to = "/dashboard" }
        }
    }
}