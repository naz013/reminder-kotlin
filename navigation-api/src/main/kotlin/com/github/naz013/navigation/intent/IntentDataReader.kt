package com.github.naz013.navigation.intent

interface IntentDataReader {
  fun hasKey(key: String): Boolean
  fun <T : Any> get(key: String, clazz: Class<T>): T?
}
