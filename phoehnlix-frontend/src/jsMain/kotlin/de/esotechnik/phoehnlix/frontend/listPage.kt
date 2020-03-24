package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.model.MeasureType
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.p
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.p
import styled.StyleSheet
import styled.css
import styled.styledDiv

/**
 * @author Bernhard Frauendienst
 */
object MeasurementStyles : StyleSheet("MeasurementStyles") {
  val entryList by css {
    display = Display.flex
    flexDirection = FlexDirection.column
  }

  val entry by css {


  }
}

interface MeasurementEntryProps : RProps {
}

private val MEASURE_TYPES = MeasureType.values().toList()

class MeasurementEntryCompoment : RComponent<MeasurementEntryProps, RState>() {
  override fun RBuilder.render() {
    styledDiv {
      css {
        +MeasurementStyles.entry
      }
      p {
        +
      }
    }
  }
}