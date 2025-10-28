package com.elementary.tasks.core.services.action.birthday

import com.elementary.tasks.birthdays.usecase.SaveBirthdayUseCase
import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.services.action.WearNotification
import com.elementary.tasks.core.services.action.birthday.cancel.BirthdayCancelHandlerQ
import com.elementary.tasks.core.services.action.birthday.process.BirthdayHandlerQ
import com.elementary.tasks.core.services.action.birthday.process.BirthdayHandlerSilent
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter

class BirthdayHandlerFactory(
  private val birthdayDataProvider: BirthdayDataProvider,
  private val contextProvider: ContextProvider,
  private val textProvider: TextProvider,
  private val notifier: Notifier,
  private val prefs: Prefs,
  private val dateTimeManager: DateTimeManager,
  private val wearNotification: WearNotification,
  private val modelDateTimeFormatter: ModelDateTimeFormatter,
  private val saveBirthdayUseCase: SaveBirthdayUseCase
) {

  fun createAction(canPlaySound: Boolean): ActionHandler<Birthday> {
    return if (canPlaySound) {
      BirthdayHandlerQ(
        birthdayDataProvider,
        contextProvider,
        textProvider,
        notifier,
        prefs,
        wearNotification,
        modelDateTimeFormatter
      )
    } else {
      BirthdayHandlerSilent(
        birthdayDataProvider,
        contextProvider,
        textProvider,
        notifier,
        prefs,
        wearNotification,
        modelDateTimeFormatter
      )
    }
  }

  fun createCancel(): ActionHandler<Birthday> {
    return BirthdayCancelHandlerQ(
      notifier,
      dateTimeManager,
      saveBirthdayUseCase
    )
  }
}
