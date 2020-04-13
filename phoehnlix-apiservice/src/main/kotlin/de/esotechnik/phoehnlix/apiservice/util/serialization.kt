package de.esotechnik.phoehnlix.apiservice.util

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.builtins.list
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * @author Bernhard Frauendienst
 */
open class SeparatedStringSerializer(private val separator: Char, transformationName: String = "SeparatedStringSerializer") : JsonTransformingSerializer<List<String>>(
  String.serializer().list, transformationName
) {
  override fun readTransform(element: JsonElement): JsonElement {
    require(element is JsonPrimitive)
    return JsonArray(
      element.content.split(separator).map(::JsonPrimitive)
    )
  }
}
object SpaceSeparatedSerializer : SeparatedStringSerializer(' ', "SpaceSeparatedSerializer")

object InstantAsStringTimestampSerializer : KSerializer<Instant> {
  override val descriptor = PrimitiveDescriptor("InstantAsStringTimestampSerializer", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): Instant {
    val timestamp = decoder.decodeString()
    return Instant.ofEpochSecond(timestamp.toLong())
  }

  override fun serialize(encoder: Encoder, value: Instant) {
    val dateString = value.epochSecond.toString()
    encoder.encodeString(dateString)
  }
}
