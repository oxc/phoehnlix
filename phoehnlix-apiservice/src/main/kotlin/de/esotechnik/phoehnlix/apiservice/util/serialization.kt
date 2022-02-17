package de.esotechnik.phoehnlix.apiservice.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import java.time.Instant

/**
 * @author Bernhard Frauendienst
 */
open class SeparatedStringSerializer(
  private val separator: Char,
  transformationName: String = "SeparatedStringSerializer"
) : JsonTransformingSerializer<List<String>>(
 ListSerializer(String.serializer())
) {
  override fun transformDeserialize(element: JsonElement): JsonElement {
    require(element is JsonPrimitive)
    return JsonArray(
      element.content.split(separator).map(::JsonPrimitive)
    )
  }
}
object SpaceSeparatedSerializer : SeparatedStringSerializer(' ', "SpaceSeparatedSerializer")

object InstantAsStringTimestampSerializer : KSerializer<Instant> {
  override val descriptor = PrimitiveSerialDescriptor("InstantAsStringTimestampSerializer", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): Instant {
    val timestamp = decoder.decodeString()
    return Instant.ofEpochSecond(timestamp.toLong())
  }

  override fun serialize(encoder: Encoder, value: Instant) {
    val dateString = value.epochSecond.toString()
    encoder.encodeString(dateString)
  }
}
