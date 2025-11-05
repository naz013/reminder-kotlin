package com.elementary.tasks.birthdays.usecase

import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.elementary.tasks.core.utils.Notifier
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.EventOccurrenceRepository
import com.github.naz013.sync.DataType

class DeleteBirthdayUseCase(
  private val birthdayRepository: BirthdayRepository,
  private val notifier: Notifier,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
  private val eventOccurrenceRepository: EventOccurrenceRepository
) {

  suspend operator fun invoke(id: String) {
    birthdayRepository.delete(id)
    notifier.showBirthdayPermanent()
    appWidgetUpdater.updateScheduleWidget()
    appWidgetUpdater.updateBirthdaysWidget()
    scheduleBackgroundWorkUseCase.invoke(
      workType = WorkType.Delete,
      dataType = DataType.Birthdays,
      id = id
    )
    eventOccurrenceRepository.deleteByEventId(id)
    Logger.i(TAG, "Deleted birthday with id = $id")
  }

  companion object {
    private const val TAG = "DeleteBirthdayUseCase"
  }
}
