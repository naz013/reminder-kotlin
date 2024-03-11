package com.elementary.tasks.reminder.build.bi

abstract class BiStorage<T>(
  var value: T? = null
) {

  override fun toString(): String {
    return "BiStorage(value=$value)"
  }
}

open class DefaultBiStorage<T>(initValue: T? = null) : BiStorage<T>(initValue)
