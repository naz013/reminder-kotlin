package com.github.naz013.appwidgets

internal object RandomRequestCode {
  fun generate(): Int {
    return (Math.random() * 10000000).toInt()
  }
}
