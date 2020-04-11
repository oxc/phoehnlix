package de.esotechnik.phoehnlix.apiservice.util

import io.ktor.features.ParameterConversionException
import io.ktor.http.content.PartData
import io.ktor.util.DefaultConversionService
import io.ktor.util.KtorExperimentalAPI
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmName
import kotlin.reflect.typeOf

/**
 * @author Bernhard Frauendienst
 */
/**
 * Get [FormItem] value converting to type [R] using [DefaultConversionService]
 * @throws ParameterConversionException when conversion from String to [R] fails
 */
@KtorExperimentalAPI
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified R : Any> PartData.FormItem.getValue(): R {
  return getValueImpl(R::class, typeOf<R>().javaType)
}

@PublishedApi
@OptIn(ExperimentalStdlibApi::class)
internal fun <R : Any> PartData.FormItem.getValueImpl(type: KClass<R>, javaType: Type): R {
  return try {
    type.cast(DefaultConversionService.fromValues(listOf(value), javaType))
  } catch (cause: Exception) {
    throw ParameterConversionException(name ?: "<unnamed>", type.jvmName, cause)
  }
}

