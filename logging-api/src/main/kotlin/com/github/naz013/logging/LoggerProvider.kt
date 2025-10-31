package com.github.naz013.logging

interface LoggerProvider {
  fun info(tag: String, message: String)
  fun debug(tag: String, message: String)
  fun error(tag: String, message: String)
  fun error(tag: String, message: String, throwable: Throwable)
  fun warning(tag: String, message: String)
  fun warning(tag: String, message: String, throwable: Throwable)
}
