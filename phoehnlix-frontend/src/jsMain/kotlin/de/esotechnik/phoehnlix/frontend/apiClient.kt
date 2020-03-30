package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.apiservice.client.ApiClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.features.json.Json
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.html.RP
import react.RBuilder
import react.RComponent
import react.RHandler
import react.RProps
import react.RProviderProps
import react.RState
import react.createContext
import react.createElement

/**
 * @author Bernhard Frauendienst <bernhard.frauendienst@markt.de>
 */

val ApiContext = createContext<ApiClient>()

fun RBuilder.apiProvider(url: String, handler: RHandler<RProviderProps<ApiClient>>) {
  val httpClient = HttpClient(Js) {
    Json {
      serializer = KotlinxSerializer()
    }
  }

  val client = ApiClient(httpClient, url)

  ApiContext.Provider(client, handler)
}

/**
 * Must set contextType = ApiContext
 */
val RComponent<*,*>.api get() = asDynamic().context.unsafeCast<ApiClient>()

inline fun <reified C : RComponent<*, *>> useApiContext() {
  C::class.js.asDynamic().contextType = ApiContext
}