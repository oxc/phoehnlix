package de.esotechnik.phoehnlix.apiservice.data

import io.ktor.application.Application
import io.ktor.application.ApplicationFeature
import io.ktor.application.featureOrNull
import io.ktor.application.install
import io.ktor.config.ApplicationConfig
import io.ktor.routing.Routing
import io.ktor.util.AttributeKey
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.ContextDsl
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
@KtorExperimentalAPI
@ContextDsl
fun Application.setupDatabase(): Database =
  featureOrNull(Database) ?: install(Database)

