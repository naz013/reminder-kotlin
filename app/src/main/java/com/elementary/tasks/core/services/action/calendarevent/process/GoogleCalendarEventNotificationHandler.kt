package com.elementary.tasks.core.services.action.calendarevent.process

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.services.GoogleCalendarEventActionReceiver
import com.elementary.tasks.navigation.BottomNavActivity
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.common.intent.PendingIntentWrapper
import com.github.naz013.domain.GoogleCalendarEvent
import com.github.naz013.logic.notificationaction.NotificationAction
import com.github.naz013.logic.notificationaction.NotificationAlertActionHandler
import com.github.naz013.logic.notificationaction.NotificationGateway
import com.github.naz013.logic.notificationaction.NotificationStyle
import com.github.naz013.logic.notificationaction.WearNotification
import com.github.naz013.logic.notificationaction.WearPreferences
import com.github.naz013.navigation.DeepLinkDestination
import com.github.naz013.navigation.ViewGoogleCalendarEventScreen

class GoogleCalendarEventNotificationHandler(
  contextProvider: ContextProvider,
  textProvider: TextProvider,
  notificationGateway: NotificationGateway,
  wearPreferences: WearPreferences,
  wearNotification: WearNotification,
  style: NotificationStyle,
) : NotificationAlertActionHandler<GoogleCalendarEvent>(
    contextProvider = contextProvider,
    textProvider = textProvider,
    notificationGateway = notificationGateway,
    wearPreferences = wearPreferences,
    wearNotification = wearNotification,
    style = style,
  ) {

  override val groupKey = "calendar_event"
  override val logTag = "GoogleCalendarEventNotificationHandler"

  override fun receiverClass(): Class<out BroadcastReceiver> = GoogleCalendarEventActionReceiver::class.java

  override fun dismissActionKey(): String = GoogleCalendarEventActionReceiver.ACTION_HIDE

  override fun isOngoing(data: GoogleCalendarEvent): Boolean = false

  override fun extraActions(data: GoogleCalendarEvent): List<NotificationAction> = emptyList()

  override fun uniqueId(data: GoogleCalendarEvent): Int = data.uniqueId

  override fun uuId(data: GoogleCalendarEvent): String = data.uuId

  override fun contentTitle(data: GoogleCalendarEvent): String = data.title

  override fun contentText(data: GoogleCalendarEvent): String? = data.calendarName.ifEmpty { null }

  override fun domainIcon(data: GoogleCalendarEvent): Int = R.drawable.ic_fluent_calendar

  override fun channelId(data: GoogleCalendarEvent): String = NotificationGateway.CHANNEL_CALENDAR_EVENT

  override fun defaultPriority(data: GoogleCalendarEvent): Int = NotificationCompat.PRIORITY_DEFAULT

  override fun vibrationPattern(data: GoogleCalendarEvent): LongArray? = null

  override fun ledColor(data: GoogleCalendarEvent): Int? = null

  override fun appName(data: GoogleCalendarEvent): String = textProvider.getString(R.string.google_calendar_event)

  override fun contentPendingIntent(data: GoogleCalendarEvent): PendingIntent =
    PendingIntentWrapper.getActivity(
      contextProvider.context,
      data.uniqueId,
      Intent(contextProvider.context, BottomNavActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .setAction(Intent.ACTION_VIEW)
        .putExtra(DeepLinkDestination.KEY, ViewGoogleCalendarEventScreen(data.uuId) as DeepLinkDestination),
      PendingIntent.FLAG_CANCEL_CURRENT,
    )
}
