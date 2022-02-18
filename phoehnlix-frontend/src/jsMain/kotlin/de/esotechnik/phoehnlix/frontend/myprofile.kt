package de.esotechnik.phoehnlix.frontend

import csstype.AlignItems
import csstype.JustifyContent
import csstype.px
import de.esotechnik.phoehnlix.api.client.ProfileId
import de.esotechnik.phoehnlix.api.model.ActivityLevel
import de.esotechnik.phoehnlix.api.model.ProfileDraft
import de.esotechnik.phoehnlix.api.model.Sex
import de.esotechnik.phoehnlix.frontend.util.DoubleFormType
import de.esotechnik.phoehnlix.frontend.util.FormField
import de.esotechnik.phoehnlix.frontend.util.className
import de.esotechnik.phoehnlix.frontend.util.isAnyError
import de.esotechnik.phoehnlix.frontend.util.optional
import de.esotechnik.phoehnlix.frontend.util.useFormField
import emotion.react.css
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.js.jso
import mui.icons.material.Clear
import mui.icons.material.DirectionsRun
import mui.icons.material.Done
import mui.icons.material.Event
import mui.icons.material.Height
import mui.icons.material.OutlinedFlag
import mui.icons.material.PermIdentity
import mui.icons.material.SvgIconComponent
import mui.icons.material.Wc
import mui.material.FormControlVariant
import mui.material.Grid
import mui.material.GridDirection
import mui.material.IconButton
import mui.material.IconButtonColor
import mui.material.IconButtonEdge
import mui.material.MenuItem
import mui.material.SvgIconSize
import mui.material.TextField
import mui.material.TextFieldProps
import mui.system.responsive
import react.*
import react.dom.aria.ariaLabel
import react.dom.onChange

/**
 * @author Bernhard Frauendienst
 */

external interface MyProfileProps : PropsWithChildren {
  var profileDraft: ProfileDraft?
}

