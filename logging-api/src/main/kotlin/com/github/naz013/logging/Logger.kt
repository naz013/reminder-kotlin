package com.github.naz013.logging

object Logger {

  var loggingEnabled: Boolean = true
  var reportingEnabled: Boolean = true
  private var isDebug: Boolean = false
  private var loggerProvider: LoggerProvider? = null
  private var firebaseLogger: FirebaseLogger? = null

  fun initLogging(
    isDebug: Boolean,
    loggerProvider: LoggerProvider,
    firebaseLogger: FirebaseLogger
  ) {
    this.isDebug = isDebug
    this.loggerProvider = loggerProvider
    this.firebaseLogger = firebaseLogger
  }

  fun d(tag: String, message: String) {
    d("$tag: $message")
  }

  fun d(message: String) {
    if (loggingEnabled && isDebug) {
      loggerProvider?.debug(message)
    }
  }

  fun i(tag: String, message: String) {
    i("$tag: $message")
  }

  fun i(message: String) {
    if (loggingEnabled) {
      loggerProvider?.info(message)
    }
  }

  fun w(tag: String, message: String) {
    w("$tag: $message")
  }

  fun w(message: String) {
    if (loggingEnabled) {
      loggerProvider?.warning(message)
    }
  }

  fun e(tag: String, message: String, t: Throwable) {
    e("$tag: $message", t)
  }

  fun e(tag: String, message: String) {
    e("$tag: $message")
  }

  fun e(t: Throwable, message: String) {
    e(message, t)
  }

  fun e(message: String, t: Throwable) {
    if (loggingEnabled) {
      loggerProvider?.error(message, t)
    }
  }

  fun e(message: String) {
    if (loggingEnabled) {
      loggerProvider?.error(message)
    }
  }

  fun logEvent(event: String) {
    loggerProvider?.info(event)
    if (reportingEnabled) {
      firebaseLogger?.logEvent(event)
    }
  }
}
