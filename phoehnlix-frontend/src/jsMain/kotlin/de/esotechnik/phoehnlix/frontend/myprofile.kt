package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.api.client.ProfileId
import de.esotechnik.phoehnlix.api.model.ActivityLevel
import de.esotechnik.phoehnlix.api.model.Profile
import de.esotechnik.phoehnlix.api.model.ProfileDraft
import de.esotechnik.phoehnlix.api.model.Sex
import de.esotechnik.phoehnlix.frontend.util.StateFormField
import de.esotechnik.phoehnlix.frontend.util.doubleField
import de.esotechnik.phoehnlix.frontend.util.enumField
import de.esotechnik.phoehnlix.frontend.util.intField
import de.esotechnik.phoehnlix.frontend.util.isAnyError
import de.esotechnik.phoehnlix.frontend.util.set
import de.esotechnik.phoehnlix.frontend.util.stringField
import de.esotechnik.phoehnlix.frontend.util.styleSets
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.css.minHeight
import kotlinx.css.minWidth
import kotlinx.css.px
import kotlinx.html.DIV
import kotlinx.html.Tag
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import materialui.components.button.enums.ButtonColor
import materialui.components.formcontrol.enums.FormControlVariant.outlined
import materialui.components.grid.enums.GridAlignItems.center
import materialui.components.grid.enums.GridDirection.column
import materialui.components.grid.enums.GridJustify
import materialui.components.grid.enums.GridStyle
import materialui.components.grid.grid
import materialui.components.icon.enums.IconFontSize
import materialui.components.icon.icon
import materialui.components.iconbutton.enums.IconButtonEdge
import materialui.components.iconbutton.iconButton
import materialui.components.menuitem.menuItem
import materialui.components.textfield.TextFieldElementBuilder
import materialui.components.textfield.textField
import materialui.styles.withStyles
import react.RBuilder
import react.RComponent
import react.RHandler
import react.RProps
import react.RState
import react.buildElement
import react.setState

/**
 * @author Bernhard Frauendienst
 */

interface MyProfileProps : RProps {
  var profileDraft: ProfileDraft?
}

interface MyProfileState : RState {
  var name: String?
  var sex: String?
  var birthday: String?
  var height: String?
  var activityLevel: String?
  var targetWeight: String?
}

class MyProfileComponent(props: MyProfileProps) : RComponent<MyProfileProps, MyProfileState>(props) {
  companion object {
    init {
      usePhoehnlixContext<MyProfileComponent>()
    }
  }

  private val nameField = stringField(MyProfileState::name, initialValue = { profileDraft?.name }).validate { name ->
    when {
      name.isBlank() -> "Ungültiger Name"
      else -> null
    }
  }
  private val sexField = enumField(MyProfileState::sex, initialValue = { profileDraft?.sex })
  private val birthdayField = stringField(MyProfileState::birthday, initialValue = { profileDraft?.birthday })
  private val heightField = intField(MyProfileState::height, initialValue = { profileDraft?.height }).validate { height ->
    when (height) {
      in 0..300 -> null
      else -> "Ungültige Größe"
    }
  }
  private val activityLevelField = enumField(MyProfileState::activityLevel, initialValue = { profileDraft?.activityLevel })
  private val targetWeightField = doubleField(MyProfileState::targetWeight, initialValue = { profileDraft?.targetWeight }).optional().validate { targetWeight: Double ->
    when (targetWeight) {
      in 0.0..500.0 -> null
      else -> "Ungültiges Gewicht"
    }
  }

  private val allFields = listOf(
    nameField, sexField, birthdayField, heightField,
    activityLevelField, targetWeightField
  )


  private fun saveProfile() {
    require(!allFields.isAnyError)
    val profileUpload = ProfileDraft(
      name = nameField.typedValue,
      sex = sexField.typedValue,
      birthday = birthdayField.typedValue,
      height = heightField.typedValue,
      activityLevel = activityLevelField.typedValue,
      targetWeight = targetWeightField.typedValue
    )
    with (phoehnlix) {
      val mainScope = MainScope() + CoroutineName("Application")
      mainScope.launch {
        val profile = api.profile[ProfileId.Me].update(profileUpload)
        update(profile)
      }
    }
  }

  private fun <T> FormRowProps.field(field: StateFormField<T, MyProfileState>) {
    value = field.fieldValue
    error = field.isError
    onChangeFunction = { setState { this[field] = it } }
  }

  private inline fun <reified E: Enum<E>> FormRowProps.field(field: StateFormField<E, MyProfileState>, labelGenerator: (E) -> String) {
    field(field)
    selectOptions(labelGenerator)
  }

