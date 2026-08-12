package com.github.naz013.logging

import android.app.Application

fun Application.initLogging(
  isDebug: Boolean = false,
  loggerProvider: LoggerProvider = LoggerProviderImpl(),
  firebaseLogger: FirebaseLogger = FirebaseLoggerImpl()
) {
  Logger.initLogging(
    isDebug = isDebug,
    loggerProvider = loggerProvider,
    firebaseLogger = firebaseLogger
  )
}
