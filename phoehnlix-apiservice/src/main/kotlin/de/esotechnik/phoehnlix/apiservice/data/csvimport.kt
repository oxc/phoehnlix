package de.esotechnik.phoehnlix.apiservice.data

import de.esotechnik.phoehnlix.api.model.ActivityLevel
import de.esotechnik.phoehnlix.apiservice.calculateMetabolicRate
import de.esotechnik.phoehnlix.util.roundToDigits
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.nio.charset.Charset
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger


private val TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")
  .withZone(ZoneId.of("Europe/Berlin"))
private val PATTERN_BACKSLASH = """\\([\\"])""".toRegex()

/**
 * @author Bernhard Frauendienst
 */
object CsvImport {

  private val charset = Charset.forName("ISO-8859-15")

  suspend fun import(csvData: InputStream, profile: Profile, activityLevel: ActivityLevel?): Int {
    // TODO: replace this with a filter input stream?
    val reader = csvData.reader(charset).buffered()
    val header = reader.readLine().csvLine()
    if (header != listOf("Datum","Uhrzeit","Gewicht","Körperfett","Wasser","Muskelmasse","BMI","Notizen")) {
      throw IllegalArgumentException("Unexpected header line: $header")
    }

    val numberFormat = NumberFormat.getNumberInstance(Locale.GERMAN)
    fun String.parseDouble() = takeIf { it.isNotEmpty() }?.let {
      numberFormat.parse(it).toDouble()
    }

    val counter = AtomicInteger(0)
    newSuspendedTransaction(Dispatchers.IO) {
      reader.forEachLine { line ->
        val (day, time, weight, fat, water, muscle, bmi, escapedNotes) = line.csvLine()
        val timestamp = Instant.from(TIMESTAMP_FORMAT.parse("$day $time"))

        // notes are backslash escaped (except for newlines, which are \n)
        // Use Json parser for first level of escaping, and manually do the second
        val notes = escapedNotes.takeIf { it.isNotEmpty() }?.let {
          (Json.parseToJsonElement(""""$it"""").jsonPrimitive).content.toString()
            .replace(PATTERN_BACKSLASH, "$1")
        }

        Measurement.new {
          this.timestamp = timestamp
          setProfile(profile)
          activityLevel?.let { this.activityLevel = it }
          this.weight = weight.parseDouble()!!
          this.bodyFatPercent = fat.parseDouble()
          this.bodyWaterPercent = water.parseDouble()
          this.muscleMassPercent = muscle.parseDouble()
          this.bodyMassIndex = bmi.parseDouble()
          this.metabolicRate = this.muscleMassPercent?.let {
            calculateMetabolicRate(this.activityLevel!!, this.weight, it).roundToDigits(1)
          }
          this.notes = notes
        }

        counter.incrementAndGet()
      }
    }

    return counter.get()
  }
}

private fun String.csvLine() = split(';', limit = 8).map { it.removeSurrounding("\"") }

private operator fun <E> List<E>.component6() = get(5)
private operator fun <E> List<E>.component7() = get(6)
private operator fun <E> List<E>.component8() = get(7)
