package de.esotechnik.phoehnlix.frontend

import csstype.*
import mui.icons.material.Menu
import mui.icons.material.MenuBook
import mui.material.*
import mui.material.IconButtonColor.inherit
import mui.material.IconButtonEdge.start
import mui.material.styles.TypographyVariant.h6
import mui.material.styles.TypographyVariant.subtitle1
import mui.system.responsive
import mui.system.sx
import react.FC
import react.PropsWithChildren
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.h3
import react.useState

external interface MainMenuProps : PropsWithChildren

val BaseMainMenu = FC<MainMenuProps>("MainMenu") { props ->
  var drawerOpen by useState(false)
  AppBar {
    position = AppBarPosition.sticky
    Toolbar {
      IconButton {
        edge = start
        color = inherit
        ariaLabel = "menu"
        onClick = { drawerOpen = true }
        Menu {}
      }
      +props.children
    }
  }
  MainDrawer {
    open = drawerOpen
    onClose = { _, _ ->
      drawerOpen = false
    }
  }
}

interface TitleMenuProps : MainMenuProps {
  var title: String
  var subtitle: String?
}

val TitleMainMenu = FC<TitleMenuProps>("TitleMainMenu") { props ->
  BaseMainMenu {
    Grid {
      container = true
      direction = responsive(mui.material.GridDirection.column)
      wrap = GridWrap.nowrap
      sx {
        flexGrow = number(1.0)
        justifyContent = JustifyContent.center
      }
      Typography {
        component = h2
        variant = h6
        +props.title
      }
      props.subtitle?.let { subtitle ->
        Typography {
          component = h3
          variant = subtitle1
          +subtitle
        }
      }
    }
    +props.children
  }
}

val LogoMenu = FC<MainMenuProps> { props ->
    BaseMainMenu {
      Typography {
        component = h1
        sx {
          flexGrow = number(1.0)
          fontFamily = string("Days One, sans-serif")
          textAlign = TextAlign.center
          textDecoration = TextDecoration.overline
          textTransform = TextTransform.uppercase
        }
        variant = h6
        +"Phoehnlix"
      }

      +props.children
    }
  }
