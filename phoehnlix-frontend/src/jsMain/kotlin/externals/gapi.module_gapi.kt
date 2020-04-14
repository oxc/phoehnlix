package gapi

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

// not modelling this is as module/object because we lazy-load it
external interface Gapi {
  fun load(apiName: String, callback: () -> Unit)
  fun load(apiName: String, config: LoadConfig)
}