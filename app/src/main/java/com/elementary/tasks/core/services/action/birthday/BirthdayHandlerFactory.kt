package com.elementary.tasks.core.services.action.birthday

import com.elementary.tasks.core.services.action.birthday.process.BirthdayNotificationHandler
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.logic.birthday.SaveBirthdayUseCase
import com.github.naz013.logic.notificationaction.ActionHandler
import com.github.naz013.logic.notificationaction.CancelNotificationDecorator
import com.github.naz013.logic.notificationaction.LoudNotificationStyle
import com.github.naz013.logic.notificationaction.NotificationGateway
import com.github.naz013.logic.notificationaction.SilentNotificationStyle
import com.github.naz013.logic.notificationaction.WearNotification
import com.github.naz013.logic.notificationaction.WearPreferences
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import org.threeten.bp.LocalDate

class BirthdayHandlerFactory(
  private val birthdayDataProvider: BirthdayDataProvider,
  private val contextProvider: ContextProvider,
  private val textProvider: TextProvider,
  private val notificationGateway: NotificationGateway,
  private val wearPreferences: WearPreferences,
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
      notificationGateway = notificationGateway,
      wearPreferences = wearPreferences,
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
      notificationGateway = notificationGateway,
      uniqueId = Birthday::uniqueId,
    )
}
