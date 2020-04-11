package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.api.client.ApiClient
import io.ktor.client.engine.js.Js
import react.RBuilder
import react.RComponent
import react.RHandler
import react.RProviderProps
import react.createContext

/**
 * @author Bernhard Frauendienst
 */

val ApiContext = createContext<ApiClient>()

fun RBuilder.apiProvider(url: String, handler: RHandler<RProviderProps<ApiClient>>) {
  val client = ApiClient(Js, url, null)

  ApiContext.Provider(client, handler)
}

/**
 * Must set contextType = ApiContext
 */
val RComponent<*,*>.api get() = asDynamic().context.unsafeCast<ApiClient>()

inline fun <reified C : RComponent<*, *>> useApiContext() {
  C::class.js.asDynamic().contextType = ApiContext
}