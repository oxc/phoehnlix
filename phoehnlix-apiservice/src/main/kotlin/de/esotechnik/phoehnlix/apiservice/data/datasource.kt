package de.esotechnik.phoehnlix.apiservice.data

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.util.*
import org.jetbrains.exposed.sql.Database as ExposedDatabase

/**
 * @author Bernhard Frauendienst
 */
class Database(configuration: Configuration) {
  val url = configuration.url!!
  val user = configuration.user!!
  private val password = configuration.password!!

  class Configuration {
    var url: String? = null
    var user: String? = "phoehnlix"
    var password: String? = null

    fun loadFromConfiguration(config: ApplicationConfig) {
      config.propertyOrNull("url")?.let { url = it.getString() }
      config.propertyOrNull("user")?.let { user = it.getString() }
      config.propertyOrNull("password")?.let { password = it.getString() }
    }
  }

  val db = ExposedDatabase.connect(url = url, user = user, password = password).also {
    println("Connected to ${it.url}")
  }

  companion object DatabasePlugin : Plugin<Application, Configuration, Database> {
    override val key = AttributeKey<Database>("PhoehnlixDatasource")

    override fun install(pipeline: Application, configure: Configuration.() -> Unit): Database {
      val config = Configuration().apply {
        loadFromConfiguration(
          pipeline.environment.config.config("phoehnlix.database")
        )
      }.apply(configure)
      return Database(config).apply {
        setupSchema()
      }
    }
  }
}

/**
 * Gets or installs a [Routing] feature for the this [Application] and runs a [configuration] script on it
 */
@KtorDsl
fun Application.setupDatabase(): Database =
  pluginOrNull(Database) ?: install(Database)

