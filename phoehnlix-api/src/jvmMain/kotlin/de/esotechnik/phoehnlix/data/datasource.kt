package de.esotechnik.phoehnlix.data

import org.jetbrains.exposed.sql.Database

/**
 * @author Bernhard Frauendienst
 */
class Database(
  val url: String,
  val user: String = "phoehnlix",
  val password: String
) {
  val db by lazy {
    Database.connect(
      url = url,
      user = user,
      password = password
    ).also {
      println("Connected to $it")
    }
  }

  fun connect(): de.esotechnik.phoehnlix.data.Database {
    db
    return this
  }
}