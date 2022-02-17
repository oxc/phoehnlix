package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.frontend.util.styleSets
import kotlinx.css.TextAlign
import kotlinx.css.TextTransform.uppercase
import kotlinx.css.flexGrow
import kotlinx.css.fontFamily
import kotlinx.css.marginBottom
import kotlinx.css.properties.TextDecorationLine
import kotlinx.css.properties.textDecoration
import kotlinx.css.px
import kotlinx.css.textAlign
import kotlinx.css.textTransform
import materialui.components.appbar.appBar
import materialui.components.appbar.enums.AppBarPosition.sticky
import materialui.components.appbar.enums.AppBarStyle
import materialui.components.button.enums.ButtonColor.inherit
import materialui.components.grid.enums.GridDirection
import materialui.components.grid.enums.GridJustify
import materialui.components.grid.enums.GridStyle
import materialui.components.grid.enums.GridWrap
import materialui.components.grid.grid
import materialui.components.icon.icon
import materialui.components.iconbutton.enums.IconButtonEdge.start
import materialui.components.iconbutton.iconButton
import materialui.components.toolbar.toolbar
import materialui.components.typography.enums.TypographyStyle
import materialui.components.typography.enums.TypographyVariant
import materialui.components.typography.enums.TypographyVariant.h6
import materialui.components.typography.typographyH1
import materialui.components.typography.typographyH2
import materialui.components.typography.typographyH3
import materialui.styles.withStyles
import react.*
import react.dom.attrs

/**
 * @author Bernhard Frauendienst
 */

interface MainMenuProps : PropsWithChildren {
}

private val styledMainMenu = withStyles("MainMenu", {
  "appbar" {

  }
}) { props: MainMenuProps ->
  val appbar by props.styleSets
  appBar(AppBarStyle.positionSticky to appbar) {
    attrs.position = sticky
    toolbar {
      iconButton {
        attrs.edge = start
        attrs.color = inherit
        attrs["aria-label"] = "menu"
        icon {
          +"menu"
        }
      }
      props.children()
    }
  }
}

fun RBuilder.baseMainMenu(handler: RHandler<MainMenuProps>) = styledMainMenu(handler)

interface TitleMenuProps : MainMenuProps {
  var title: String
  var subtitle: String?
}

private val titleMainMenu = withStyles("TitleMainMenu", {
  "title" {
    flexGrow = 1.0
  }
}) { props: TitleMenuProps ->
  baseMainMenu {
    val title by props.styleSets
    grid(GridStyle.container to title) {
      attrs {
        container = true
        direction = GridDirection.column
        wrap = GridWrap.nowrap
        justify = GridJustify.center
      }
      typographyH2 {
        attrs.variant = h6
        +props.title
      }
      props.subtitle?.let { subtitle ->
        typographyH3 {
          attrs.variant = TypographyVariant.subtitle1
          +subtitle
        }
      }
    }
    props.children()
  }
}

fun RBuilder.mainMenu(handler: RHandler<TitleMenuProps>) = titleMainMenu(handler)

class LogoMenu(props: MainMenuProps) : RComponent<MainMenuProps, State>(props) {
  override fun RBuilder.render() {
    val title by props.styleSets
    baseMainMenu {
      typographyH1(TypographyStyle.root to title) {
        attrs.variant = h6
        +"Phoehnlix"
      }

      props.children()
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

fun RBuilder.logoMenu(handler: RHandler<MainMenuProps>) = styledLogoMenu(handler)