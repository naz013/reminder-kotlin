package com.elementary.tasks.core.services.action.birthday.process

import android.app.PendingIntent
import android.content.BroadcastReceiver
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.dialog.BirthdayActionActivity
import com.elementary.tasks.core.services.BirthdayActionReceiver
import com.elementary.tasks.core.services.action.NotificationAction
import com.elementary.tasks.core.services.action.NotificationAlertActionHandler
import com.elementary.tasks.core.services.action.NotificationStyle
import com.elementary.tasks.core.services.action.WearNotification
import com.elementary.tasks.core.services.action.birthday.BirthdayDataProvider
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.common.intent.PendingIntentWrapper
import com.github.naz013.domain.Birthday
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter

class BirthdayNotificationHandler(
  private val birthdayDataProvider: BirthdayDataProvider,
  contextProvider: ContextProvider,
  textProvider: TextProvider,
  notifier: Notifier,
  private val prefs: Prefs,
  wearNotification: WearNotification,
  private val modelDateTimeFormatter: ModelDateTimeFormatter,
  style: NotificationStyle,
) : NotificationAlertActionHandler<Birthday>(
    contextProvider = contextProvider,
    textProvider = textProvider,
    notifier = notifier,
    prefs = prefs,
    wearNotification = wearNotification,
    style = style,
  ) {

  override val groupKey = "birthday"
  override val logTag = "BirthdayNotificationHandler"

  override fun receiverClass(): Class<out BroadcastReceiver> = BirthdayActionReceiver::class.java

  override fun dismissActionKey(): String = BirthdayActionReceiver.ACTION_HIDE

  override fun extraActions(data: Birthday): List<NotificationAction> =
    if (data.number.isNotEmpty()) {
      listOf(
        NotificationAction(
          icon = R.drawable.ic_fluent_phone,
          label = textProvider.getText(R.string.make_call),
          actionKey = BirthdayActionReceiver.ACTION_CALL,
        ),
        NotificationAction(
          icon = R.drawable.ic_fluent_chat,
          label = textProvider.getText(R.string.send_sms),
          actionKey = BirthdayActionReceiver.ACTION_SMS,
        ),
      )
    } else {
      emptyList()
    }

  override fun uniqueId(data: Birthday): Int = data.uniqueId

  override fun uuId(data: Birthday): String = data.uuId

  override fun contentTitle(data: Birthday): String = data.name

  override fun contentText(data: Birthday): String? =
    if (!data.ignoreYear) modelDateTimeFormatter.getAgeFormatted(data.date) else null

  override fun domainIcon(data: Birthday): Int = R.drawable.ic_fluent_food_cake

  override fun defaultPriority(data: Birthday): Int = birthdayDataProvider.priority(prefs.birthdayPriority)

  override fun vibrationPattern(data: Birthday): LongArray? = birthdayDataProvider.getVibrationPattern()

  override fun ledColor(data: Birthday): Int? =
    if (BuildParams.isPro && birthdayDataProvider.isBirthdayLed()) {
      birthdayDataProvider.getLedColor()
    } else {
      null
    }

  override fun appName(data: Birthday): String = birthdayDataProvider.getAppName()

  override fun contentPendingIntent(data: Birthday): PendingIntent =
    PendingIntentWrapper.getActivity(
      contextProvider.context,
      data.uniqueId,
      BirthdayActionActivity.getLaunchIntent(contextProvider.context, data.uuId),
      PendingIntent.FLAG_CANCEL_CURRENT,
    )
}