val MyProfilePage = FC<MyProfileProps> { props ->
  val phoehnlix = useContext(PhoehnlixContext)

  val nameField = useFormField(props.profileDraft?.name) { name ->
    when {
      name.isBlank() -> "Ungültiger Name"
      else -> null
    }
  }
  val sexField = useFormField(props.profileDraft?.sex)
  val birthdayField = useFormField(props.profileDraft?.birthday)
  val heightField = useFormField(props.profileDraft?.height) { height ->
    when (height) {
      in 0..300 -> null
      else -> "Ungültige Größe"
    }
  }
  val activityLevelField = useFormField(props.profileDraft?.activityLevel)
  val targetWeightField =
    useFormField(props.profileDraft?.targetWeight, DoubleFormType.optional) { targetWeight ->
      when (targetWeight) {
        in 0.0..500.0 -> null
        else -> "Ungültiges Gewicht"
      }
    }

  val allFields = listOf(
    nameField, sexField, birthdayField, heightField,
    activityLevelField, targetWeightField
  )


  fun saveProfile() {
    require(!allFields.isAnyError)
    val profileUpload = ProfileDraft(
      name = nameField.typedValue,
      sex = sexField.typedValue,
      birthday = birthdayField.typedValue,
      height = heightField.typedValue,
      activityLevel = activityLevelField.typedValue,
      targetWeight = targetWeightField.typedValue
    )
    with(phoehnlix) {
      val mainScope = MainScope() + CoroutineName("Application")
      mainScope.launch {
        val profile = api.profile[ProfileId.Me].update(profileUpload)
        update(profile)
      }
    }
  }

  fun <T> FormRowProps.field(field: FormField<T>) {
    value = field.fieldValue
    error = field.isError
    onChangeFunction = { field.setFieldValue(it) }
  }

  inline fun <reified E : Enum<E>> FormRowProps.field(
    field: FormField<E>,
    labelGenerator: (E) -> String
  ) {
    field(field)
    selectOptions(labelGenerator)
  }

  TitleMainMenu {
    title = "Profil bearbeiten"
    IconButton {
      edge = IconButtonEdge.end
      color = IconButtonColor.inherit
      disabled = allFields.isAnyError
      ariaLabel = "save"
      onClick = { saveProfile() }
      Done {}
    }
  }

  Grid {
    container = true
    direction = responsive(GridDirection.column)
    spacing = responsive(3)

    FormRow {
      id = "name"
      label = "Name"
      required = true
      icon = PermIdentity
      field(nameField)
    }

    FormRow {
      id = "birthday"
      label = "Geburtstag"
      required = true
      icon = Event
      field(birthdayField)
      // TODO: date picker
    }

    FormRow {
      id = "sex"
      label = "Geschlecht"
      required = true
      icon = Wc
      field(sexField) {
        when (it) {
          Sex.Male -> "Männlich"
          Sex.Female -> "Weiblich"
        }
      }
    }

    FormRow {
      id = "height"
      label = "Körpergröße"
      required = true
      icon = Height
      field(heightField)
      FormTextField {
        SelectProps = jso {
          endAdornment = Fragment.create {
            +"cm"
          }
        }
      }
    }

    FormRow {
      id = "activity-level"
      label = "Aktivitätsgrad"
      required = true
      icon = DirectionsRun
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

    FormRow {
      id = "target-weight"
      label = "Zielgewicht"
      icon = OutlinedFlag
      field(targetWeightField)
      clearable = true
      FormTextField {
        helperText = Fragment.create { +"Rote Linie im Diagramm" }
        SelectProps = jso {
          endAdornment = Fragment.create {
            +"kg"
          }
        }
      }
    }
  }
}


private external interface FormRowProps : PropsWithChildren {
  var id: String
  var label: String
  var icon: SvgIconComponent?
  var value: Any?
  var error: Boolean
  var required: Boolean
  var clearable: Boolean
  var selectOptions: List<Pair<String, String>>?
  var textField: ((TextFieldProps) -> Unit)?
  var onChangeFunction: (String) -> Unit
}

private fun FormRowProps.FormTextField(block: TextFieldProps.() -> Unit) {
  this.textField = block
}

private inline fun <reified T : Enum<T>> FormRowProps.selectOptions(labelGenerator: (T) -> String) {
  selectOptions = enumValues<T>().map { it.name to labelGenerator(it) }
}

private val FormRow = FC<FormRowProps> { props ->
  val gridIcon by className
  Grid {
    item = true
    container = true
    spacing = responsive(1)
    sx = jso {
      alignItems = AlignItems.flexStart
    }
    css {
      gridIcon {
        minHeight = 64.px
        minWidth = 64.px
      }
    }


    Grid {
      classes = jso {
        item = gridIcon
      }
      item = true
      container = true
      sx = jso {
        justifyContent = JustifyContent.center
        alignItems = AlignItems.center
      }
      asDynamic().xs = 2
      //xs(2)
      props.icon?.let { icon ->
        icon {
          fontSize = SvgIconSize.large
        }
      }
    }

    Grid {
      item = true
      asDynamic().xs = 8

      //xs(8)

      TextField {
        id = props.id
        variant = FormControlVariant.outlined
        label = Fragment.create { +props.label }
        value = props.value ?: ""
        error = props.error
        required = props.required
        fullWidth = true
        onChange = { event ->
          props.onChangeFunction(event.target.asDynamic().value.unsafeCast<String>())
        }
        props.selectOptions?.let { options ->
          select = true
          for ((option, optionLabel) in options) {
            MenuItem {
              key = option
              value = option
              +optionLabel
            }
          }
        }
        props.textField?.let { it(this) }
      }
    }

    Grid {
      classes = jso {
        item = gridIcon
      }
      item = true
      container = true
      sx = jso {
        justifyContent = JustifyContent.center
        alignItems = AlignItems.center
      }
      // xs(2)
      asDynamic().xs = 2

      if (props.clearable) {
        IconButton {
          Clear {}
          onClick = {
            props.onChangeFunction("")
          }
        }
      }
    }
  }
}