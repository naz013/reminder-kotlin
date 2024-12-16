package com.github.naz013.logging

interface LoggerProvider {
  fun info(message: String)
  fun debug(message: String)
  fun error(message: String)
  fun error(message: String, throwable: Throwable)
  fun warning(message: String)
  fun warning(message: String, throwable: Throwable)
}
