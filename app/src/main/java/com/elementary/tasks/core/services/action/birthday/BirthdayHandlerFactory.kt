package com.elementary.tasks.core.services.action.birthday

import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.repository.BirthdayRepository
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.services.action.WearNotification
import com.elementary.tasks.core.services.action.birthday.cancel.BirthdayCancelHandlerQ
import com.elementary.tasks.core.services.action.birthday.process.BirthdayHandlerQ
import com.elementary.tasks.core.services.action.birthday.process.BirthdayHandlerSilent
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.work.WorkerLauncher

class BirthdayHandlerFactory(
  private val birthdayDataProvider: BirthdayDataProvider,
  private val contextProvider: ContextProvider,
  private val textProvider: TextProvider,
  private val notifier: Notifier,
  private val prefs: Prefs,
  private val birthdayRepository: BirthdayRepository,
  private val dateTimeManager: DateTimeManager,
  private val workerLauncher: WorkerLauncher,
  private val wearNotification: WearNotification,
  private val updatesHelper: UpdatesHelper
) {

  fun createAction(canPlaySound: Boolean): ActionHandler<Birthday> {
    return if (canPlaySound) {
      BirthdayHandlerQ(
        birthdayDataProvider,
        contextProvider,
        textProvider,
        notifier,
        prefs,
        dateTimeManager,
        wearNotification
      )
    } else {
      BirthdayHandlerSilent(
        birthdayDataProvider,
        contextProvider,
        textProvider,
        notifier,
        prefs,
        wearNotification,
        dateTimeManager
      )
    }
  }

  fun createCancel(): ActionHandler<Birthday> {
    return BirthdayCancelHandlerQ(
      notifier,
      birthdayRepository,
      dateTimeManager,
      workerLauncher,
      updatesHelper
    )
  }
}
