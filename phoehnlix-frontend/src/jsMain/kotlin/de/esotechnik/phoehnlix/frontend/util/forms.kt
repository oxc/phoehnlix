package de.esotechnik.phoehnlix.frontend.util

import react.Component
import react.Props
import react.State
import kotlin.reflect.KMutableProperty1

typealias Converter<T> = (String) -> Result<T?>
typealias Validator<T> = (String, Result<T?>) -> Result<T?>


/**
 * @author Bernhard Frauendienst <bernhard.frauendienst@markt.de>
 */
data class FormType<T>(
  val converter: Converter<T>,
  val validator: Validator<T> = { _, result -> result },
  val serializer: (T?) -> String = { it?.toString() ?: "" }
)

val StringFormType = FormType({ Result.success(it) })
val IntFormType = FormType({ Result.runCatching { it.toInt() } })
val DoubleFormType = FormType({ Result.runCatching { it.toDouble() } })
inline fun <reified E : Enum<E>> enumFormType() = FormType({ Result.runCatching { enumValueOf<E>(it) } },
  serializer = { it?.name ?: "" })

interface FormField<T> {
  val fieldValue: String
  val isError: Boolean
  val typedValue: T?
}

interface StateFormField<T, S: State> : FormField<T> {
  fun setStateValue(state: S, value: String?)

  fun validate(validator: Validator<T>): StateFormField<T, S>
  fun validate(validator: (T) -> String?) = validate validator@{ _, result ->
    if (result.isSuccess) {
      result.getOrNull()?.let { validator(it) }?.let { error ->
        return@validator Result.failure(IllegalArgumentException(error))
      }
    }
    return@validator result
  }
  fun optional() = validate { raw, result ->
    if (result.isFailure && raw.isBlank()) {
      Result.success(null)
    } else {
      result
    }
  }
}

operator fun <S : State> S.set(field: StateFormField<*, S>, value: String?)
  = field.setStateValue(this, value)

class StateFormFieldImpl<T, C: Component<P, S>, P: Props, S: State>(
  private val component: C,
  private val stateProperty: KMutableProperty1<S, String?>,
  private val type: FormType<T>,
  private val valueInitializer: P.() -> T?
) : StateFormField<T, S> {
  private val initialValue by memoizeOne(
    { props -> type.serializer(valueInitializer(props)) },
    { component.props }
  )

  override val fieldValue by memoizeOne(
    { state, initialValue -> stateProperty.get(state) ?: initialValue },
    { component.state }, { initialValue }
  )

  private val result by memoizeOne(
    { v -> type.validator(v, type.converter(v)) },
    { fieldValue }
  )

  override val isError get() = result.isFailure

  override val typedValue get() = result.getOrNull()

  override fun setStateValue(state: S, value: String?) {
    stateProperty.set(state, value)
  }

  override fun validate(validator: Validator<T>): StateFormField<T, S> =
    StateFormFieldImpl(component, stateProperty, type.copy(validator = { raw, result ->
      // chain this validator to the existing one
      validator(raw, type.validator(raw, result))
    }), valueInitializer)
}

val Iterable<FormField<*>>.isAnyError get() = any { it.isError }

fun <T, C: Component<P, S>, P: Props, S: State> C.formField(
  stateProperty: KMutableProperty1<S, String?>,
  type: FormType<T>,
  initialValue: P.() -> T?
): StateFormField<T, S> = StateFormFieldImpl(this, stateProperty, type, initialValue)

fun <C: Component<P, S>, P: Props, S: State> C.intField(
  stateProperty: KMutableProperty1<S, String?>,
  initialValue: P.() -> Int?
) = formField(stateProperty, IntFormType, initialValue)

fun <C: Component<P, S>, P: Props, S: State> C.doubleField(
  stateProperty: KMutableProperty1<S, String?>,
  initialValue: P.() -> Double?
) = formField(stateProperty, DoubleFormType, initialValue)

fun <C: Component<P, S>, P: Props, S: State> C.stringField(
  stateProperty: KMutableProperty1<S, String?>,
  initialValue: P.() -> String?
) = formField(stateProperty, StringFormType, initialValue)

inline fun <reified T : Enum<T>, C: Component<P, S>, P: Props, S: State> C.enumField(
  stateProperty: KMutableProperty1<S, String?>,
  noinline initialValue: P.() -> T?
) = formField(stateProperty, enumFormType(), initialValue)
