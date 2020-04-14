package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.api.google.SCOPE_FITNESS_BODY_WRITE
import de.esotechnik.phoehnlix.api.google.SCOPE_USERINFO_PROFILE
import de.esotechnik.phoehnlix.api.google.SCOPE_USER_BIRTHDAY_READ
import de.esotechnik.phoehnlix.api.model.PhoehnlixApiToken
import de.esotechnik.phoehnlix.frontend.dashboard.new
import de.esotechnik.phoehnlix.frontend.google.loadGoogleAuth2
import de.esotechnik.phoehnlix.frontend.google.authorize
import gapi.auth2.AuthorizeConfig
import gapi.auth2.GapiAuth2
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import react.RBuilder
import react.RComponent
import react.RHandler
import react.RProps
import react.RState

/**
 * @author Bernhard Frauendienst
 */

interface LoginPageProps : RProps {
  var googleClientId: String
  var onAccessTokenReceived: (PhoehnlixApiToken) -> Unit
}

class LoginPageComponent(props: LoginPageProps) : RComponent<LoginPageProps, RState>(props) {
  companion object {
    init {
      useApiContext<LoginPageComponent>()
    }
  }

  override fun RBuilder.render() {
    loadGoogleAuth2 {
      startLogin()
    }
  }

  private fun GapiAuth2.startLogin() {
    val mainScope = MainScope() + CoroutineName("Dashboard")
    mainScope.launch {
      val response = authorize(new {
        client_id = props.googleClientId
        scope = listOf(
          SCOPE_USERINFO_PROFILE,
          SCOPE_USER_BIRTHDAY_READ,
          SCOPE_FITNESS_BODY_WRITE
        ).joinToString(" ")
        response_type = "code"
      })
      val accessToken = api.login.google(response.code!!)
      props.onAccessTokenReceived(accessToken)
    }
  }
}

fun RBuilder.loginPage(handler: RHandler<LoginPageProps>) {
  child(LoginPageComponent::class, handler)
}