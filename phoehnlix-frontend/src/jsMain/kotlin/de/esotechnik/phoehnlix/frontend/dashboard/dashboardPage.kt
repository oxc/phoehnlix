package de.esotechnik.phoehnlix.frontend.dashboard

import date_fns.FormatOptions
import date_fns.format
import date_fns.locale.de
import de.esotechnik.phoehnlix.apiservice.model.Profile
import de.esotechnik.phoehnlix.apiservice.model.ProfileMeasurement
import de.esotechnik.phoehnlix.frontend.api
import de.esotechnik.phoehnlix.frontend.color
import de.esotechnik.phoehnlix.frontend.dashboard.DashboardViewType.*
import de.esotechnik.phoehnlix.frontend.logoMenu
import de.esotechnik.phoehnlix.frontend.parseTimestamp
import de.esotechnik.phoehnlix.frontend.title
import de.esotechnik.phoehnlix.frontend.useApiContext
import de.esotechnik.phoehnlix.frontend.util.circleDiameter
import de.esotechnik.phoehnlix.frontend.util.measurementColor
import de.esotechnik.phoehnlix.frontend.util.styleSets
import de.esotechnik.phoehnlix.model.MeasureType
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.JustifyContent
import kotlinx.css.TextAlign
import kotlinx.css.alignItems
import kotlinx.css.backgroundColor
import kotlinx.css.boxShadow
import kotlinx.css.color
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
import kotlinx.css.properties.BoxShadows
import kotlinx.css.px
import kotlinx.css.textAlign
import kotlinx.css.vw
import kotlinx.css.width
import kotlinx.html.H2
import kotlinx.html.Tag
import kotlinx.html.js.onClickFunction
import materialui.components.button.button
import materialui.components.button.enums.ButtonColor
import materialui.components.button.enums.ButtonVariant
import materialui.components.buttongroup.buttonGroup
import materialui.components.buttongroup.enums.ButtonGroupStyle
import materialui.components.icon.icon
import materialui.components.iconbutton.enums.IconButtonStyle
import materialui.components.iconbutton.iconButton
import materialui.components.paper.paper
import materialui.components.typography.enums.TypographyAlign
import materialui.components.typography.enums.TypographyStyle
import materialui.components.typography.enums.TypographyVariant
import materialui.components.typography.typography
import materialui.lab.components.skeleton.enums.SkeletonStyle
import materialui.lab.components.skeleton.enums.SkeletonVariant
import materialui.lab.components.skeleton.skeleton
import materialui.styles.StylesSet
import materialui.styles.withStyles
import react.RBuilder
import react.RComponent
import react.RHandler
import react.RProps
import react.RState
import react.dom.div
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
  var measureTypes: List<MeasureType>
  var visibleMeasureTypes: Set<MeasureType>
}

enum class DashboardViewType {
  Loading, Empty, Graph, List
}

