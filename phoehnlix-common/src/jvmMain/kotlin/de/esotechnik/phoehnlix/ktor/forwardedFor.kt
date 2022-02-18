package de.esotechnik.phoehnlix.ktor

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.util.AttributeKey

private val attributeKey = AttributeKey<Boolean>("phoehnlixForwardedFor")

/**
 * @author Bernhard Frauendienst
 */
fun Application.setupForwardedFor() {
  if (attributes.getOrNull(attributeKey) == true) {
    return
  }

  val config = environment.config
  install(XForwardedHeaders) {
    hostHeaders.clear()
    protoHeaders.clear()
    forHeaders.apply {
      clear()
      config.propertyOrNull("phoehnlix.connector.forwardedForHeader")?.getString()?.let {
        this@setupForwardedFor.log.info("Setting XForwardedHeaderSupport#forHeaders to [$it]")
        add(it)
      }
    }
    httpsFlagHeaders.apply {
      clear()
      config.propertyOrNull("phoehnlix.connector.forwardedSSLHeader")?.getString()?.let {
        this@setupForwardedFor.log.info("Setting XForwardedHeaderSupport#httpsFlagHeaders to [$it]")
        add(it)
      }
    }
  }
  attributes.put(attributeKey, true)
}