package com.github.naz013.feature.reminder.build.bi

abstract class BiStorage<T>(
  var value: T? = null,
) {
  override fun toString(): String = "BiStorage(value=$value)"
}

open class DefaultBiStorage<T>(
  initValue: T? = null,
) : BiStorage<T>(initValue)
