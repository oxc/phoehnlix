package de.esotechnik.phoehnlix.frontend.dashboard

import de.esotechnik.phoehnlix.apiservice.client.ApiClient
import de.esotechnik.phoehnlix.apiservice.model.Profile
import de.esotechnik.phoehnlix.apiservice.model.ProfileMeasurement
import de.esotechnik.phoehnlix.frontend.ApiContext
import de.esotechnik.phoehnlix.frontend.Application
import de.esotechnik.phoehnlix.frontend.api
import de.esotechnik.phoehnlix.frontend.useApiContext
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import materialui.components.get
import react.RBuilder
import react.RComponent
import react.RContext
import react.RHandler
import react.RProps
import react.RState
import react.RStatics
import react.setState

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
  companion object {
    init {
      useApiContext<DashboardComponent>()
    }
  }


  override fun DashboardState.init(props: DashboardProps) {
    view = DashboardViewType.Loading

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
      if (state.view != DashboardViewType.Loading) {
        setState {
          view = DashboardViewType.Loading
        }
      }
    }
    val mainScope = MainScope() + CoroutineName("Dashboard")
    mainScope.launch {
      val measurements = api.profile[profile.id].measurements().sortedBy { it.timestamp }
      setState {
        this.measurements = measurements
        this.view = DashboardViewType.Graph
      }
    }
  }

  override fun RBuilder.render() {
    measurementChart {
      attrs.measurements = state.measurements
    }
    measurementList {
      attrs.measurements = state.measurements.asReversed()
    }
  }
}

fun RBuilder.dashboardPage(handler: RHandler<DashboardProps>) = with(
  DashboardComponent
) {
  child(DashboardComponent::class, handler)
}