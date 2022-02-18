package de.esotechnik.phoehnlix.frontend.google

import gapi.auth2.AuthorizeConfig
import gapi.auth2.AuthorizeResponse
import gapi.auth2.GapiAuth2
import gapi.gapi
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLScriptElement
import react.FC
import react.Props
import react.useEffect
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

external interface GoogleAuth2LoaderProps : Props {
  var onAuthApiLoaded: (GapiAuth2) -> Unit
}

val GoogleAuth2Loader = FC<GoogleAuth2LoaderProps> { props ->
  useEffect {
    val script = document.createElement("script").unsafeCast<HTMLScriptElement>().apply {
      src = "https://apis.google.com/js/platform.js"
      onload = {
        val gapi = window.gapi
        gapi.load("auth2") {
          props.onAuthApiLoaded(gapi.auth2)
        }
      }
    }
    document.body!!.appendChild(script)
  }
}

suspend fun GapiAuth2.authorize(params: AuthorizeConfig): AuthorizeResponse =
  suspendCoroutine { cont ->
    authorize(params) { response ->
      cont.resume(response)
    }
  }