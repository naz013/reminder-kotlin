package com.github.naz013.logging

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

internal class FirebaseLoggerImpl : FirebaseLogger {
  override fun logEvent(event: String) {
    Firebase.crashlytics.log(event)
  }

  override fun logEvent(event: String, params: Map<String, Any>) {
    Firebase.crashlytics.log(event)
  }
}
