package gapi

import gapi.auth2.GapiAuth2
import org.w3c.dom.Window
import org.w3c.dom.get

/**
 * @author Bernhard Frauendienst
 */

external interface LoadConfig {
  var callback: (() -> Unit)?
    get() = definedExternally
    set(value) = definedExternally
  var onerror: (() -> Unit)?
    get() = definedExternally
    set(value) = definedExternally
  var timeout: Int?
    get() = definedExternally
    set(value) = definedExternally
  var ontimeout: (() -> Unit)?
    get() = definedExternally
    set(value) = definedExternally
}

val Window.gapi: Gapi get() = this["gapi"].unsafeCast<Gapi>()

// not modelling this is as module/object because we lazy-load it
external interface Gapi {
  fun load(apiName: String, callback: () -> Unit)
  fun load(apiName: String, config: LoadConfig)
  // libraries that we load. Must be loaded to be availalbe
  val auth2: GapiAuth2
}