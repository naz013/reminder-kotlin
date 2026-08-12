package com.github.naz013.logic.birthday

import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.files.DataType
import com.github.naz013.logging.Logger
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.EventOccurrenceRepository
import com.github.naz013.repository.TagAssignmentRepository

class DeleteBirthdayUseCase(
  private val birthdayRepository: BirthdayRepository,
  private val birthdayNotifier: BirthdayNotifier,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
  private val eventOccurrenceRepository: EventOccurrenceRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
) {
  suspend operator fun invoke(id: String) {
    birthdayRepository.delete(id)
    birthdayNotifier.showBirthdayPermanent()
    appWidgetUpdater.updateScheduleWidget()
    appWidgetUpdater.updateBirthdaysWidget()
    scheduleBackgroundWorkUseCase.invoke(
      workType = WorkType.Delete,
      dataType = DataType.Birthdays,
      id = id,
    )
    eventOccurrenceRepository.deleteByEventId(id)
    tagAssignmentRepository.detachAll(id, TaggedItemType.BIRTHDAY)
    Logger.i(TAG, "Deleted birthday with id = $id")
  }

  companion object {
    private const val TAG = "DeleteBirthdayUseCase"
  }
}
