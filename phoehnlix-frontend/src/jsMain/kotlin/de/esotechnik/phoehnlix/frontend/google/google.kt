package de.esotechnik.phoehnlix.frontend.google

import gapi.Gapi
import gapi.auth2.AuthorizeConfig
import gapi.auth2.AuthorizeResponse
import gapi.auth2.GapiAuth2
import gapi.gapi
import kotlinx.coroutines.yield
import org.w3c.dom.HTMLScriptElement
import org.w3c.dom.get
import react.RBuilder
import react.RComponent
import react.Props
import react.State
import kotlinx.browser.document
import kotlinx.browser.window
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * @author Bernhard Frauendienst
 */

interface GoogleAuth2LoaderProps : Props {
  var onAuthApiLoaded: GapiAuth2.() -> Unit
}

class GoogleAuth2Loader : RComponent<GoogleAuth2LoaderProps, State>() {
  override fun componentDidMount() {
    val script = document.createElement("script").unsafeCast<HTMLScriptElement>().apply {
      src = "https://apis.google.com/js/platform.js"
      onload = {
        val gapi = window.gapi
        gapi.load("auth2") {
          props.onAuthApiLoaded.invoke(gapi.auth2)
        }
      }
    }
    document.body!!.appendChild(script)
  }

  override fun RBuilder.render() {
  }
}

fun RBuilder.loadGoogleAuth2(callback: GapiAuth2.() -> Unit) {
  child(GoogleAuth2Loader::class) {
    attrs.onAuthApiLoaded = callback
  }
}

suspend fun GapiAuth2.authorize(params: AuthorizeConfig): AuthorizeResponse =
  suspendCoroutine { cont ->
    authorize(params) { response ->
      cont.resume(response)
    }
  }