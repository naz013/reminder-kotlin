package com.github.naz013.logic.reminder.usecase

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.logic.reminder.fixedZone
import com.github.naz013.repository.ReminderV2Repository
import org.threeten.bp.ZoneId

/**
 * Compares the device zone with the last known one and, when it changed, re-anchors active floating reminders so
 * their wall-clock time is preserved (the instant moves). Fixed-zone reminders keep their instant.
 *
 * Only persists the new schedule; the caller must re-run activation afterwards to reschedule alarms.
 *
 * @return true when the zone changed and reminders were re-anchored.
 */
class HandleZoneChangeUseCase(
  private val reminderRepository: ReminderV2Repository,
  private val saveReminderUseCase: SaveReminderUseCase,
  private val dateTimeManager: DateTimeManager,
  private val reminderPreferences: ReminderPreferences,
) {
  suspend operator fun invoke(): Boolean {
    val newZone = ZoneId.systemDefault()
    val oldZone = reminderPreferences.lastKnownZoneId?.let(::parseZone)
    reminderPreferences.lastKnownZoneId = newZone.id
    if (oldZone == null) {
      Logger.i(TAG, "Initialised last known zone to ${newZone.id}")
      return false
    }
    if (oldZone == newZone) return false

    Logger.i(TAG, "Zone changed from ${oldZone.id} to ${newZone.id}")
    val floating = reminderRepository.getAll(active = true, removed = false).filter { it.isFloatingTimeBased() }
    floating.forEach { reanchor(it, oldZone, newZone) }
    Logger.i(TAG, "Re-anchored ${floating.size} floating reminder(s)")
    return true
  }

  private suspend fun reanchor(
    reminder: ReminderV2,
    oldZone: ZoneId,
    newZone: ZoneId,
  ) {
    val old = reminder.schedule.eventDateTime ?: return
    val wallClock = dateTimeManager.utcToLocal(old, oldZone)
    val anchored =
      reminder.copy(
        schedule = reminder.schedule.copy(eventDateTime = dateTimeManager.localToUtc(wallClock, newZone)),
        sync = reminder.sync.copy(version = reminder.sync.version + 1, syncState = SyncState.WaitingForUpload),
      )
    saveReminderUseCase(anchored)
  }

  private fun parseZone(id: String): ZoneId? =
    try {
      ZoneId.of(id)
    } catch (e: Exception) {
      Logger.w(TAG, "Unknown stored zone id: $id")
      null
    }

  private fun ReminderV2.isFloatingTimeBased(): Boolean =
    fixedZone() == null &&
      schedule.eventDateTime != null &&
      places.isEmpty() &&
      recurrence !is RecurrenceRule.Countdown &&
      recurrence !is RecurrenceRule.LocationEnter &&
      recurrence !is RecurrenceRule.LocationExit

  companion object {
    private const val TAG = "HandleZoneChangeUseCase"
  }
}
