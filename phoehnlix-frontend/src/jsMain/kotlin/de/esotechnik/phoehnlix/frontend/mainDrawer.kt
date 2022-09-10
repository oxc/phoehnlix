package de.esotechnik.phoehnlix.frontend

import csstype.None
import de.esotechnik.phoehnlix.frontend.util.className
import emotion.react.css
import kotlinx.js.jso
import mui.icons.material.Person
import mui.icons.material.SsidChart
import mui.material.Drawer
import mui.material.List
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.MenuItem
import mui.material.MenuList
import mui.material.styles.Theme
import mui.material.styles.useTheme
import react.FC
import react.Props
import react.router.dom.NavLink

external interface MainDrawerProps : Props {
  var open: Boolean
  var onClose: ((event: dynamic, reason: String) -> Unit)?
}

/**
 * @author Bernhard Frauendienst
 */
val MainDrawer = FC<MainDrawerProps> { props ->
  val menuItem by className
  val selected by className

  val theme = useTheme<Theme>()

  Drawer {
    open = props.open
    onClose = props.onClose

    MenuList {
      css {
        menuItem {
          "a" {
            textDecoration = None.none
            color = theme.palette.text.primary
          }
        }
      }

      MenuItem {
        className = menuItem
        ListItemButton {
          ListItemIcon {
            SsidChart {}
          }
          ListItemText {
            NavLink {
              to = "/dashboard"
              +"Dashboard"
            }
          }
        }
      }

      MenuItem {
        className = menuItem
        ListItemButton {
          ListItemIcon {
            Person {}
          }
          ListItemText {
            NavLink {
              to = "/myprofile"
              +"Profil"
            }
          }
        }
      }

    }
  }
}
