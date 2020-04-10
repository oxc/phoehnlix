package de.esotechnik.phoehnlix.frontend.dashboard

import date_fns.subDays
import date_fns.subMonths
import date_fns.subWeeks
import date_fns.subYears
import de.esotechnik.phoehnlix.apiservice.model.Profile
import de.esotechnik.phoehnlix.apiservice.model.ProfileMeasurement
import de.esotechnik.phoehnlix.frontend.api
import de.esotechnik.phoehnlix.frontend.dashboard.DashboardViewType.*
import de.esotechnik.phoehnlix.frontend.logoMenu
import de.esotechnik.phoehnlix.frontend.useApiContext
import de.esotechnik.phoehnlix.frontend.util.isAfter
import de.esotechnik.phoehnlix.frontend.util.isBefore
import de.esotechnik.phoehnlix.frontend.util.styleSets
import de.esotechnik.phoehnlix.frontend.util.subYears
import de.esotechnik.phoehnlix.model.MeasureType
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.css.backgroundColor
import kotlinx.css.height
import kotlinx.css.vh
import kotlinx.html.Tag
import kotlinx.html.js.onClickFunction
import materialui.components.button.enums.ButtonColor
import materialui.components.icon.icon
import materialui.components.iconbutton.iconButton
import materialui.components.paper.enums.PaperStyle
import materialui.components.paper.paper
import materialui.components.typography.typography
import materialui.styles.palette.paper
import materialui.styles.withStyles
import react.RBuilder
import react.RComponent
import react.RHandler
import react.RProps
import react.RState
import react.setState
import kotlin.collections.List
import kotlin.js.Date

/**
 * @author Bernhard Frauendienst
 */
interface DashboardProps : RProps {
  var profile: Profile?
}

interface DashboardState : RState {
  var view: DashboardViewType
  var selectedSince: Date?
  var selectedRange: DateRange
  var measurements: List<ProfileMeasurement>
}

enum class DashboardViewType {
  Loading, Empty, Graph, List
}

sealed class DateRange {
  abstract fun getRange(): Pair<Date?, Date?>
}

sealed class Sub(private val amount: Int, private val sub: (Date, Number) -> Date) : DateRange() {
  override fun getRange() = sub(Date(), amount) to null
}
data class Days(val days: Int) : Sub(days, ::subDays)
data class Weeks(val weeks: Int) : Sub(weeks, ::subWeeks)
data class Months(val months: Int) : Sub(months, ::subMonths)
data class Years(val years: Int) : Sub(years, ::subYears)
object Everything : DateRange() {
  override fun getRange() = null to null
}

private val MEASURE_TYPES = MeasureType.values().asList()

class DashboardComponent(props: DashboardProps) : RComponent<DashboardProps, DashboardState>(props) {
  companion object {
    init {
      useApiContext<DashboardComponent>()
    }
  }

  override fun DashboardState.init(props: DashboardProps) {
    view = Loading

    measurements = listOf()
    selectedRange = Days(10)
  }

  override fun componentDidMount() {
    loadData()
  }

  override fun componentDidUpdate(prevProps: DashboardProps, prevState: DashboardState, snapshot: Any) {
    if (prevProps.profile?.id !== props.profile?.id) {
      loadData()
    }
  }

  private fun loadData(dateRange: DateRange = state.selectedRange) {
    console.log("Loading with profile = %o", props.profile)
    val profile = props.profile ?: return run {
      if (state.view != Loading) {
        setState {
          view = Loading
          selectedRange = dateRange
        }
      }
    }
    val mainScope = MainScope() + CoroutineName("Dashboard")
    mainScope.launch {
      val (from, _) = dateRange.getRange()
      // we always load with open-ended to, so we have the newest for the bullets
      val measurements = api.profile[profile.id].measurements(
        from = from?.toISOString()
      ).sortedBy { it.timestamp }
      setState {
        this.measurements = measurements
        this.selectedSince = from
        this.selectedRange = dateRange
        this.view = when {
          measurements.isEmpty() -> Empty
          state.view == List -> List
          else -> Graph
        }
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
      attrs.elevation = 0
      when (state.view) {
        Graph -> graphFragment {
          attrs {
            profile = props.profile
            measureTypes = MEASURE_TYPES
            measurements = state.measurements
            requestMoreData = ::onMoreDataRequested
          }
        }
        List -> {
          measurementList {
            attrs {
              measurements = state.measurements.asReversed()
              requestMoreData = if (state.selectedRange != Everything) {
                { loadData(Everything) }
              } else null
            }
          }
        }
        Empty -> {
          typography {
            +"Keine Messwerte vorhanden."
          }
        }
        Loading -> graphSkeletonFragment(MEASURE_TYPES.size)
      }
    }
  }

  private fun onMoreDataRequested(dateRange: DateRange, callback: (Boolean) -> Unit) {
    val selectedRange = state.selectedRange
    if (selectedRange == Everything) {
      return callback(false)
    }

    val (from, _) = dateRange.getRange()
    if (from == null) {
      return callback(true).also {
        loadData(Everything)
      }
    }
    if (state.selectedSince?.isBefore(from) == true) {
      return callback(false)
    }
    return callback(true).also {
      if (Date().subYears(1).isBefore(from)) {
        loadData(Days(370)) // load a bit more than a year
      } else {
        loadData(Everything)
      }
    }
  }
}

private val styledComponent = withStyles(DashboardComponent::class, {
  "root" {

  }
})

fun RBuilder.dashboardPage(handler: RHandler<DashboardProps>) = styledComponent(handler)