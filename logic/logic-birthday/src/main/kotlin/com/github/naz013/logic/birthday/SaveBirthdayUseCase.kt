package com.github.naz013.logic.birthday

import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.files.DataType
import com.github.naz013.logging.Logger
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.workapi.WorkScheduler

class SaveBirthdayUseCase(
  private val birthdayRepository: BirthdayRepository,
  private val birthdayNotifier: BirthdayNotifier,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase,
  private val workScheduler: WorkScheduler,
) {
  suspend operator fun invoke(birthday: Birthday) {
    birthdayRepository.save(birthday.copy(version = birthday.version + 1))
    birthdayRepository.updateSyncState(birthday.uuId, SyncState.WaitingForUpload)

    birthdayNotifier.showBirthdayPermanent()

    appWidgetUpdater.updateBirthdaysWidget()
    appWidgetUpdater.updateScheduleWidget()

    workScheduler.enqueue(CalculateBirthdayOccurrencesTask.prepareWorkRequest(birthday.uuId))

    scheduleBackgroundWorkUseCase(
      workType = WorkType.Upload,
      dataType = DataType.Birthdays,
      id = birthday.uuId,
    )
    Logger.i(TAG, "Birthday saved: ${birthday.uuId}")
  }

  companion object {
    private const val TAG = "SaveBirthdayUseCase"
  }
}
