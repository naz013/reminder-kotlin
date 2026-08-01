package com.elementary.tasks.core.utils

import android.app.NotificationManager
import android.content.Context
import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.reminder.v2.LockScreenVisibility
import com.github.naz013.domain.reminder.v2.NotificationSettings
import com.github.naz013.domain.reminder.v2.ReminderNotificationCategory
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.feature.common.android.SystemServiceProvider
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

class NotifierTest : BaseTest() {
  private val context = mockk<Context>(relaxed = true)
  private val prefs = mockk<Prefs>(relaxed = true)
  private val dateTimeManager = mockk<DateTimeManager>()
  private val systemServiceProvider = mockk<SystemServiceProvider>()
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val birthdayRepository = mockk<BirthdayRepository>()
  private val modelDateTimeFormatter = mockk<ModelDateTimeFormatter>()
  private val notificationManager = mockk<NotificationManager>(relaxed = true)

  private lateinit var notifier: Notifier

  private fun settings(
    vibrate: Boolean = true,
    vibrationPattern: List<Long>? = listOf(0L, 250L),
    priority: ReminderPriority = ReminderPriority.NORMAL,
    bypassDoNotDisturb: Boolean = false,
  ) = NotificationSettings(
    vibrate = vibrate,
    vibrationPattern = vibrationPattern,
    priority = priority,
    bypassDoNotDisturb = bypassDoNotDisturb,
    category = ReminderNotificationCategory.DEFAULT,
    lockScreenVisibility = LockScreenVisibility.PRIVATE,
  )

  @Before
  override fun setUp() {
    super.setUp()
    every { systemServiceProvider.provideNotificationManager() } returns notificationManager
    every { notificationManager.getNotificationChannel(any()) } returns null
    notifier = Notifier(
      context = context,
      prefs = prefs,
      dateTimeManager = dateTimeManager,
      systemServiceProvider = systemServiceProvider,
      reminderV2Repository = reminderV2Repository,
      birthdayRepository = birthdayRepository,
      modelDateTimeFormatter = modelDateTimeFormatter,
    )
  }

  @Test
  fun `reminderChannelId returns the same id for the same settings`() {
    val id1 = notifier.reminderChannelId(settings())
    val id2 = notifier.reminderChannelId(settings())

    assertEquals(id1, id2)
  }

  @Test
  fun `reminderChannelId differs when vibrate differs`() {
    val id1 = notifier.reminderChannelId(settings(vibrate = true))
    val id2 = notifier.reminderChannelId(settings(vibrate = false))

    assertNotEquals(id1, id2)
  }

  @Test
  fun `reminderChannelId differs when the vibration pattern differs`() {
    val id1 = notifier.reminderChannelId(settings(vibrationPattern = listOf(0L, 250L)))
    val id2 = notifier.reminderChannelId(settings(vibrationPattern = listOf(0L, 800L)))

    assertNotEquals(id1, id2)
  }

  @Test
  fun `reminderChannelId differs when priority differs`() {
    val id1 = notifier.reminderChannelId(settings(priority = ReminderPriority.NORMAL))
    val id2 = notifier.reminderChannelId(settings(priority = ReminderPriority.HIGH))

    assertNotEquals(id1, id2)
  }

  @Test
  fun `reminderChannelId differs when bypassDoNotDisturb differs`() {
    val id1 = notifier.reminderChannelId(settings(bypassDoNotDisturb = false))
    val id2 = notifier.reminderChannelId(settings(bypassDoNotDisturb = true))

    assertNotEquals(id1, id2)
  }
}