class DashboardComponent(props: DashboardProps) : RComponent<DashboardProps, DashboardState>(props) {
  override fun DashboardState.init(props: DashboardProps) {
    view = Loading

    measurements = listOf()
    measureTypes = MeasureType.values().asList()
    visibleMeasureTypes = measureTypes.toSet()
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
          attrs["edge"] = "end"
          color = ButtonColor.inherit
          onClickFunction = {
            setState {
              view = when (state.view) {
                Graph -> List
                else -> Graph
              }
            }
          }
          (this as Tag).disabled = when (state.view) {
            Graph, List -> false
            else -> true
          }
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

  private fun RBuilder.graphSkeletonFragment() {
    val skeletonContainer by styleSets
    val skeletonHeadline by styleSets
    val skeletonBulletContainer by styleSets
    val skeletonBullet by styleSets
    val skeletonBulletCaption by styleSets
    val skeletonTimeButton by styleSets
    val skeletonGraph by styleSets

    div(skeletonContainer) {
      skeleton(SkeletonStyle.root to skeletonHeadline) {
        attrs.variant = SkeletonVariant.text
      }
    }
    div(skeletonContainer) {
      repeat(state.measureTypes.size) {
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
      repeat(state.measureTypes.size) {
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

  private fun RBuilder.graphFragment() {
    val graphHeadline by styleSets
    val bulletGroup by styleSets
    val bulletContainer by styleSets
    val bulletCaption by styleSets
    val timeButton by styleSets
    val graphContainer by styleSets
    val toggleButton by styleSets
    val unchecked by styleSets

    val measureTypes = state.measureTypes
    val visibleMeasureTypes = state.visibleMeasureTypes
    val latest = state.measurements.last()

    typography(TypographyStyle.root to graphHeadline, factory = { H2(mapOf(), it) }) {
      attrs.variant = TypographyVariant.subtitle1
      attrs.align = TypographyAlign.center

      val timestamp = latest.parseTimestamp()
      val formatOptions = new<FormatOptions> {
        locale = de
      }

      +"Ihre Messwerte vom "
      +format(timestamp, "dd.MM.yyyy", formatOptions)
    }
    div(bulletGroup) {
      bullets(latest, measureTypes, props) { measureType, classes, content ->
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
    buttonGroup(ButtonGroupStyle.groupedContained to timeButton) {
      attrs {
        fullWidth = true
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
    div(graphContainer) {
      measurementChart {
        attrs.measureTypes = measureTypes
        attrs.visibleMeasureTypes = visibleMeasureTypes
        attrs.measurements = state.measurements
        attrs.targetWeight = props.profile?.targetWeight
      }
    }
    div(bulletGroup) {
      measureTypes.forEach { measureType ->
        div(bulletContainer) {
          typography(TypographyStyle.root to bulletCaption) {
            attrs.variant = TypographyVariant.caption
            +measureType.title
          }

          val uncheckedClass = if (measureType !in visibleMeasureTypes) unchecked else ""
          iconButton(IconButtonStyle.root to "$toggleButton ${measureType.cssClass} $uncheckedClass") {
            attrs {
              onClickFunction = {
                toggleVisibleType(measureType)
              }
            }
            icon {
              +when (measureType) {
                MeasureType.Weight -> "speed"
                MeasureType.BodyFatPercent -> "scatter_plot"
                MeasureType.BodyWaterPercent -> "waves"
                MeasureType.MuscleMassPercent -> "fitness_center"
                MeasureType.BodyMassIndex -> "image_aspect_ratio"
                MeasureType.MetabolicRate -> "whatshot"
              }
            }
          }
        }
      }
    }
  }

  private fun toggleVisibleType(measureType: MeasureType) {
    setState(partialState = new {
        visibleMeasureTypes = state.visibleMeasureTypes.withElementToggled(measureType)
    })
  }

  private fun <T> Set<T>.withElementToggled(element: T): Set<T> = if (element in this) {
    this - element
  } else {
    this + element
  }

  private fun RBuilder.listFragment() {
    measurementList {
      attrs.measurements = state.measurements.asReversed()
    }
  }

  companion object {

    private val styleSets: StylesSet.() -> Unit = {
      val diameter = 100.vw.div(MeasureType.values().size + 1)
      // graph
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
      makeBulletStyles(diameter = diameter, fontSize = 16.px)
      "timeButton" {
        boxShadow = BoxShadows.none
        color = Color.white
        backgroundColor = Color("#A6A6A6")
        hover {
          boxShadow = BoxShadows.none
          backgroundColor = Color("#626262")
        }
      }
      "graphContainer" {
        padding(10.px, 0.px)
      }
      "toggleButton" {
        circleDiameter = diameter
        width = circleDiameter
        height = circleDiameter
        backgroundColor = measurementColor
        color = Color.white
        MeasureType.values().forEach { type ->
          +type.cssClass {
            measurementColor = Color(type.color)
          }
        }
        hover {
          backgroundColor = measurementColor
          color = Color.white
        }
        "&\$unchecked" {
          backgroundColor = Color("#EAEAEA")
          color = Color("#A8A8A8")
        }
      }
      "unchecked" {}

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

    private val styledComponent = withStyles(DashboardComponent::class, styleSets)

    fun RBuilder.render(handler: RHandler<DashboardProps>) =
      styledComponent(handler)
  }
}

fun RBuilder.dashboardPage(handler: RHandler<DashboardProps>)
  = with(DashboardComponent) { render(handler) }