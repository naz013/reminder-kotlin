package com.github.naz013.feature.home.scheduleview

import com.github.naz013.datecalc.DateTimeManager
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart

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
) {
  @OptIn(ExperimentalCoroutinesApi::class)
  operator fun invoke(): Flow<List<TodayItem>> {
    val changes = merge(
      reminderV2Repository.observeByRemovedStatus(false).map { },
      birthdayRepository.observeAll().map { },
      googleTaskRepository.observeAll().map { },
    ).onStart { emit(Unit) }
    return changes.flatMapLatest { scheduleWithTicker() }
  }

  private fun scheduleWithTicker(): Flow<List<TodayItem>> = flow {
    var items = load()
    emit(items)
    while (items.any { it.bucket == TodayBucket.Timed }) {
      delay(TICK_MILLIS)
      items = load()
      emit(items)
    }
  }

  private suspend fun load(): List<TodayItem> {
    val now = dateTimeManager.getCurrentDateTime()
    Logger.d(TAG, "Loading today schedule for $now")
    return getTodayScheduleUseCase(now)
  }

  companion object {
    private const val TAG = "ObserveTodayScheduleUseCase"
    internal const val TICK_MILLIS = 60_000L
  }
}
