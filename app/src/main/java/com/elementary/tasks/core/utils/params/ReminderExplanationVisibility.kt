package com.elementary.tasks.core.utils.params

class ReminderExplanationVisibility(
  private val prefs: Prefs
) {

  fun shouldShowExplanation(type: Type): Boolean {
    return prefs.getBoolean(getKey(type), true)
  }

  fun explanationShowed(type: Type) {
    prefs.putBoolean(getKey(type), false)
  }

  private fun getKey(type: Type): String {
    return PREFS_KEY + type.name
  }

  enum class Type {
    BY_DATE,
    BY_WEEKDAY,
    BY_TIMER,
    BY_MONTH,
    BY_YEAR,
    EMAIL,
    LINK,
    BY_LOCATION,
    BY_PLACE,
    SHOPPING,
    BY_RECUR
  }

  companion object {
    private const val PREFS_KEY = "type_explanation_"
  }
}
