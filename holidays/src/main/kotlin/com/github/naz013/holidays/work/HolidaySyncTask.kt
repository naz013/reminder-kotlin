package com.github.naz013.holidays.work

import com.github.naz013.holidays.remote.HolidayFirestoreDataSource
import com.github.naz013.holidaysapi.HolidaySettingsGate
import com.github.naz013.logging.Logger
import com.github.naz013.repository.HolidayRepository
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult
import org.threeten.bp.LocalDate

/**
 * Fetches current + next year of holidays for the selected country and replaces the local cache.
 * Used for both the one-shot sync (enqueued when the feature is turned on) and the periodic
 * update check (enqueued weekly while it stays on) - the work is identical either way, only the
 * [com.github.naz013.workapi.WorkScheduler] request shape differs; see [HolidaySyncSchedulerImpl].
 */
internal class HolidaySyncTask(
  private val holidaySettingsGate: HolidaySettingsGate,
  private val holidayFirestoreDataSource: HolidayFirestoreDataSource,
  private val holidayRepository: HolidayRepository,
) : BackgroundTask {
  override suspend fun run(input: TaskData, progress: TaskProgressReporter): TaskResult {
    if (!holidaySettingsGate.isEnabled()) {
      Logger.i(TASK_KEY, "Holidays feature disabled, skipping sync")
      return TaskResult.Success
    }

    val countryCode = holidaySettingsGate.countryCode()
    val currentYear = LocalDate.now().year

    var anyFailure = false
    for (year in listOf(currentYear, currentYear + 1)) {
      holidayFirestoreDataSource.fetch(countryCode, year).fold(
        onSuccess = { holidays -> holidayRepository.replaceForYear(countryCode, year, holidays) },
        onFailure = {
          Logger.e(TASK_KEY, "Failed to sync holidays for $countryCode/$year", it)
          anyFailure = true
        }
      )
    }

    return if (anyFailure) TaskResult.Retry else TaskResult.Success
  }

  companion object {
    const val TASK_KEY = "holiday_sync"
  }
}
