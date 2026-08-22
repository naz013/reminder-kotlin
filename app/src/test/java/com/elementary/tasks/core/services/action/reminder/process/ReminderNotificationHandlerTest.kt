package com.elementary.tasks.core.services.action.reminder.process

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.services.action.reminder.ReminderDataProvider
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.reminder.v2.LockScreenVisibility
import com.github.naz013.domain.reminder.v2.NotificationSettings
import com.github.naz013.domain.reminder.v2.ReminderNotificationCategory
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.notificationaction.LoudNotificationStyle
import com.github.naz013.logic.notificationaction.NotificationGateway
import com.github.naz013.logic.notificationaction.WearNotification
import com.github.naz013.logic.notificationaction.WearPreferences
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.notification.NotificationApi
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ReminderNotificationHandlerTest : BaseTest() {
  private val reminderDataProvider = mockk<ReminderDataProvider>()
  private val contextProvider = mockk<ContextProvider>()
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val notificationGateway = mockk<NotificationGateway>()
  private val wearPreferences = mockk<WearPreferences>(relaxed = true)
  private val notificationApi = mockk<NotificationApi>()
  private val reminderPreferences = mockk<ReminderPreferences>(relaxed = true)
  private val wearNotification = mockk<WearNotification>()

  private fun settings(
    vibrate: Boolean = true,
    vibrationPattern: List<Long>? = listOf(0L, 250L),
    priority: ReminderPriority = ReminderPriority.HIGH,
    color: Int = 5,
  ) = NotificationSettings(
    vibrate = vibrate,
    vibrationPattern = vibrationPattern,
    priority = priority,
    color = color,
    category = ReminderNotificationCategory.DEFAULT,
    lockScreenVisibility = LockScreenVisibility.PRIVATE,
  )

  private fun handler(notificationSettings: NotificationSettings) =
    ReminderNotificationHandler(
      reminderDataProvider = reminderDataProvider,
      notificationSettings = notificationSettings,
      contextProvider = contextProvider,
      textProvider = textProvider,
      notificationGateway = notificationGateway,
      wearPreferences = wearPreferences,
      notificationApi = notificationApi,
      reminderPreferences = reminderPreferences,
      wearNotification = wearNotification,
      style = LoudNotificationStyle,
    )

  private val reminder = ReminderV2(schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))

  @Test
  fun `channelId resolves a channel derived from the notification settings`() {
    val settings = settings()
    every { notificationApi.reminderChannelId(settings) } returns "reminder.channel.events.abc123"

    val id = handler(settings).channelId(reminder)

    assertEquals("reminder.channel.events.abc123", id)
  }

  @Test
  fun `defaultPriority maps the resolved priority through the reminder data provider`() {
    every { reminderDataProvider.priority(ReminderPriority.HIGH.ordinal) } returns 3

    val result = handler(settings(priority = ReminderPriority.HIGH)).defaultPriority(reminder)

    assertEquals(3, result)
  }

  @Test
  fun `vibrationPattern returns the resolved pattern when vibrate is on`() {
    val pattern = listOf(0L, 200L, 150L, 200L)

    val result = handler(settings(vibrate = true, vibrationPattern = pattern)).vibrationPattern(reminder)

    assertEquals(pattern, result?.toList())
  }

  @Test
  fun `vibrationPattern returns null when vibrate is off, regardless of a stored pattern`() {
    val result = handler(settings(vibrate = false, vibrationPattern = listOf(0L, 250L))).vibrationPattern(reminder)

    assertNull(result)
  }

  @Test
  fun `ledColor reads the resolved color, not a reminder-only override`() {
    every { reminderDataProvider.getLedColor(5) } returns 5

    val result = handler(settings(color = 5)).ledColor(reminder)

    assertEquals(5, result)
  }
}
