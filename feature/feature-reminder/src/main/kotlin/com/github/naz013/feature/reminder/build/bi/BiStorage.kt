package com.github.naz013.feature.reminder.build.bi

internal abstract class BiStorage<T>(
  var value: T? = null,
) {
  override fun toString(): String = "BiStorage(value=$value)"
}

internal open class DefaultBiStorage<T>(
  initValue: T? = null,
) : BiStorage<T>(initValue)
