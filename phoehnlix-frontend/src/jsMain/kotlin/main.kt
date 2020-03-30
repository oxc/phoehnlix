import de.esotechnik.phoehnlix.frontend.Application
import de.esotechnik.phoehnlix.frontend.apiProvider
import react.dom.render
import kotlin.browser.document

/**
 * @author Bernhard Frauendienst
 */

fun main() {
  val root = document.getElementById("root")!!
  val apiUrl = root.getAttribute("data-api-url")!!
  render(root) {
    apiProvider(apiUrl) {
      child(Application::class) {}
    }
  }
}