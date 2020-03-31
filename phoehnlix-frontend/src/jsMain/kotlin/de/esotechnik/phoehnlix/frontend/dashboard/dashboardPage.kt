package de.esotechnik.phoehnlix.frontend.dashboard

import date_fns.FormatOptions
import date_fns.format
import date_fns.locale.de
import de.esotechnik.phoehnlix.apiservice.model.Profile
import de.esotechnik.phoehnlix.apiservice.model.ProfileMeasurement
import de.esotechnik.phoehnlix.apiservice.model.get
import de.esotechnik.phoehnlix.frontend.api
import de.esotechnik.phoehnlix.frontend.color
import de.esotechnik.phoehnlix.frontend.dashboard.DashboardViewType.*
import de.esotechnik.phoehnlix.frontend.logoMenu
import de.esotechnik.phoehnlix.frontend.parseTimestamp
import de.esotechnik.phoehnlix.frontend.unit
import de.esotechnik.phoehnlix.frontend.useApiContext
import de.esotechnik.phoehnlix.frontend.util.measurementColor
import de.esotechnik.phoehnlix.frontend.util.style
import de.esotechnik.phoehnlix.frontend.util.styleSets
import de.esotechnik.phoehnlix.model.MeasureType
import de.esotechnik.phoehnlix.util.formatDecimalDigits
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.JustifyContent
import kotlinx.css.TextAlign
import kotlinx.css.display
import kotlinx.css.justifyContent
import kotlinx.css.padding
import kotlinx.css.px
import kotlinx.css.textAlign
import kotlinx.css.vw
import kotlinx.html.H6
import kotlinx.html.js.onClickFunction
import materialui.components.button.enums.ButtonColor
import materialui.components.circularprogress.circularProgress
import materialui.components.icon.icon
import materialui.components.iconbutton.iconButton
import materialui.components.paper.paper
import materialui.components.typography.enums.TypographyAlign
import materialui.components.typography.enums.TypographyVariant
import materialui.components.typography.typography
import materialui.styles.StylesSet
import materialui.styles.childWithStyles
import react.Fragment
import react.RBuilder
import react.RComponent
import react.RHandler
import react.RProps
import react.RState
import react.dom.div
import react.dom.h6
import react.dom.span
import react.dom.sup
import react.setState
import kotlin.collections.List

/**
 * @author Bernhard Frauendienst <bernhard.frauendienst@markt.de>
 */
interface DashboardProps : RProps {
  var profile: Profile?
}

interface DashboardState : RState {
  var view: DashboardViewType
  var measurements: List<ProfileMeasurement>
}

enum class DashboardViewType {
  Loading, Graph, List
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
        this.view = Graph
      }
    }
  }

  override fun RBuilder.render() {
    logoMenu {
      iconButton {
        attrs.color = ButtonColor.inherit
        attrs.onClickFunction = {
          setState {
            view = when (state.view) {
              Graph -> List
              else -> Graph
            }
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
        Loading -> circularProgress {
        }
      }
    }
  }

  private fun RBuilder.graphFragment() {
    val measureTypes = MeasureType.values().toList()

    state.measurements.lastOrNull()?.let { entry ->
      val timestamp = entry.parseTimestamp()
      val formatOptions = new<FormatOptions> {
        locale = de
      }
      val bulletGroup by styleSets

      typography(factory = { H6(mapOf(), it) }) {
        attrs.variant = TypographyVariant.h6
        attrs.align = TypographyAlign.center
        +"Ihre Messwerte vom "
        +format(timestamp, "dd.MM.yyyy", formatOptions)
      }
      div(bulletGroup) {
        bullets(entry, measureTypes, props) { classes, content ->
          div(classes) {
            content()
          }
        }
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
      "bulletsCaption" {
        textAlign = TextAlign.center

      }
      "bulletGroup" {
        display = Display.flex
        justifyContent = JustifyContent.spaceEvenly
        padding(10.px)
      }
      makeBulletStyles(diameter = 100.vw.div(MeasureType.values().size+1), fontSize = 16.px)
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