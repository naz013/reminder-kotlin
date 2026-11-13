package com.github.naz013.logic.reminder.snooze

/**
 * Quick snooze choices. [relativeMinutes] is set for presets measured from now; presets without it
 * resolve to a wall-clock time configured in preferences.
 */
enum class SnoozePresetType(
  val relativeMinutes: Int? = null,
) {
  MINUTES_15(15),
  HOUR_1(60),
  HOURS_3(180),
  THIS_EVENING,
  TOMORROW_MORNING,
  ;

  companion object {
    val DEFAULT_ENABLED: List<SnoozePresetType> = entries.toList()

    fun parseList(raw: String?): List<SnoozePresetType> {
      if (raw == null) return DEFAULT_ENABLED
      return raw.split(SEPARATOR)
        .mapNotNull { name -> entries.firstOrNull { it.name == name.trim() } }
        .distinct()
    }

    fun serialize(presets: Collection<SnoozePresetType>): String =
      presets.distinct().joinToString(SEPARATOR) { it.name }

    private const val SEPARATOR = ","
  }
}
