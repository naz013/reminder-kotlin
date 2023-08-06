package com.backdoor.engine.misc

internal object Logger {

  var LOG_ENABLED: Boolean = true

  fun log(message: String) {
    if (LOG_ENABLED) {
      println("Recognizer: $message")
    }
  }
}
