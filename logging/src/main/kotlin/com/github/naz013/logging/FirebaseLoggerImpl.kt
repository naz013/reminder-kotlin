package com.github.naz013.logging

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics

internal class FirebaseLoggerImpl : FirebaseLogger {
  override fun logEvent(event: String) {
    Firebase.crashlytics.log(event)
  }

  override fun logEvent(event: String, params: Map<String, Any>) {
    Firebase.crashlytics.log(event)
  }
}
