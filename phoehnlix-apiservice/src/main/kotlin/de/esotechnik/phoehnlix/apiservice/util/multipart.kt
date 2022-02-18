package de.esotechnik.phoehnlix.apiservice.util

import io.ktor.http.content.*
import io.ktor.server.plugins.*
import io.ktor.util.converters.*
import io.ktor.util.reflect.*
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.jvm.jvmName

/**
 * @author Bernhard Frauendienst
 */
/**
 * Get [FormItem] value converting to type [R] using [DefaultConversionService]
 * @throws ParameterConversionException when conversion from String to [R] fails
 */
inline fun <reified R : Any> PartData.FormItem.getValue(): R {
  return getValueImpl(R::class, typeInfo<R>())
}

@PublishedApi
internal fun <R : Any> PartData.FormItem.getValueImpl(type: KClass<R>, typeInfo: TypeInfo): R {
  return try {
    type.cast(DefaultConversionService.fromValues(listOf(value), typeInfo))
  } catch (cause: Exception) {
    throw ParameterConversionException(name ?: "<unnamed>", type.jvmName, cause)
  }
}

