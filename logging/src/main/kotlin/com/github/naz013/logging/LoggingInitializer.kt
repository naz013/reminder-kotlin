package com.github.naz013.logging

import android.app.Application

fun Application.initLogging(
  isDebug: Boolean = false
) {
  Logger.initLogging(
    isDebug = isDebug,
    loggerProvider = LoggerProviderImpl(),
    firebaseLogger = FirebaseLoggerImpl()
  )
}
