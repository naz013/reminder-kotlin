package com.github.naz013.logging

import android.util.Log
import org.slf4j.LoggerFactory

internal class LoggerProviderImpl : LoggerProvider {

  private val logger: org.slf4j.Logger = LoggerFactory.getLogger("FileLogger")

  override fun info(message: String) {
    logger.info(message)
    Log.i(TAG, message)
  }

  override fun debug(message: String) {
    logger.debug(message)
    Log.d(TAG, message)
  }

  override fun error(message: String) {
    logger.error(message)
    Log.e(TAG, message)
  }

  override fun error(message: String, throwable: Throwable) {
    logger.error(message, throwable)
    Log.e(TAG, message, throwable)
  }

  override fun warning(message: String) {
    logger.warn(message)
    Log.w(TAG, message)
  }

  override fun warning(message: String, throwable: Throwable) {
    logger.warn(message, throwable)
    Log.w(TAG, message, throwable)
  }

  companion object {
    private const val TAG = "ReminderLog"
  }
}
