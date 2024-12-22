package com.github.naz013.logging

import org.slf4j.LoggerFactory

internal class LoggerProviderImpl : LoggerProvider {

  private val logger: org.slf4j.Logger = LoggerFactory.getLogger("FileLogger")

  override fun info(message: String) {
    logger.info(message)
  }

  override fun debug(message: String) {
    logger.debug(message)
  }

  override fun error(message: String) {
    logger.error(message)
  }

  override fun error(message: String, throwable: Throwable) {
    logger.error(message, throwable)
  }

  override fun warning(message: String) {
    logger.warn(message)
  }

  override fun warning(message: String, throwable: Throwable) {
    logger.warn(message, throwable)
  }

  companion object {
    private const val TAG = "ReminderLog"
  }
}
