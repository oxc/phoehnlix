import de.esotechnik.phoehnlix.frontend.Application
import react.dom.*
import kotlin.browser.document

/**
 * @author Bernhard Frauendienst
 */

fun main() {
  render(document.getElementById("root")) {
    child(Application::class) {}
  }
}