package com.github.naz013.feature.common

fun String.capitalizeFirstLetter(): String {
  if (this.isEmpty()) return this
  return this[0].uppercaseChar() + this.substring(1)
}
