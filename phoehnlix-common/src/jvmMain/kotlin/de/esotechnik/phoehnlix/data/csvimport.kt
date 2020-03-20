package de.esotechnik.phoehnlix.data

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import de.esotechnik.phoehnlix.model.ActivityLevel
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.text.NumberFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale


private val TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")
/**
 * @author Bernhard Frauendienst
 */
class CsvImport {

  private val csv = csvReader {
    charset = "ISO-8859-15"
    quoteChar = '"'
    delimiter = ';'
    escapeChar = '\\'
  }

  fun readData(csvData: InputStream): CsvData {
    val data = csv.readAll(csvData)
    val header = data.first()
    if (header != listOf("Datum","Uhrzeit","Gewicht","Körperfett","Wasser","Muskelmasse","BMI","Notizen")) {
      throw IllegalArgumentException("Unexpected header line: $header")
    }
    return CsvData(data)
  }
}
class CsvData(private val data: List<List<String>>) {
  suspend fun import(profile: Profile, activityLevel: ActivityLevel?) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.GERMAN)
    fun String.parseDouble() = takeIf { it.isNotEmpty() }?.let {
      numberFormat.parse(it).toDouble()
    }

    newSuspendedTransaction(Dispatchers.IO) {
      data.drop(1).forEach { line ->
        val (day, time, weight, fat, water, muscle, bmi, notes) = line
        val timestamp = Instant.from(TIMESTAMP_FORMAT.parse("$day $time"))

        Measurement.new {
          this.timestamp = timestamp
          setProfile(profile)
          activityLevel?.let { this.activityLevel = it }
          this.weight = weight.parseDouble()!!
          this.bodyFatPercent = fat.parseDouble()
          this.bodyWaterPercent = water.parseDouble()
          this.muscleMassPercent = muscle.parseDouble()
          this.bodyMassIndex = bmi.parseDouble()
          this.notes = notes
        }
      }
    }
  }
}

private operator fun <E> List<E>.component6() = get(6)
private operator fun <E> List<E>.component7() = get(7)
private operator fun <E> List<E>.component8() = get(8)
