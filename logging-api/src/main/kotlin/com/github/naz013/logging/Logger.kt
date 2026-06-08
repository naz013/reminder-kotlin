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

  fun v(tag: String, message: String) {
    if (loggingEnabled) {
      loggerProvider?.verbose(tag, message)
    }
  }

  fun d(tag: String, message: String) {
    if (loggingEnabled && isDebug) {
      loggerProvider?.debug(tag, message)
    }
  }

  fun i(tag: String, message: String) {
    if (loggingEnabled) {
      loggerProvider?.info(tag, message)
    }
    if (reportingEnabled) {
      firebaseLogger?.logEvent(message)
    }
  }

  fun w(tag: String, message: String) {
    if (loggingEnabled) {
      loggerProvider?.warning(tag, message)
    }
  }

  fun e(tag: String, message: String, t: Throwable) {
    if (loggingEnabled) {
      loggerProvider?.error(tag, message, t)
    }
    if (reportingEnabled) {
      firebaseLogger?.logEvent(message)
    }
  }

  fun e(tag: String, message: String) {
    if (loggingEnabled) {
      loggerProvider?.error(tag, message)
    }
    if (reportingEnabled) {
      firebaseLogger?.logEvent(message)
    }
  }

  fun logEvent(event: String) {
    loggerProvider?.info("Event", event)
    if (reportingEnabled) {
      firebaseLogger?.logEvent(event)
    }
  }

  fun private(value: String?): String {
    return if (value == null) {
      "Null"
    } else {
      if (isDebug) {
        value
      } else {
        "*****"
      }
    }
  }

  fun data(value: String?): String {
    return if (value == null) {
      "Null"
    } else if (value.isEmpty()) {
      "Empty"
    } else {
      value
    }
  }
}
