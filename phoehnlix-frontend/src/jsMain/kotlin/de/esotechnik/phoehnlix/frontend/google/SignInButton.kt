package de.esotechnik.phoehnlix.frontend.google

import de.esotechnik.phoehnlix.frontend.util.styleSets
import kotlinext.js.Object
import kotlinx.css.Color
import kotlinx.css.backgroundColor
import kotlinx.css.color
import kotlinx.css.margin
import kotlinx.css.marginLeft
import kotlinx.css.padding
import kotlinx.css.px
import kotlinx.html.BUTTON
import kotlinx.html.js.onClickFunction
import materialui.components.button.ButtonElementBuilder
import materialui.components.button.ButtonProps
import materialui.components.button.button
import materialui.components.button.enums.ButtonSize
import materialui.components.button.enums.ButtonStyle
import materialui.components.get
import materialui.styles.withStyles
import org.w3c.dom.events.Event
import react.RBuilder
import react.RHandler
import react.Props
import react.dom.attrs

/**
 * @author Bernhard Frauendienst
 */

interface SignInButtonProps : Props {
  var onClickFunction: ((Event) -> Unit)?
}

private val styledSignInButton = withStyles("google-signIn-button", {
  "root" {
    padding(horizontal = 8.px)
    backgroundColor = Color.white
  }
  "startIcon" {
    margin(left = 0.px, right = 24.px)
  }
}) { props: SignInButtonProps ->
  val root by props.styleSets
  val startIcon by props.styleSets
  button(
    ButtonStyle.root to root,
    ButtonStyle.startIcon to startIcon
  ) {
    attrs {
      startIcon {
        googleIcon()
      }
      size = ButtonSize.large
      props.onClickFunction?.let { onClickFunction = it }
    }
    +"Anmelden"
  }
}

fun RBuilder.googleSignInButton(handler: RHandler<SignInButtonProps>) = styledSignInButton(handler)