package com.github.naz013.feature.common

fun List<String>.append(): String {
  val stringBuilder = StringBuilder()
  for (string in this) {
    stringBuilder.append(string)
  }
  return stringBuilder.toString()
}

fun listOfNotEmpty(vararg items: String?): List<String> = items.filterNotNull().filterNotEmpty()

fun List<String?>.filterNotEmpty() = filterNotNull().filter { it.isNotEmpty() }
