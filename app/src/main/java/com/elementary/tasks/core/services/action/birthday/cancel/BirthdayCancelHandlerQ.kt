package com.elementary.tasks.core.services.action.birthday.cancel

import android.os.Build
import androidx.annotation.RequiresApi
import com.elementary.tasks.birthdays.work.SingleBackupWorker
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.repository.BirthdayRepository
import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.work.WorkerLauncher
import org.threeten.bp.LocalDate

@RequiresApi(Build.VERSION_CODES.Q)
class BirthdayCancelHandlerQ(
  private val notifier: Notifier,
  private val birthdayRepository: BirthdayRepository,
  private val dateTimeManager: DateTimeManager,
  private val workerLauncher: WorkerLauncher,
  private val updatesHelper: UpdatesHelper
) : ActionHandler<Birthday> {

  override fun handle(data: Birthday) {
    birthdayRepository.save(
      data.copy(
        updatedAt = dateTimeManager.getNowGmtDateTime(),
        showedYear = LocalDate.now().year
      )
    )
    notifier.showBirthdayPermanent()
    notifier.cancel(data.uniqueId)
    workerLauncher.startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, data.uuId)
    updatesHelper.updateWidgets()
    updatesHelper.updateCalendarWidget()
  }
}
