package de.esotechnik.phoehnlix.frontend.google

import mui.material.Button
import mui.material.Size
import react.*
import react.dom.events.MouseEventHandler

external interface SignInButtonProps : Props {
  var onClickFunction: MouseEventHandler<*>?
}

val GoogleSignInButton = FC<SignInButtonProps> { props ->
  Button {
    startIcon = googleIcon.create()
    size = Size.large
    props.onClickFunction?.let { onClick = it }

    +"Anmelden"
  }
}