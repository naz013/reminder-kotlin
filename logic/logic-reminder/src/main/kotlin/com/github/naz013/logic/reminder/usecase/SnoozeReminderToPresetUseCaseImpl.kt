package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.logic.reminder.snooze.SnoozePresetResolver
import com.github.naz013.logic.reminder.snooze.SnoozePresetType
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.LocalTime
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeParseException

internal class SnoozeReminderToPresetUseCaseImpl(
  private val snoozeReminderUseCase: SnoozeReminderUseCase,
  private val resolver: SnoozePresetResolver,
  private val reminderPreferences: ReminderPreferences,
  private val clock: Clock = Clock.systemDefaultZone(),
) : SnoozeReminderToPresetUseCase {
  override suspend operator fun invoke(
    reminder: ReminderV2,
    preset: SnoozePresetType,
  ): ReminderV2 {
    val now = ZonedDateTime.now(clock)
    val target =
      resolver.resolveZoned(
        preset = preset,
        now = now,
        eveningTime = parseTime(reminderPreferences.snoozeEveningTime, DEFAULT_EVENING),
        morningTime = parseTime(reminderPreferences.snoozeMorningTime, DEFAULT_MORNING),
      )
    val millis = Duration.between(now, target).toMillis()
    val minutes = ((millis + MILLIS_IN_MINUTE - 1) / MILLIS_IN_MINUTE).toInt().coerceAtLeast(1)
    Logger.i(TAG, "Snoozing reminder id=${reminder.uuId} to preset=$preset, target=$target")
    return snoozeReminderUseCase(reminder, minutes)
  }

  private fun parseTime(
    value: String,
    fallback: LocalTime,
  ): LocalTime =
    try {
      LocalTime.parse(value)
    } catch (e: DateTimeParseException) {
      Logger.w(TAG, "Invalid snooze preset time '$value', using $fallback")
      fallback
    }

  companion object {
    private const val TAG = "SnoozeReminderToPreset"
    private const val MILLIS_IN_MINUTE = 60_000L
    val DEFAULT_EVENING: LocalTime = LocalTime.of(18, 0)
    val DEFAULT_MORNING: LocalTime = LocalTime.of(8, 0)
  }
}
