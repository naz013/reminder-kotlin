package com.elementary.tasks.core.os

class IntentDataHolder {

  private val map = mutableMapOf<String, Any>()

  @Suppress("UNCHECKED_CAST")
  fun <T : Any> get(key: String, clazz: Class<T>): T? {
    return map[key]?.takeIf { it.javaClass == clazz } as? T
  }

  fun hasKey(key: String): Boolean {
    return map.containsKey(key)
  }

  fun putData(key: String, data: Any) {
    map[key] = data
  }

  fun remove(key: String) {
    map.remove(key)
  }
}
