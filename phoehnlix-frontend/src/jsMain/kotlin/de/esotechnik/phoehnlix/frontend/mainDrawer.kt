package de.esotechnik.phoehnlix.frontend

import csstype.AlignItems
import csstype.Display
import csstype.FlexDirection
import csstype.JustifyContent
import csstype.None
import csstype.Padding
import csstype.number
import csstype.px
import de.esotechnik.phoehnlix.api.model.Profile
import de.esotechnik.phoehnlix.frontend.util.className
import emotion.css.ClassName
import emotion.react.css
import kotlinx.js.jso
import mui.icons.material.Logout
import mui.icons.material.Person
import mui.icons.material.SsidChart
import mui.material.Avatar
import mui.material.Drawer
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.MenuItem
import mui.material.MenuList
import mui.material.Typography
import mui.material.styles.Theme
import mui.material.styles.useTheme
import react.FC
import react.Props
import react.router.dom.NavLink
import react.useContext

external interface ProfileMenuProps : Props {
  var profile: Profile
}

val menu = csstype.ClassName("main-menu")

private val ProfileMenu = FC<ProfileMenuProps> { props ->
  val profile = props.profile
  val theme = useTheme<Theme>()

  MenuItem {
    classes = jso {
      root = ClassName {
        ".$menu &" {
          backgroundColor = theme.palette.secondary.light
          padding = 0.px

          "a" {
            display = Display.flex
            flexDirection = FlexDirection.row
            justifyContent = JustifyContent.left
            alignItems = AlignItems.center

            color = theme.palette.secondary.contrastText
          }
        }
      }
    }
    divider = true

    NavLink {
      to = "/myprofile"

      Avatar {
        css {
          width = theme.spacing(7)
          height = theme.spacing(7)
          margin = theme.spacing(2)
        }
        profile.imageUrl?.let { src = it }
      }
      Typography {
        +profile.name
      }
    }
  }


  MenuItem {
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

  MenuItem {
    ListItemButton {
      ListItemIcon {
        Logout {}
      }
      ListItemText {
        NavLink {
          to = "/logout"
          +"Logout"
        }
      }
    }
  }
}

external interface MainDrawerProps : Props {
  var open: Boolean
  var onClose: ((event: dynamic, reason: String) -> Unit)?
}


/**
 * @author Bernhard Frauendienst
 */
val MainDrawer = FC<MainDrawerProps> { props ->
  val theme = useTheme<Theme>()
  val ctx = useContext(PhoehnlixContext)

  Drawer {
    open = props.open
    onClose = props.onClose

    MenuList {
      className = ClassName(menu) {
       ".MuiMenuItem-root" {
          "a" {
            textDecoration = None.none
            color = theme.palette.text.primary
          }
        }
      }

      if (ctx.isLoggedIn) {
        val currentProfile = ctx.currentProfile
        if (currentProfile != null) {
          ProfileMenu {
            profile = currentProfile
          }
        } else {
          MenuItem {
            ListItemButton {
              ListItemIcon {
                Person {}
              }
              ListItemText {
                NavLink {
                  to = "/myprofile"
                  +"Profil vervollständigen"
                }
              }
            }
          }
        }
      } else {
        MenuItem {
          ListItemButton {
            ListItemIcon {
              Person {}
            }
            ListItemText {
              NavLink {
                to = "/login"
                +"Login"
              }
            }
          }
        }
      }
    }
  }
}
