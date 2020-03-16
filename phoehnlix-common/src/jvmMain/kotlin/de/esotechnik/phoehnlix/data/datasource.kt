package de.esotechnik.phoehnlix.data

import io.ktor.application.Application
import io.ktor.application.ApplicationFeature
import io.ktor.config.ApplicationConfig
import io.ktor.util.AttributeKey
import io.ktor.util.KtorExperimentalAPI
import org.jetbrains.exposed.sql.Database as ExposedDatabase

/**
 * @author Bernhard Frauendienst
 */
class Database(val configuration: Configuration) {
  val url = configuration.url!!
  val user = configuration.user!!
  private val password = configuration.password!!

  class Configuration {
    var url: String? = null
    var user: String? = "phoehnlix"
    var password: String? = null
    var setup: () -> Unit = {}

    @KtorExperimentalAPI
    fun loadFromConfiguration(config: ApplicationConfig) {
      config.propertyOrNull("url")?.let { url = it.getString() }
      config.propertyOrNull("user")?.let { user = it.getString() }
      config.propertyOrNull("password")?.let { password = it.getString() }
    }
  }

  val db = ExposedDatabase.connect(url = url, user = user, password = password).also {
    println("Connected to ${it.url}")
  }

  @KtorExperimentalAPI
  companion object Feature : ApplicationFeature<Application, Configuration, Database> {
    override val key = AttributeKey<Database>("PhoehnlixDatasource")

    override fun install(pipeline: Application, configure: Configuration.() -> Unit): Database {
      val config = Configuration().apply {
        loadFromConfiguration(
          pipeline.environment.config.config("database")
        )
      }.apply(configure)
      return Database(config).apply {
        config.setup()
      }
    }
  }
}
