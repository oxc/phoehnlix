package de.esotechnik.phoehnlix.frontend.util

import react.StateSetter
import react.useEffect
import react.useMemo
import react.useState

typealias Converter<T> = (String) -> Result<T?>
typealias Validator<T> = (String, Result<T?>) -> Result<T?>


/**
 * @author Bernhard Frauendienst
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

fun <T> FormType<T>.validate(validator: Validator<T>) = copy(validator = { raw, result ->
  // chain this validator to the existing one
  validator(raw, this.validator(raw, result))
})
fun <T> FormType<T>.validate(validator: (T) -> String?) = validate validator@{ _, result ->
  if (result.isSuccess) {
    result.getOrNull()?.let { validator(it) }?.let { error ->
      return@validator Result.failure(IllegalArgumentException(error))
    }
  }
  return@validator result
}

val <T> FormType<T>.optional: FormType<T> get() = validate { raw, result ->
  if (result.isFailure && raw.isBlank()) {
    Result.success(null)
  } else {
    result
  }
}


interface FormField<T> {
  val fieldValue: String
  val setFieldValue: (String) -> Unit
  val isError: Boolean
  val typedValue: T?
}

class FormFieldImpl<T>(
  override val fieldValue: String,
  override val setFieldValue: (String) -> Unit,
  type: FormType<T>,
) : FormField<T> {
  private val result = type.validator(fieldValue, type.converter(fieldValue))

  override val isError get() = result.isFailure

  override val typedValue get() = result.getOrNull()
}

val Iterable<FormField<*>>.isAnyError get() = any { it.isError }

inline fun <reified T> formType(): FormType<T> = when (T::class) {
  String::class -> StringFormType
  Int::class -> IntFormType
  Double::class -> DoubleFormType
  else -> error("No form type for ${T::class}")
} as FormType<T>

inline fun <reified T> useFormField(initialValue: T?, noinline validator: ((T) -> String?)? = null): FormField<T> {
  return useFormField(initialValue, formType(), validator)
}
inline fun <reified E: Enum<E>> useFormField(initialValue: E?, noinline validator: ((E) -> String?)? = null): FormField<E> {
  return useFormField(initialValue, enumFormType(), validator)
}

fun <T> useFormField(initialValue: T?, type: FormType<T>, validator: ((T) -> String?)? = null): FormField<T> {
  val formType = if (validator != null) type.validate(validator) else type
  return useFormFieldImpl(formType) { initialValue }
}
private fun <T> useFormFieldImpl(type: FormType<T>, initialValue: () -> T?): FormField<T> {
  var modified by useState(false)
  var state by useState { type.serializer(initialValue()) }
  useEffect(initialValue) {
    if (!modified) {
      state = type.serializer(initialValue())
    }
  }
  fun setFieldValue(value: String) {
    state = value
    modified = true
  }
  return useMemo(state, type) {
    FormFieldImpl(state, ::setFieldValue, type)
  }
}
