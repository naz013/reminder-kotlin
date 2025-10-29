package com.elementary.tasks.birthdays.usecase

import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.elementary.tasks.core.utils.Notifier
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.sync.DataType

class SaveBirthdayUseCase(
  private val birthdayRepository: BirthdayRepository,
  private val notifier: Notifier,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
) {

  suspend operator fun invoke(birthday: Birthday) {
    birthdayRepository.save(birthday.copy(version = birthday.version + 1))
    birthdayRepository.updateSyncState(birthday.uuId, SyncState.WaitingForUpload)
    notifier.showBirthdayPermanent()
    appWidgetUpdater.updateBirthdaysWidget()
    appWidgetUpdater.updateScheduleWidget()
    scheduleBackgroundWorkUseCase.invoke(
      workType = WorkType.Upload,
      dataType = DataType.Birthdays,
      id = birthday.uuId
    )
    Logger.i(TAG, "Birthday saved: ${birthday.uuId}")
  }

  companion object {
    private const val TAG = "DeleteBirthdayUseCase"
  }
}
