import de.esotechnik.phoehnlix.frontend.Application
import react.dom.render
import kotlin.browser.document

/**
 * @author Bernhard Frauendienst
 */

fun main() {
  val root = document.getElementById("root")!!
  val apiUrl = root.getAttribute("data-api-url")!!
  val googleClientId = root.getAttribute("data-google-clientId")!!
  render(root) {
    child(Application::class) {
      attrs.apiUrl = apiUrl
      attrs.googleClientId = googleClientId
    }
  }
}