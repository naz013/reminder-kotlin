package com.github.naz013.navigation

import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.navigation.intent.IntentDataWriter

internal class IntentDataHolder : IntentDataWriter, IntentDataReader {

  private val map = mutableMapOf<String, Any>()

  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> get(key: String, clazz: Class<T>): T? {
    return (map[key]?.takeIf { it.javaClass == clazz } as? T).also {
      remove(key)
    }
  }

  override fun hasKey(key: String): Boolean {
    return map.containsKey(key)
  }

  override fun putData(key: String, data: Any) {
    map[key] = data
  }

  override fun remove(key: String) {
    map.remove(key)
  }
}
