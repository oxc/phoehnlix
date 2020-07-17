package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.frontend.util.styleSets
import materialui.components.appbar.appBar
import materialui.components.appbar.enums.AppBarPosition
import materialui.components.appbar.enums.AppBarStyle
import materialui.components.button.enums.ButtonColor
import materialui.components.drawer.drawer
import materialui.components.icon.icon
import materialui.components.iconbutton.enums.IconButtonEdge
import materialui.components.iconbutton.iconButton
import materialui.components.list.list
import materialui.components.toolbar.toolbar
import materialui.styles.withStyles
import react.RBuilder
import react.RHandler

/**
 * @author Bernhard Frauendienst <bernhard.frauendienst@markt.de>
 */
private val styledMainDrawer = withStyles("MainDrawer", {
  "appbar" {

  }
}) { props: MainMenuProps ->
  val appbar by props.styleSets
  drawer {
    list{

    }
  }
  appBar(AppBarStyle.positionSticky to appbar) {
    attrs.position = AppBarPosition.sticky
    toolbar {
      iconButton {
        attrs.edge = IconButtonEdge.start
        attrs.color = ButtonColor.inherit
        attrs["aria-label"] = "menu"
        icon {
          +"menu"
        }
      }
      props.children()
    }
  }
}

fun RBuilder.mainDrawer(handler: RHandler<MainMenuProps>) = styledMainDrawer(handler)
