package com.github.naz013.workapi

/**
 * Immutable key-value payload passed into a [BackgroundTask] as input and used to
 * report progress back to callers observing the work.
 *
 * Only types natively supported by WorkManager's `Data` are allowed (String, Boolean,
 * String array), so values placed here survive the round trip through the underlying
 * scheduler unchanged.
 */
class TaskData private constructor(
  private val values: Map<String, Any>,
) {
  fun getString(key: String): String? = values[key] as? String

  fun getBoolean(
    key: String,
    default: Boolean = false,
  ): Boolean = values[key] as? Boolean ?: default

  fun getStringArray(key: String): Array<String>? {
    @Suppress("UNCHECKED_CAST")
    return values[key] as? Array<String>
  }

  fun asMap(): Map<String, Any> = values

  class Builder {
    private val values = mutableMapOf<String, Any>()

    fun putString(
      key: String,
      value: String,
    ) = apply { values[key] = value }

    fun putBoolean(
      key: String,
      value: Boolean,
    ) = apply { values[key] = value }

    fun putStringArray(
      key: String,
      value: Array<String>,
    ) = apply { values[key] = value }

    fun build(): TaskData = TaskData(values.toMap())
  }

  companion object {
    val EMPTY = TaskData(emptyMap())

    fun builder() = Builder()

    fun of(values: Map<String, Any?>): TaskData =
      TaskData(
        values
          .filterValues { it != null }
          .mapValues { it.value as Any },
      )
  }
}
