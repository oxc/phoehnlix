package de.esotechnik.phoehnlix.frontend.util

import kotlinx.browser.window
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.w3c.dom.Storage
import org.w3c.dom.WindowLocalStorage
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.reflect.KProperty

/**
 * @author Bernhard Frauendienst <bernhard.frauendienst@markt.de>
 */

private val json = Json.Default

class StorageAttribute<T>(
  private val storage: Storage = window.localStorage,
  private val propertyName: String,
  private val serializer: KSerializer<T>
) {
  fun setValue(value: T?) {
    if (value != null) {
      storage[propertyName] = json.encodeToString(serializer, value)
    } else {
      storage.removeItem(propertyName)
    }
  }

  fun getValue() = storage[propertyName]?.let { json.decodeFromString(serializer, it) }
}

class StorageAttributeDelegateProvider<T>(private val storage: Storage, private val serializer: KSerializer<T>) {
  operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): StorageAttribute<T> {
    return StorageAttribute(storage, property.name, serializer)
  }
}

fun <T> Storage.attribute(serializer: KSerializer<T>) = StorageAttributeDelegateProvider(this, serializer)
fun <T> Storage.attribute(name: String, serializer: KSerializer<T>) = StorageAttribute(this, name, serializer)

operator fun <T> StorageAttribute<T>.getValue(thisRef: Any?, property: KProperty<*>) = getValue()
operator fun <T> StorageAttribute<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T?) = setValue(value)