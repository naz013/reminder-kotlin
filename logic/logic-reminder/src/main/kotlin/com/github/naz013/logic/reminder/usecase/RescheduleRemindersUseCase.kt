package com.github.naz013.logic.reminder.usecase

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.behavior.BehaviorStrategyResolverV2
import com.github.naz013.logic.reminder.behavior.LocationBasedStrategyV2
import com.github.naz013.logic.reminder.behavior.NoReminderStrategyV2
import com.github.naz013.logic.reminder.behavior.ReminderBehaviorStrategyV2
import com.github.naz013.logic.reminder.reminderLocalToUtc
import com.github.naz013.logic.reminder.reminderNow
import com.github.naz013.logic.reminder.reminderUtcToLocal
import com.github.naz013.repository.ReminderV2Repository
import org.threeten.bp.LocalDateTime

/**
 * Moves overdue reminders to a new time in bulk: one-time reminders get the target time, recurring ones are
 * skipped forward to their first future occurrence. Unsupported reminders are reported, never changed.
 */
class RescheduleRemindersUseCase(
  private val reminderRepository: ReminderV2Repository,
  private val strategyResolver: BehaviorStrategyResolverV2,
  private val activateReminderUseCase: ActivateReminderUseCase,
  private val dateTimeManager: DateTimeManager,
) {
  suspend operator fun invoke(
    ids: List<String>,
    target: Target,
  ): Summary {
    var moved = 0
    val skipped = mutableListOf<Skipped>()
    ids.distinct().forEach { id ->
      val reason = rescheduleOne(id, target)
      if (reason == null) {
        moved++
      } else {
        skipped.add(Skipped(id, reason))
      }
    }
    Logger.i(TAG, "Rescheduled moved=$moved, skipped=${skipped.size} to $target")
    return Summary(moved = moved, skipped = skipped)
  }

  private suspend fun rescheduleOne(
    id: String,
    target: Target,
  ): SkipReason? {
    val reminder = reminderRepository.getById(id) ?: return SkipReason.NOT_FOUND
    val strategy = strategyResolver.resolve(reminder)
    val unsupported =
      strategy === LocationBasedStrategyV2 ||
        strategy === NoReminderStrategyV2 ||
        reminder.places.isNotEmpty() ||
        reminder.recurrence is RecurrenceRule.Countdown
    return when {
      unsupported -> SkipReason.UNSUPPORTED_TYPE
      strategy.canSkip(reminder) -> skipForward(reminder, strategy)
      reminder.recurrence != RecurrenceRule.Once -> SkipReason.NOTHING_TO_MOVE
      else -> moveOneTime(reminder, target)
    }
  }

  private suspend fun moveOneTime(
    reminder: ReminderV2,
    target: Target,
  ): SkipReason? {
    val now = dateTimeManager.reminderNow(reminder)
    val newLocal = resolveTarget(reminder, target, now)
    val newUtc = dateTimeManager.reminderLocalToUtc(reminder, newLocal)
    if (!newLocal.isAfter(now)) return SkipReason.TARGET_IN_PAST
    if (newUtc == reminder.schedule.eventDateTime) return SkipReason.NOTHING_TO_MOVE
    return activateAt(reminder, newUtc)
  }

  private fun resolveTarget(
    reminder: ReminderV2,
    target: Target,
    now: LocalDateTime,
  ): LocalDateTime =
    when (target) {
      is Target.Picked -> {
        target.dateTime
      }

      Target.Today -> {
        val candidate = now.toLocalDate().atTime(timeOfDay(reminder))
        if (candidate.isAfter(now)) candidate else now.plusHours(1)
      }

      Target.Tomorrow -> {
        now.toLocalDate().plusDays(1).atTime(timeOfDay(reminder))
      }
    }

  private fun timeOfDay(reminder: ReminderV2) =
    dateTimeManager
      .reminderUtcToLocal(reminder, reminder.schedule.eventDateTime ?: reminder.schedule.startDateTime)
      .toLocalTime()

  private suspend fun skipForward(
    reminder: ReminderV2,
    strategy: ReminderBehaviorStrategyV2,
  ): SkipReason? {
    if (isInFuture(reminder)) return SkipReason.NOTHING_TO_MOVE
    val now = dateTimeManager.reminderNow(reminder)
    var from = reminder.schedule.eventDateTime
      ?.let { dateTimeManager.reminderUtcToLocal(reminder, it) }
      ?.plusSeconds(1)
      ?: now
    repeat(MAX_SKIPS) {
      val next = strategy.calculateNextOccurrence(reminder, from) ?: return SkipReason.NOTHING_TO_MOVE
      if (next.isAfter(now)) {
        return activateAt(reminder, dateTimeManager.reminderLocalToUtc(reminder, next))
      }
      from = next.plusSeconds(1)
    }
    return SkipReason.STILL_OVERDUE
  }

  private suspend fun activateAt(
    reminder: ReminderV2,
    eventUtc: LocalDateTime,
  ): SkipReason? {
    val updated = reminder.copy(schedule = reminder.schedule.copy(eventDateTime = eventUtc))
    val result = activateReminderUseCase(updated)
    return if (result.isActive && result.schedule.eventDateTime == eventUtc) null else SkipReason.NOT_ACTIVATED
  }

  private fun isInFuture(reminder: ReminderV2): Boolean {
    val event = reminder.schedule.eventDateTime ?: return false
    return dateTimeManager.reminderUtcToLocal(reminder, event).isAfter(dateTimeManager.reminderNow(reminder))
  }

  sealed class Target {
    data object Today : Target()

    data object Tomorrow : Target()

    data class Picked(val dateTime: LocalDateTime) : Target()
  }

  enum class SkipReason {
    UNSUPPORTED_TYPE,
    NOT_FOUND,
    NOTHING_TO_MOVE,
    STILL_OVERDUE,
    TARGET_IN_PAST,
    NOT_ACTIVATED,
  }

  data class Skipped(
    val id: String,
    val reason: SkipReason,
  )

  data class Summary(
    val moved: Int,
    val skipped: List<Skipped>,
  ) {
    val mostCommonSkipReason: SkipReason?
      get() = skipped.groupingBy { it.reason }.eachCount().maxByOrNull { it.value }?.key
  }

  companion object {
    private const val TAG = "RescheduleRemindersUseCase"
    private const val MAX_SKIPS = 100
  }
}
