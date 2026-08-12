package com.github.naz013.logging

interface FirebaseLogger {
  fun logEvent(event: String)
  fun logEvent(event: String, params: Map<String, Any>)
}
