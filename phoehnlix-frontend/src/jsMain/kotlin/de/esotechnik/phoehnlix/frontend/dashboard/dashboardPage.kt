package de.esotechnik.phoehnlix.frontend.dashboard

import date_fns.FormatOptions
import date_fns.format
import date_fns.locale.de
import de.esotechnik.phoehnlix.apiservice.model.Profile
import de.esotechnik.phoehnlix.apiservice.model.ProfileMeasurement
import de.esotechnik.phoehnlix.frontend.api
import de.esotechnik.phoehnlix.frontend.dashboard.DashboardViewType.*
import de.esotechnik.phoehnlix.frontend.logoMenu
import de.esotechnik.phoehnlix.frontend.parseTimestamp
import de.esotechnik.phoehnlix.frontend.title
import de.esotechnik.phoehnlix.frontend.useApiContext
import de.esotechnik.phoehnlix.frontend.util.styleSets
import de.esotechnik.phoehnlix.model.MeasureType
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.css.Align
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.JustifyContent
import kotlinx.css.TextAlign
import kotlinx.css.alignContent
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
import kotlinx.css.paddingTop
import kotlinx.css.px
import kotlinx.css.textAlign
import kotlinx.css.vh
import kotlinx.css.vw
import kotlinx.css.width
import kotlinx.html.H2
import kotlinx.html.H6
import kotlinx.html.js.onClickFunction
import materialui.components.button.enums.ButtonColor
import materialui.components.icon.icon
import materialui.components.iconbutton.iconButton
import materialui.components.button.button
import materialui.components.button.enums.ButtonVariant
import materialui.components.buttongroup.buttonGroup
import materialui.components.paper.paper
import materialui.components.typography.enums.TypographyAlign
import materialui.components.typography.enums.TypographyStyle
import materialui.components.typography.enums.TypographyVariant
import materialui.components.typography.typography
import materialui.lab.components.skeleton.enums.SkeletonAnimation
import materialui.lab.components.skeleton.enums.SkeletonStyle
import materialui.lab.components.skeleton.enums.SkeletonVariant
import materialui.lab.components.skeleton.skeleton
import materialui.styles.StylesSet
import materialui.styles.childWithStyles
import react.RBuilder
import react.RComponent
import react.RHandler
import react.RProps
import react.RState
import react.dom.div
import react.dom.span
import react.setState
import kotlin.collections.List

/**
 * @author Bernhard Frauendienst
 */
interface DashboardProps : RProps {
  var profile: Profile?
}

interface DashboardState : RState {
  var view: DashboardViewType
  var measurements: List<ProfileMeasurement>
}

enum class DashboardViewType {
  Loading, Empty, Graph, List
}

class DashboardComponent(props: DashboardProps) : RComponent<DashboardProps, DashboardState>(props) {
  override fun DashboardState.init(props: DashboardProps) {
    view = Loading

    measurements = listOf()
  }

  override fun componentDidMount() {
    loadData()
  }

  override fun componentDidUpdate(prevProps: DashboardProps, prevState: DashboardState, snapshot: Any) {
    if (prevProps.profile?.id !== props.profile?.id) {
      loadData()
    }
  }

  private fun loadData() {
    console.log("Loading with profile = %o", props.profile)
    val profile = props.profile ?: return run {
      if (state.view != Loading) {
        setState {
          view = Loading
        }
      }
    }
    val mainScope = MainScope() + CoroutineName("Dashboard")
    mainScope.launch {
      val measurements = api.profile[profile.id].measurements().sortedBy { it.timestamp }
      setState {
        this.measurements = measurements
        this.view = if (measurements.isEmpty()) Empty else Graph
      }
    }
  }

  override fun RBuilder.render() {
    logoMenu {
      iconButton {
        attrs {
          color = ButtonColor.inherit
          onClickFunction = {
            setState {
              view = when (state.view) {
                Graph -> Loading // should be List
                else -> Graph
              }
            }
          }
          /*
          disabled = when (state.view) {
            Graph, List -> false
            else -> true
          }
           */
        }
        icon {
          +"visibility"
        }
      }
    }
    paper {
      when (state.view) {
        Graph -> graphFragment()
        List -> listFragment()
        Empty -> {
          typography {
            +"Keine Messwerte vorhanden."
          }
        }
        Loading -> graphSkeletonFragment()
      }
    }
  }

  private val measureTypes = MeasureType.values().toList()

  private fun RBuilder.graphSkeletonFragment() {
    val skeletonContainer by styleSets
    val skeletonHeadline by styleSets
    val skeletonBulletContainer by styleSets
    val skeletonBullet by styleSets
    val skeletonBulletCaption by styleSets
    val skeletonTimeButton by styleSets
    val skeletonGraph by styleSets

    div (skeletonContainer) {
      skeleton(SkeletonStyle.root to skeletonHeadline) {
        attrs.variant = SkeletonVariant.text
      }
    }
    div(skeletonContainer) {
      repeat(measureTypes.size) {
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
  }

  private fun RBuilder.graphFragment() {
    state.measurements.last().let { entry ->
      val timestamp = entry.parseTimestamp()
      val formatOptions = new<FormatOptions> {
        locale = de
      }
      val graphHeadline by styleSets
      val bulletGroup by styleSets
      val bulletContainer by styleSets
      val bulletCaption by styleSets

      typography(TypographyStyle.root to graphHeadline, factory = { H2(mapOf(), it) }) {
        attrs.variant = TypographyVariant.subtitle1
        attrs.align = TypographyAlign.center
        +"Ihre Messwerte vom "
        +format(timestamp, "dd.MM.yyyy", formatOptions)
      }
      div(bulletGroup) {
        bullets(entry, measureTypes, props) { measureType, classes, content ->
          div(bulletContainer) {
            typography(TypographyStyle.root to bulletCaption) {
              attrs.variant = TypographyVariant.caption
              +measureType.title
            }
            div(classes) {
              content()
            }
          }
        }
      }
    }
    buttonGroup {
      attrs {
        fullWidth = true
        color = ButtonColor.secondary
        variant = ButtonVariant.contained
      }
      button {
        +"Woche"
      }
      button {
        +"Monat"
      }
      button {
        +"6\u00a0Monate"
      }
      button {
        +"Jahr"
      }
      button {
        +"…"
      }
    }
    measurementChart {
      attrs.measureTypes = measureTypes
      attrs.measurements = state.measurements
      attrs.targetWeight = props.profile?.targetWeight
    }
  }

  private fun RBuilder.listFragment() {
    measurementList {
      attrs.measurements = state.measurements.asReversed()
    }
  }

  companion object {

    private val styleSets: StylesSet.() -> Unit = {
      "graphHeadline" {
        textAlign = TextAlign.center
        marginTop = 5.px
      }
      "bulletGroup" {
        display = Display.flex
        justifyContent = JustifyContent.spaceEvenly
        padding(10.px)
      }
      "bulletContainer" {
        display = Display.flex
        flexDirection = FlexDirection.columnReverse
        alignItems = Align.center
      }
      "bulletCaption" {

      }
      val diameter = 100.vw.div(MeasureType.values().size + 1)
      makeBulletStyles(diameter = diameter, fontSize = 16.px)

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
        width = diameter
        height = diameter
      }
      "skeletonBulletCaption" {
        width = diameter/2
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

    init {
      useApiContext<DashboardComponent>()
    }

    fun RBuilder.render(handler: RHandler<DashboardProps>) =
      childWithStyles(DashboardComponent::class, styleSets, handler = handler)
  }
}

fun RBuilder.dashboardPage(handler: RHandler<DashboardProps>)
  = with(DashboardComponent) { render(handler) }