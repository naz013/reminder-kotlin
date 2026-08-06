package com.elementary.tasks.core.services.action.birthday

import com.elementary.tasks.birthdays.usecase.SaveBirthdayUseCase
import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.services.action.CancelNotificationDecorator
import com.elementary.tasks.core.services.action.LoudNotificationStyle
import com.elementary.tasks.core.services.action.SilentNotificationStyle
import com.elementary.tasks.core.services.action.WearNotification
import com.elementary.tasks.core.services.action.birthday.process.BirthdayNotificationHandler
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import org.threeten.bp.LocalDate

class BirthdayHandlerFactory(
  private val birthdayDataProvider: BirthdayDataProvider,
  private val contextProvider: ContextProvider,
  private val textProvider: TextProvider,
  private val notifier: Notifier,
  private val prefs: Prefs,
  private val dateTimeManager: DateTimeManager,
  private val wearNotification: WearNotification,
  private val modelDateTimeFormatter: ModelDateTimeFormatter,
  private val saveBirthdayUseCase: SaveBirthdayUseCase,
) {
  fun createAction(canPlaySound: Boolean): ActionHandler<Birthday> =
    BirthdayNotificationHandler(
      birthdayDataProvider = birthdayDataProvider,
      contextProvider = contextProvider,
      textProvider = textProvider,
      notifier = notifier,
      prefs = prefs,
      wearNotification = wearNotification,
      modelDateTimeFormatter = modelDateTimeFormatter,
      style = if (canPlaySound) LoudNotificationStyle else SilentNotificationStyle,
    )

  fun createCancel(): ActionHandler<Birthday> =
    CancelNotificationDecorator(
      delegate =
        ActionHandler { birthday: Birthday ->
          saveBirthdayUseCase(
            birthday.copy(
              updatedAt = dateTimeManager.getNowGmtDateTime(),
              showedYear = LocalDate.now().year,
            ),
          )
        },
      notifier = notifier,
      uniqueId = Birthday::uniqueId,
    )
}
