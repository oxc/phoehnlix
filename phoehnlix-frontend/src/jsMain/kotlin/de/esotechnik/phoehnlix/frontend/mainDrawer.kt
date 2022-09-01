package de.esotechnik.phoehnlix.frontend

import mui.icons.material.Menu
import mui.material.AppBar
import mui.material.AppBarPosition
import mui.material.Drawer
import mui.material.IconButton
import mui.material.IconButtonColor
import mui.material.IconButtonEdge
import mui.material.List
import mui.material.Toolbar
import react.FC
import react.dom.aria.ariaLabel

/**
 * @author Bernhard Frauendienst <bernhard.frauendienst@markt.de>
 */
val MainDrawer = FC { props: MainMenuProps ->
  Drawer {
    List {

    }
  }
  AppBar {
    position = AppBarPosition.sticky
    Toolbar {
      IconButton {
        edge = IconButtonEdge.start
        color = IconButtonColor.inherit
        ariaLabel = "menu"
        Menu {}
      }
      +props.children
    }
  }
}
