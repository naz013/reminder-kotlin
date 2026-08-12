package com.github.naz013.navigation.intent

interface IntentDataWriter {
  fun putData(key: String, data: Any)
  fun remove(key: String)
}