  override fun RBuilder.render() {
    mainMenu {
      attrs {
        title = "Profil bearbeiten"
      }
      iconButton {
        attrs {
          edge = IconButtonEdge.end
          color = ButtonColor.inherit
          (this as Tag).disabled = allFields.isAnyError
          this["aria-label"] = "save"
          onClickFunction = { saveProfile() }
        }
        icon {
          +"done"
        }
      }
    }

    grid {
      attrs {
        container = true
        direction = column
        spacing(3)
      }

      formRow {
        attrs {
          id = "name"
          label = "Name"
          required = true
          icon = "perm_identity"
          field(nameField)
        }
      }

      formRow {
        attrs {
          id = "birthday"
          label = "Geburtstag"
          required = true
          icon = "event"
          field(birthdayField)
          // TODO: date picker
        }
      }

      formRow {
        attrs {
          id = "sex"
          label = "Geschlecht"
          required = true
          icon = "wc"
          field(sexField) {
            when (it) {
              Sex.Male -> "Männlich"
              Sex.Female -> "Weiblich"
            }
          }
        }
      }

      formRow {
        attrs {
          id = "height"
          label = "Körpergröße"
          required = true
          icon = "height"
          field(heightField)
          textField {
            attrs {
              inputProps {
                endAdornment {
                  +"cm"
                }
              }
            }
          }
        }
      }

      formRow {
        attrs {
          id = "activity-level"
          label = "Aktivitätsgrad"
          required = true
          icon = "directions_run"
          field(activityLevelField) {
            when (it) {
              ActivityLevel.VeryLow -> "sehr gering: 1"
              ActivityLevel.Low -> "gering: 2"
              ActivityLevel.Normal -> "normal: 3"
              ActivityLevel.High -> "sportlich: 4"
              ActivityLevel.VeryHigh -> "sehr sportlich: 5"
            }
          }
        }
      }

      formRow {
        attrs {
          id = "target-weight"
          label = "Zielgewicht"
          icon = "outlined_flag"
          field(targetWeightField)
          clearable = true
          textField {
            attrs {
              helperText = buildElement { +"Rote Linie im Diagramm" }
              inputProps {
                endAdornment {
                  +"kg"
                }
              }
            }
          }
        }
      }
    }
  }
}

private val styledComponent = withStyles(MyProfileComponent::class, {

})

private interface FormRowProps : RProps {
  var id: String
  var label: String
  var icon: String?
  var value: Any?
  var error: Boolean
  var required: Boolean
  var clearable: Boolean
  var selectOptions: List<Pair<String, String>>?
  var textField: (TextFieldElementBuilder<DIV>.() -> Unit)?
  var onChangeFunction: (String) -> Unit
}

private fun FormRowProps.textField(block: TextFieldElementBuilder<DIV>.() -> Unit) {
  this.textField = block
}

private inline fun <reified T : Enum<T>> FormRowProps.selectOptions(labelGenerator: (T) -> String) {
  selectOptions = enumValues<T>().map { it.name to labelGenerator(it) }
}

private val formRow = withStyles("MyProfileFormRow", {
  "gridIcon" {
    minHeight = 64.px
    minWidth = 64.px
  }
}) { props: FormRowProps ->
  val gridIcon by props.styleSets
  grid {
    attrs {
      item = true
      container = true
      spacing(1)
    }

    grid(GridStyle.item to gridIcon) {
      attrs {
        item = true
        container = true
        justify = GridJustify.center
        alignItems = center
        xs(2)
      }
      props.icon?.let { iconName ->
        icon {
          attrs.fontSize = IconFontSize.large
          +iconName
        }
      }
    }

    grid {
      attrs {
        item = true
        xs(8)
      }

      textField {
        attrs {
          id = attrs.id
          variant = outlined
          label { +props.label }
          value = props.value ?: ""
          error = props.error
          required = props.required
          fullWidth = true
          onChangeFunction = { event ->
            props.onChangeFunction(event.target.asDynamic().value.unsafeCast<String>())
          }
        }
        props.selectOptions?.let { options ->
          attrs.select = true
          for ((option, label) in options) {
            menuItem {
              attrs {
                key = option
                value = option
              }
              +label
            }
          }
        }
        props.textField?.let { it() }
      }
    }

    grid(GridStyle.item to gridIcon) {
      attrs {
        item = true
        container = true
        justify = GridJustify.center
        alignItems = center
        xs(2)
      }

      if (props.clearable) {
        iconButton {
          icon {
            +"clear"
          }
          attrs.onClickFunction = {
            props.onChangeFunction("")
          }
        }
      }
    }
  }
}

fun RBuilder.myProfilePage(handler: RHandler<MyProfileProps>) = styledComponent(handler)