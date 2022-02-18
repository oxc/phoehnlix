package de.esotechnik.phoehnlix.frontend.dashboard

import csstype.number
import csstype.px
import date_fns.subDays
import date_fns.subMonths
import date_fns.subWeeks
import date_fns.subYears
import de.esotechnik.phoehnlix.frontend.dashboard.DashboardViewType.*
import de.esotechnik.phoehnlix.frontend.util.isBefore
import de.esotechnik.phoehnlix.frontend.util.subYears
import de.esotechnik.phoehnlix.api.model.MeasureType
import de.esotechnik.phoehnlix.api.model.Profile
import de.esotechnik.phoehnlix.api.model.ProfileMeasurement
import de.esotechnik.phoehnlix.frontend.*
import de.esotechnik.phoehnlix.frontend.dashboard.DashboardViewType.List
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import mui.icons.material.Visibility
import mui.material.*
import mui.system.ThemeProvider
import mui.system.sx
import react.*
import kotlin.js.Date

external interface DashboardProps : PropsWithChildren {
  var profile: Profile?
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

val DashboardPage = FC<DashboardProps> { props ->
  val phoehnlix = useContext(PhoehnlixContext)

  var view: DashboardViewType by useState(Loading)
  var selectedSince: Date? by useState(null)
  var selectedAt: Date? by useState(null)
  var selectedRange: DateRange by useState(Days(10))
  var selectedMeasurements by useState(listOf<ProfileMeasurement>())

  fun loadData(dateRange: DateRange = selectedRange) {
    console.log("Loading with profile = %o", props.profile)
    val profile = props.profile ?: return run {
      if (view != Loading) {
        view = Loading
        selectedRange = dateRange
      }
    }
    val mainScope = MainScope() + CoroutineName("Dashboard")
    mainScope.launch {
      val (from, _) = dateRange.getRange()
      // we always load with open-ended to, so we have the newest measurement for the bullets
      // but save the current date for adjusting the graph x-axis
      val now = Date()
      val newMeasurements = phoehnlix.api.profile[profile.id].measurements(
        from = from?.toISOString()
      ).sortedBy { it.timestamp }
        selectedMeasurements = newMeasurements
        selectedSince = from
        selectedAt = now
        selectedRange = dateRange
        view = when {
          newMeasurements.isEmpty() -> Empty
          view == List -> List
          else -> Graph
      }
    }
  }

  useEffect(props.profile?.id) {
    loadData()
  }

  fun onMoreDataRequested(dateRange: DateRange, callback: (Boolean) -> Unit) {
    val selectedRange = selectedRange
    if (selectedRange == Everything) {
      return callback(false)
    }

    val (from, _) = dateRange.getRange()
    if (from == null) {
      return callback(true).also {
        loadData(Everything)
      }
    }
    if (selectedSince?.isBefore(from) == true) {
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

  ThemeProvider {
    theme = whiteToolbarTheme
    LogoMenu {
      IconButton {
        edge = IconButtonEdge.end
        color = IconButtonColor.inherit
        onClick = {
          view = when (view) {
            Graph -> List
            else -> Graph
          }
        }
        disabled = when (view) {
          Graph, List -> false
          else -> true
        }
        Visibility()
      }
    }

    Grid {
      item = true
      sx {
        flexGrow = number(1.0)
        marginTop = 10.px
      }
      when (view) {
        Graph -> {
          GraphComponent {
            profile = props.profile
            measureTypes = MEASURE_TYPES
            measurements = selectedMeasurements
            selectionDate = selectedAt
            requestMoreData = ::onMoreDataRequested
          }
        }
        List -> {
          MeasurementList {
            measurements = selectedMeasurements.asReversed()
            requestMoreData = if (selectedRange != Everything) {
              { loadData(Everything) }
            } else null
          }
        }
        Empty -> {
          Typography {
            +"Keine Messwerte vorhanden."
          }
        }
        Loading -> {
          GraphSkeleton { measureTypeCount = MEASURE_TYPES.size }
        }
      }
    }
  }
}