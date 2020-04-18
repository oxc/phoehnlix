package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.frontend.Application.Companion.whiteToolbarTheme
import de.esotechnik.phoehnlix.frontend.util.styleSets
import kotlinx.css.TextAlign
import kotlinx.css.TextTransform.uppercase
import kotlinx.css.flexGrow
import kotlinx.css.fontFamily
import kotlinx.css.properties.TextDecorationLine
import kotlinx.css.properties.textDecoration
import kotlinx.css.textAlign
import kotlinx.css.textTransform
import materialui.components.appbar.appBar
import materialui.components.appbar.enums.AppBarPosition.sticky
import materialui.components.button.enums.ButtonColor.inherit
import materialui.components.icon.icon
import materialui.components.iconbutton.iconButton
import materialui.components.toolbar.toolbar
import materialui.components.typography.enums.TypographyStyle
import materialui.components.typography.enums.TypographyVariant.h6
import materialui.components.typography.enums.TypographyVariant.headline
import materialui.components.typography.enums.TypographyVariant.subheading
import materialui.components.typography.typography
import materialui.styles.themeprovider.themeProvider
import materialui.styles.withStyles
import react.RBuilder
import react.RComponent
import react.RHandler
import react.RProps
import react.RState

/**
 * @author Bernhard Frauendienst
 */

interface MainMenuProps : RProps {
}

interface MainMenuState : RState {

}

class MainMenuComponent(props: MainMenuProps) : RComponent<MainMenuProps, MainMenuState>(props) {
  override fun RBuilder.render() {
    appBar {
      attrs.position = sticky
      toolbar {
        iconButton {
          attrs["edge"] = "start"
          attrs.color = inherit
          attrs["aria-label"] = "menu"
          icon {
            +"menu"
          }
        }
        children()
      }
    }
  }

  companion object {
    fun RBuilder.render(handler: RHandler<MainMenuProps>) =
      child(MainMenuComponent::class) {
        this.handler()
      }
  }
}

fun RBuilder.mainMenu(handler: RHandler<MainMenuProps>) = with(MainMenuComponent) {
  render(handler)
}

private val styledMainMenu = withStyles(MainMenuComponent::class, {
  "title" {
    flexGrow = 1.0
  }
})

fun RBuilder.mainMenu(title: String, subtitle: String? = null, handler: RHandler<MainMenuProps>) {
  styledMainMenu {
    typography {
      attrs.variant = headline
      +title
    }
    subtitle?.let { subtitle ->
      typography {
        attrs.variant = subheading
        +subtitle
      }
    }
    handler()
  }
}

class LogoMenu(props: MainMenuProps) : RComponent<MainMenuProps, RState>(props) {
  override fun RBuilder.render() {
    val title by props.styleSets
    mainMenu {
      typography(TypographyStyle.root to title) {
        attrs.variant = h6
        +"Phoehnlix"
      }

      children()
    }
  }

}

private val styledLogoMenu = withStyles(LogoMenu::class, {
  "title" {
    flexGrow = 1.0
    fontFamily = "Days One, sans-serif"
    textAlign = TextAlign.center
    textDecoration(TextDecorationLine.overline)
    textTransform = uppercase
  }
})

fun RBuilder.logoMenu(handler: RHandler<MainMenuProps>) = themeProvider(whiteToolbarTheme) {
  styledLogoMenu(handler)
}