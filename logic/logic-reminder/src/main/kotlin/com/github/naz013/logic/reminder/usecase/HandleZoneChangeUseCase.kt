package com.github.naz013.logic.reminder.usecase

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.logic.reminder.fixedZone
import com.github.naz013.repository.ReminderV2Repository
import kotlin.coroutines.cancellation.CancellationException
import org.threeten.bp.ZoneId

/**
 * Compares the device zone with the last known one and, when it changed, re-anchors active floating reminders so
 * their wall-clock time is preserved (the instant moves). Fixed-zone reminders keep their instant.
 *
 * Only `schedule.eventDateTime` is shifted: `startDateTime` is never used as a recurrence anchor (the strategies
 * advance from `eventDateTime`), so it keeps its creation value.
 *
 * A reminder that fails to save is remembered together with the zone it is still anchored to
 * ([ReminderPreferences.pendingZoneReanchor]) and retried on the next run, while the new zone is stored right away
 * so reminders that did succeed are never shifted twice.
 *
 * Only persists the new schedule; the caller must re-run activation when this returns true.
 * Inactive reminders (including ones waiting on a chain) are skipped: activation recomputes their time in the
 * current zone (`ActivateChainedRemindersUseCase.withFreshFireTime`).
 *
 * @return true when the zone changed or pending reminders were retried, so activation should be re-run.
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
    if (oldZone == null) {
      reminderPreferences.lastKnownZoneId = newZone.id
      Logger.i(TAG, "Initialised last known zone to ${newZone.id}")
      return false
    }
    val pending = parsePending(reminderPreferences.pendingZoneReanchor)
    val zoneChanged = oldZone != newZone
    if (!zoneChanged && pending.isEmpty()) return false

    val floating = reminderRepository.getAll(active = true, removed = false).filter { it.isFloatingTimeBased() }
    val pendingIds = pending.values.flatten().toSet()
    val failed = mutableMapOf<String, MutableSet<String>>()
    var reanchored = 0

    floating.forEach { reminder ->
      val source =
        pending.entries.firstOrNull { reminder.uuId in it.value }?.key?.let(::parseZone)
          ?: oldZone.takeIf { zoneChanged && reminder.uuId !in pendingIds }
          ?: return@forEach
      if (source == newZone) return@forEach
      if (reminder.schedule.eventDateTime == null) {
        Logger.w(TAG, "Skipping reminder id=${reminder.uuId} without event time")
        return@forEach
      }
      if (reanchor(reminder, source, newZone)) {
        reanchored++
      } else {
        failed.getOrPut(source.id) { mutableSetOf() }.add(reminder.uuId)
      }
    }

    reminderPreferences.pendingZoneReanchor = serializePending(failed)
    reminderPreferences.lastKnownZoneId = newZone.id
    Logger.i(TAG, "Zone ${oldZone.id} -> ${newZone.id}: re-anchored $reanchored, failed ${failed.values.sumOf { it.size }}")
    return true
  }

  private suspend fun reanchor(
    reminder: ReminderV2,
    sourceZone: ZoneId,
    newZone: ZoneId,
  ): Boolean {
    return try {
      val old = requireNotNull(reminder.schedule.eventDateTime)
      val wallClock = dateTimeManager.utcToLocal(old, sourceZone)
      val anchored =
        reminder.copy(
          schedule = reminder.schedule.copy(eventDateTime = dateTimeManager.localToUtc(wallClock, newZone)),
          sync = reminder.sync.copy(version = reminder.sync.version + 1, syncState = SyncState.WaitingForUpload),
        )
      saveReminderUseCase(anchored)
      true
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to re-anchor reminder id=${reminder.uuId}", e)
      false
    }
  }

  private fun parseZone(id: String): ZoneId? =
    try {
      ZoneId.of(id)
    } catch (e: Exception) {
      Logger.w(TAG, "Unknown stored zone id: $id")
      null
    }

  private fun parsePending(raw: String?): Map<String, Set<String>> =
    raw
      ?.split(";")
      ?.mapNotNull { entry ->
        val zone = entry.substringBefore("=", "")
        val ids = entry.substringAfter("=", "").split(",").filter { it.isNotBlank() }
        if (zone.isBlank() || ids.isEmpty()) null else zone to ids.toSet()
      }?.toMap()
      .orEmpty()

  private fun serializePending(failed: Map<String, Set<String>>): String? =
    failed.entries
      .joinToString(";") { "${it.key}=${it.value.joinToString(",")}" }
      .takeIf { it.isNotEmpty() }

  private fun ReminderV2.isFloatingTimeBased(): Boolean =
    fixedZone() == null &&
      places.isEmpty() &&
      recurrence !is RecurrenceRule.Countdown &&
      recurrence !is RecurrenceRule.LocationEnter &&
      recurrence !is RecurrenceRule.LocationExit

  companion object {
    private const val TAG = "HandleZoneChangeUseCase"
  }
}
