package com.elementary.tasks.core.services.action.birthday.cancel

import com.elementary.tasks.birthdays.work.SingleBackupWorker
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.elementary.tasks.core.services.action.ActionHandler
import com.github.naz013.common.intent.IntentKeys
import com.elementary.tasks.core.utils.Notifier
import com.github.naz013.common.datetime.DateTimeManager
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.github.naz013.domain.Birthday
import com.github.naz013.repository.BirthdayRepository
import org.threeten.bp.LocalDate

class BirthdayCancelHandlerQ(
  private val notifier: Notifier,
  private val birthdayRepository: BirthdayRepository,
  private val dateTimeManager: DateTimeManager,
  private val workerLauncher: WorkerLauncher,
  private val appWidgetUpdater: AppWidgetUpdater
) : ActionHandler<Birthday> {

  override suspend fun handle(data: Birthday) {
    birthdayRepository.save(
      data.copy(
        updatedAt = dateTimeManager.getNowGmtDateTime(),
        showedYear = LocalDate.now().year
      )
    )
    notifier.showBirthdayPermanent()
    notifier.cancel(data.uniqueId)
    workerLauncher.startWork(SingleBackupWorker::class.java, IntentKeys.INTENT_ID, data.uuId)
    appWidgetUpdater.updateAllWidgets()
    appWidgetUpdater.updateCalendarWidget()
  }
}
