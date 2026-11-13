package com.github.naz013.logic.reminder.snooze

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZonedDateTime

/**
 * Turns a [SnoozePresetType] into the moment the reminder should fire again.
 *
 * Relative presets are added to the instant, so they stay correct across DST changes. Absolute
 * presets use wall-clock time in the zone of `now`; a time that falls in a DST gap moves forward.
 * "This evening" rolls to the next day when it has already passed, "tomorrow morning" is always
 * the next calendar day.
 */
class SnoozePresetResolver {
  fun resolve(
    preset: SnoozePresetType,
    now: ZonedDateTime,
    eveningTime: LocalTime,
    morningTime: LocalTime,
  ): LocalDateTime = resolveZoned(preset, now, eveningTime, morningTime).toLocalDateTime()

  fun resolveZoned(
    preset: SnoozePresetType,
    now: ZonedDateTime,
    eveningTime: LocalTime,
    morningTime: LocalTime,
  ): ZonedDateTime {
    val minutes = preset.relativeMinutes
    if (minutes != null) return now.plusMinutes(minutes.toLong())
    return when (preset) {
      SnoozePresetType.TOMORROW_MORNING -> at(now, now.toLocalDate().plusDays(1), morningTime)
      else -> {
        val today = at(now, now.toLocalDate(), eveningTime)
        if (today.isAfter(now)) today else at(now, now.toLocalDate().plusDays(1), eveningTime)
      }
    }
  }

  private fun at(
    now: ZonedDateTime,
    date: LocalDate,
    time: LocalTime,
  ): ZonedDateTime = ZonedDateTime.of(date, time, now.zone)
}
