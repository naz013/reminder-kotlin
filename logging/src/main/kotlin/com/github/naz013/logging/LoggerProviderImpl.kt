package com.github.naz013.logging

import android.util.Log
import org.slf4j.LoggerFactory

internal class LoggerProviderImpl : LoggerProvider {

  private val logger: org.slf4j.Logger = LoggerFactory.getLogger("FileLogger")

  override fun info(tag: String, message: String) {
    logger.info("$tag: $message")
    Log.i(tag, message)
  }

  override fun debug(tag: String, message: String) {
    logger.debug("$tag: $message")
    Log.d(tag, message)
  }

  override fun error(tag: String, message: String) {
    logger.error("$tag: $message")
    Log.e(tag, message)
  }

  override fun error(tag: String, message: String, throwable: Throwable) {
    logger.error("$tag: $message", throwable)
    Log.e(tag, message, throwable)
  }

  override fun warning(tag: String, message: String) {
    logger.warn("$tag: $message")
    Log.w(tag, message)
  }

  override fun warning(tag: String, message: String, throwable: Throwable) {
    logger.warn("$tag: $message", throwable)
    Log.w(tag, message, throwable)
  }
}
