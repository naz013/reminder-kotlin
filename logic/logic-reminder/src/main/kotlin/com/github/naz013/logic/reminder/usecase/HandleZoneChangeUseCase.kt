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
 * Crash safe via an intent log: before shifting anything, the new zone is stored as last known and every
 * candidate id is written to [ReminderPreferences.pendingZoneReanchor] together with the zone it is anchored to.
 * An id is removed right after its save succeeds. A later run retries what is left from the recorded source zone.
 * If a save throws, the row is re-read and counted as done when it already holds the shifted value, so a
 * failure after the row was persisted never shifts it twice. Reminder ids must not contain ';', ',' or '='
 * (they are UUIDs).
 *
 * Inactive reminders (including ones waiting on a chain) are skipped: activation recomputes their time in the
 * current zone (`ActivateChainedRemindersUseCase.withFreshFireTime`). Pending ids that are no longer active
 * floating reminders are dropped.
 *
 * Only persists the new schedule; the caller must re-run activation when this returns true.
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

    val plan = buildPlan(oldZone, newZone, zoneChanged, pending)
    val remaining = plan.groupBy({ it.sourceZone.id }, { it.reminder.uuId }).mapValues { it.value.toMutableSet() }
    reminderPreferences.pendingZoneReanchor = serializePending(remaining)
    reminderPreferences.lastKnownZoneId = newZone.id

    var reanchored = 0
    plan.forEach { item ->
      val target = shifted(item.reminder, item.sourceZone, newZone)
      if (save(target)) {
        reanchored++
        remaining[item.sourceZone.id]?.remove(item.reminder.uuId)
        reminderPreferences.pendingZoneReanchor = serializePending(remaining)
      }
    }
    Logger.i(
      TAG,
      "Zone ${oldZone.id} -> ${newZone.id}: re-anchored $reanchored of ${plan.size}",
    )
    return true
  }

  private class PlanItem(val reminder: ReminderV2, val sourceZone: ZoneId)

  private suspend fun buildPlan(
    oldZone: ZoneId,
    newZone: ZoneId,
    zoneChanged: Boolean,
    pending: Map<String, Set<String>>,
  ): List<PlanItem> {
    val pendingSource = pending.entries.flatMap { (zone, ids) -> ids.map { it to zone } }.toMap()
    return reminderRepository
      .getAll(active = true, removed = false)
      .filter { it.isFloatingTimeBased() }
      .mapNotNull { reminder ->
        val source =
          pendingSource[reminder.uuId]?.let(::parseZone)
            ?: oldZone.takeIf { zoneChanged && reminder.uuId !in pendingSource }
        when {
          source == null || source == newZone -> null
          reminder.schedule.eventDateTime == null -> {
            Logger.w(TAG, "Skipping reminder id=${reminder.uuId} without event time")
            null
          }
          else -> PlanItem(reminder, source)
        }
      }
  }

  private fun shifted(
    reminder: ReminderV2,
    sourceZone: ZoneId,
    newZone: ZoneId,
  ): ReminderV2 {
    val old = requireNotNull(reminder.schedule.eventDateTime)
    val wallClock = dateTimeManager.utcToLocal(old, sourceZone)
    return reminder.copy(
      schedule = reminder.schedule.copy(eventDateTime = dateTimeManager.localToUtc(wallClock, newZone)),
      sync = reminder.sync.copy(version = reminder.sync.version + 1, syncState = SyncState.WaitingForUpload),
    )
  }

  private suspend fun save(target: ReminderV2): Boolean =
    try {
      saveReminderUseCase(target)
      true
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to re-anchor reminder id=${target.uuId}", e)
      val stored = reminderRepository.getById(target.uuId)
      stored?.schedule?.eventDateTime == target.schedule.eventDateTime
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
      .filter { it.value.isNotEmpty() }
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
