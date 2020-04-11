package de.esotechnik.phoehnlix.frontend.dashboard

import de.esotechnik.phoehnlix.frontend.util.circleDiameter
import de.esotechnik.phoehnlix.frontend.util.styleSets
import de.esotechnik.phoehnlix.api.model.MeasureType
import kotlinx.css.Align
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.JustifyContent
import kotlinx.css.StyledElement
import kotlinx.css.alignItems
import kotlinx.css.display
import kotlinx.css.em
import kotlinx.css.flexDirection
import kotlinx.css.flexGrow
import kotlinx.css.height
import kotlinx.css.justifyContent
import kotlinx.css.margin
import kotlinx.css.marginLeft
import kotlinx.css.marginRight
import kotlinx.css.marginTop
import kotlinx.css.padding
import kotlinx.css.px
import kotlinx.css.vw
import kotlinx.css.width
import materialui.lab.components.skeleton.enums.SkeletonStyle
import materialui.lab.components.skeleton.enums.SkeletonVariant
import materialui.lab.components.skeleton.skeleton
import materialui.styles.StylesSet
import materialui.styles.withStyles
import react.RBuilder
import react.RProps
import react.dom.div

/**
 * @author Bernhard Frauendienst
 */

interface SkeletonProps : RProps {
  var measureTypeCount: Int
}

private val styleSets: StylesSet.() -> Unit = {
  "root" {
    circleDiameter = 100.vw.div(MeasureType.values().size + 1)
  }
  // skeleton
  "skeletonContainer" {
    display = Display.flex
    justifyContent = JustifyContent.spaceEvenly
    padding(2.px, 10.px)
  }
  "skeletonHeadline" {
    marginTop = 10.px
    width = 20.em
  }
  "skeletonBulletContainer" {
    display = Display.flex
    flexDirection = FlexDirection.columnReverse
    alignItems = Align.center
  }
  "skeletonBullet" {
    width = circleDiameter
    height = circleDiameter
  }
  "skeletonBulletCaption" {
    width = circleDiameter/2
  }
  "skeletonTimeButton" {
    flexGrow = 1.0
    height = 40.px
    margin(0.px, 1.px)
    firstChild {
      marginLeft = 0.px
    }
    lastChild {
      marginRight = 0.px
    }
  }
  "skeletonGraph" {
    flexGrow = 1.0
    height = 100.vw/2
  }
}

private val graphSkeleton = withStyles("GraphSkeleton", styleSets) { props: SkeletonProps ->
  val root by props.styleSets
  val skeletonContainer by props.styleSets
  val skeletonHeadline by props.styleSets
  val skeletonBulletContainer by props.styleSets
  val skeletonBullet by props.styleSets
  val skeletonBulletCaption by props.styleSets
  val skeletonTimeButton by props.styleSets
  val skeletonGraph by props.styleSets

  div(root) {
    div(skeletonContainer) {
      skeleton(SkeletonStyle.root to skeletonHeadline) {
        attrs.variant = SkeletonVariant.text
      }
    }
    div(skeletonContainer) {
      repeat(props.measureTypeCount) {
        div(skeletonBulletContainer) {
          skeleton(SkeletonStyle.root to skeletonBulletCaption) {
            attrs.variant = SkeletonVariant.text
          }
          skeleton(SkeletonStyle.root to skeletonBullet) {
            attrs.variant = SkeletonVariant.circle
          }
        }
      }
    }
    div(skeletonContainer) {
      // buttons
      repeat(5) {
        skeleton(SkeletonStyle.root to skeletonTimeButton) {
          attrs.variant = SkeletonVariant.rect
        }
      }
    }
    div(skeletonContainer) {
      // graph
      skeleton(SkeletonStyle.root to skeletonGraph) {
        attrs.variant = SkeletonVariant.rect
      }
    }
    div(skeletonContainer) {
      repeat(props.measureTypeCount) {
        div(skeletonBulletContainer) {
          skeleton(SkeletonStyle.root to skeletonBulletCaption) {
            attrs.variant = SkeletonVariant.text
          }
          skeleton(SkeletonStyle.root to skeletonBullet) {
            attrs.variant = SkeletonVariant.circle
          }
        }
      }
    }
  }
}

fun RBuilder.graphSkeletonFragment(measureTypeCount: Int)
  = graphSkeleton { attrs.measureTypeCount = measureTypeCount }