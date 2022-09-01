package de.esotechnik.phoehnlix.frontend.dashboard

import csstype.*
import csstype.JustifyContent.Companion.spaceEvenly
import de.esotechnik.phoehnlix.api.model.MeasureType
import de.esotechnik.phoehnlix.frontend.util.circleDiameter
import emotion.react.css
import mui.material.Skeleton
import mui.material.SkeletonVariant
import mui.material.SkeletonVariant.*
import react.FC
import react.PropsWithChildren
import react.dom.html.ReactHTML.div

external interface SkeletonProps : PropsWithChildren {
  var measureTypeCount: Int
}

/*
private val graphSkeleton = withStyles("GraphSkeleton", {
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
  }
  "skeletonGraph" {
    flexGrow = 1.0
    height = 100.vw/2
  }
}) { props: SkeletonProps ->
}

 */

val skeletonContainer = ClassName("skeletonContainer")
val skeletonButton = ClassName("skeletonButton")
val skeletonBulletCaption = ClassName("skeletonBulletCaption")
val skeletonBullet = ClassName("skeletonBullet")

val GraphSkeleton = FC<SkeletonProps> { props ->
  div {
    css {
      skeletonContainer {
        display = Display.flex
        justifyContent = spaceEvenly
        padding = Padding(2.px, 10.px)
      }

      skeletonButton {
        flexGrow = number(1.0)
        height = 40.px
        margin = Margin(0.px, 1.px)
        firstChild {
          marginLeft = 0.px
        }
        lastChild {
          marginRight = 0.px
        }
      }

      circleDiameter = 100.vw / (MeasureType.values().size + 1)

      skeletonBulletCaption {
        width = circleDiameter / 2
      }
      skeletonBullet {
        width = circleDiameter
        height = circleDiameter
      }
    }
    div {
      className = skeletonContainer
      Skeleton {
        css {
          marginTop = 10.px
          width = 20.em
        }
        variant = text
      }
    }
    div {
      className = skeletonContainer
      repeat(props.measureTypeCount) {
        div {
          Skeleton {
            className = skeletonBulletCaption
            variant = text
          }
          Skeleton {
            className = skeletonBullet
            variant = circular
          }
        }
      }
    }
    div {
      className = skeletonContainer
      // buttons
      repeat(5) {
        Skeleton {
          className = skeletonButton
          variant = rectangular
        }
      }
    }
    div {
      className = skeletonContainer

      // graph
      Skeleton {
        css {
          flexGrow = number(1.0)
          height = 100.vw/2
        }
        variant = SkeletonVariant.rectangular
      }
    }
    div {
      className = skeletonContainer

      repeat(props.measureTypeCount) {
        div {
          css {
          }
          Skeleton {
            css {
              width = circleDiameter.div(2)
            }
            variant = text
          }
          Skeleton {
            className = skeletonBullet
            variant = circular
          }
        }
      }
    }
  }
}