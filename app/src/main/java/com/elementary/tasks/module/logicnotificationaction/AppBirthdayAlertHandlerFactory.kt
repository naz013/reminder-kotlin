package com.elementary.tasks.module.logicnotificationaction

import com.elementary.tasks.core.services.action.birthday.BirthdayDataProvider
import com.elementary.tasks.core.services.action.birthday.process.BirthdayNotificationHandler
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.domain.Birthday
import com.github.naz013.logic.birthday.BirthdayPreferences
import com.github.naz013.logic.notificationaction.ActionHandler
import com.github.naz013.logic.notificationaction.LoudNotificationStyle
import com.github.naz013.logic.notificationaction.NotificationGateway
import com.github.naz013.logic.notificationaction.SilentNotificationStyle
import com.github.naz013.logic.notificationaction.WearNotification
import com.github.naz013.logic.notificationaction.WearPreferences
import com.github.naz013.logic.notificationaction.birthday.BirthdayAlertHandlerFactory
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter

/**
 * `logic-notification-action` needs the concrete "show the birthday alert" handler to target
 * `app`-only classes (`BirthdayActionReceiver`, `BirthdayActionActivity`, via
 * `BirthdayNotificationHandler`), so this factory - the only place that construction happens -
 * lives in `app` and implements the module's [BirthdayAlertHandlerFactory] seam.
 */
class AppBirthdayAlertHandlerFactory(
  private val birthdayDataProvider: BirthdayDataProvider,
  private val contextProvider: ContextProvider,
  private val textProvider: TextProvider,
  private val notificationGateway: NotificationGateway,
  private val wearPreferences: WearPreferences,
  private val birthdayPreferences: BirthdayPreferences,
  private val wearNotification: WearNotification,
  private val modelDateTimeFormatter: ModelDateTimeFormatter,
  private val buildInfo: BuildInfo,
) : BirthdayAlertHandlerFactory {
  override fun create(canPlaySound: Boolean): ActionHandler<Birthday> =
    BirthdayNotificationHandler(
      birthdayDataProvider = birthdayDataProvider,
      contextProvider = contextProvider,
      textProvider = textProvider,
      notificationGateway = notificationGateway,
      wearPreferences = wearPreferences,
      birthdayPreferences = birthdayPreferences,
      wearNotification = wearNotification,
      modelDateTimeFormatter = modelDateTimeFormatter,
      style = if (canPlaySound) LoudNotificationStyle else SilentNotificationStyle,
      buildInfo = buildInfo,
    )
}
