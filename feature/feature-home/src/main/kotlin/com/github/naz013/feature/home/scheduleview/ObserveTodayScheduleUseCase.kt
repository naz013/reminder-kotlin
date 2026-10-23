package com.github.naz013.feature.home.scheduleview

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.feature.common.viewmodel.PendingDeleteCoordinator
import com.github.naz013.feature.common.viewmodel.PendingEntity
import com.github.naz013.feature.common.viewmodel.pendingIdsOf
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.today.GetTodayScheduleUseCase
import com.github.naz013.logic.reminder.today.TodayBucket
import com.github.naz013.logic.reminder.today.TodayItem
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.ReminderV2Repository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoUnit

internal data class TodaySnapshot(
  val items: List<TodayItem>,
  val now: LocalDateTime,
)

/**
 * Re-queries [GetTodayScheduleUseCase] whenever reminders, birthdays or Google tasks change, and
 * once a minute while the schedule has timed items. Device calendar events have no change flow,
 * so they are only refreshed by those other triggers.
 */
internal class ObserveTodayScheduleUseCase(
  private val dateTimeManager: DateTimeManager,
  private val getTodayScheduleUseCase: GetTodayScheduleUseCase,
  private val reminderV2Repository: ReminderV2Repository,
  private val birthdayRepository: BirthdayRepository,
  private val googleTaskRepository: GoogleTaskRepository,
  private val pendingDeleteCoordinator: PendingDeleteCoordinator,
) {
  @OptIn(ExperimentalCoroutinesApi::class)
  operator fun invoke(): Flow<TodaySnapshot> {
    val changes = merge(
      reminderV2Repository.observeByRemovedStatus(false).map { },
      birthdayRepository.observeAll().map { },
      googleTaskRepository.observeAll().map { },
      pendingDeleteCoordinator.pendingIdsOf(PendingEntity.REMINDER, PendingEntity.BIRTHDAY).drop(1).map { },
    ).onStart { emit(Unit) }
    return changes
      .conflate()
      .flatMapLatest { scheduleWithTicker() }
      .distinctUntilChanged { old, new ->
        old.items == new.items && old.now.truncatedTo(ChronoUnit.MINUTES) == new.now.truncatedTo(ChronoUnit.MINUTES)
      }
  }

  private fun scheduleWithTicker(): Flow<TodaySnapshot> = flow {
    var snapshot = load()
    emit(snapshot)
    while (snapshot.items.any { it.bucket == TodayBucket.Timed }) {
      delay(TICK_MILLIS)
      snapshot = load()
      emit(snapshot)
    }
  }

  private suspend fun load(): TodaySnapshot {
    val now = dateTimeManager.getCurrentDateTime()
    Logger.d(TAG, "Loading today schedule for $now")
    val pendingReminders = pendingDeleteCoordinator.pendingIds(PendingEntity.REMINDER).value
    val pendingBirthdays = pendingDeleteCoordinator.pendingIds(PendingEntity.BIRTHDAY).value
    val items = getTodayScheduleUseCase(now).filterNot { item ->
      (item is TodayItem.Reminder && item.id in pendingReminders) ||
        (item is TodayItem.Birthday && item.id in pendingBirthdays)
    }
    return TodaySnapshot(items, now)
  }

  companion object {
    private const val TAG = "ObserveTodayScheduleUseCase"
    internal const val TICK_MILLIS = 60_000L
  }
}
