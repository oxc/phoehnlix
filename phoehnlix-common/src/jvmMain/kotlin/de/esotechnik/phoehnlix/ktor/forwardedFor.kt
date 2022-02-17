package de.esotechnik.phoehnlix.ktor

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.XForwardedHeaderSupport
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
  install(XForwardedHeaderSupport) {
    hostHeaders.clear()
    protoHeaders.clear()
    forHeaders.apply {
      clear()
      config.propertyOrNull("phoehnlix.connector.forwardedForHeader")?.getString()?.let {
        log.info("Setting XForwardedHeaderSupport#forHeaders to [$it]")
        add(it)
      }
    }
    httpsFlagHeaders.apply {
      clear()
      config.propertyOrNull("phoehnlix.connector.forwardedSSLHeader")?.getString()?.let {
        log.info("Setting XForwardedHeaderSupport#httpsFlagHeaders to [$it]")
        add(it)
      }
    }
  }
  attributes.put(attributeKey, true)
}