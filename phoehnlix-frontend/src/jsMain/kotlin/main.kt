import de.esotechnik.phoehnlix.frontend.Application
import react.dom.render
import kotlinx.browser.document
import react.create
import react.dom.client.createRoot

/**
 * @author Bernhard Frauendienst
 */

fun main() {
  val rootElement = document.getElementById("root")!!
  val apiUrl = rootElement.getAttribute("data-api-url")!!
  val googleClientId = rootElement.getAttribute("data-google-clientId")!!
  val root = createRoot(rootElement)
  root.render(Application.create {
    this.apiUrl = apiUrl
    this.googleClientId = googleClientId
  })
}