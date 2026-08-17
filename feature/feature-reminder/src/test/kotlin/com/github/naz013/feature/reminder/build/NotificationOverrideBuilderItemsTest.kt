package com.github.naz013.feature.reminder.build

import com.github.naz013.domain.reminder.v2.LockScreenVisibility
import com.github.naz013.domain.reminder.v2.ReminderNotificationCategory
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

class NotificationOverrideBuilderItemsTest {

  private fun baseReminder() = ReminderV2(schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))

  @Test
  fun `category builder item writes the selected category into the notification override`() {
    val item = CategoryBuilderItem(title = "c", description = null, categoryFormatter = mockk(relaxed = true))
    item.modifier.update(ReminderNotificationCategory.ALARM.ordinal)

    val result = item.modifier.putInto(baseReminder())

    assertEquals(ReminderNotificationCategory.ALARM, result.notification.category)
  }

  @Test
  fun `lock screen visibility builder item writes the selected visibility into the notification override`() {
    val item = LockScreenVisibilityBuilderItem(
      title = "l",
      description = null,
      lockScreenVisibilityFormatter = mockk(relaxed = true),
    )
    item.modifier.update(LockScreenVisibility.SECRET.ordinal)

    val result = item.modifier.putInto(baseReminder())

    assertEquals(LockScreenVisibility.SECRET, result.notification.lockScreenVisibility)
  }

  @Test
  fun `bypass dnd builder item writes the flag into the notification override`() {
    val item = BypassDndBuilderItem(title = "b", description = null, bypassDndFormatter = mockk(relaxed = true))
    item.modifier.update(true)

    val result = item.modifier.putInto(baseReminder())

    assertEquals(true, result.notification.bypassDoNotDisturb)
  }

  @Test
  fun `wake screen builder item writes the flag into the notification override`() {
    val item = WakeScreenBuilderItem(title = "w", description = null, wakeScreenFormatter = mockk(relaxed = true))
    item.modifier.update(true)

    val result = item.modifier.putInto(baseReminder())

    assertEquals(true, result.notification.wakeScreen)
  }

  @Test
  fun `vibration pattern builder item writes the selected pattern into the notification override`() {
    val item = VibrationPatternBuilderItem(
      title = "v",
      description = null,
      vibrationPatternFormatter = mockk(relaxed = true),
    )
    val pattern = listOf(0L, 200L, 150L, 200L)
    item.modifier.update(pattern)

    val result = item.modifier.putInto(baseReminder())

    assertEquals(pattern, result.notification.vibrationPattern)
  }

  @Test
  fun `delay minutes builder item writes the minute count into the notification override`() {
    val item = DelayMinutesBuilderItem(title = "d", description = null, delayMinutesFormatter = mockk(relaxed = true))
    item.modifier.update(15)

    val result = item.modifier.putInto(baseReminder())

    assertEquals(15, result.notification.delayMinutes)
  }

  @Test
  fun `builder items leave the notification override unchanged when no value was set`() {
    val items = listOf(
      CategoryBuilderItem(title = "c", description = null, categoryFormatter = mockk(relaxed = true)),
      LockScreenVisibilityBuilderItem(
        title = "l",
        description = null,
        lockScreenVisibilityFormatter = mockk(relaxed = true),
      ),
      BypassDndBuilderItem(title = "b", description = null, bypassDndFormatter = mockk(relaxed = true)),
      WakeScreenBuilderItem(title = "w", description = null, wakeScreenFormatter = mockk(relaxed = true)),
      VibrationPatternBuilderItem(title = "v", description = null, vibrationPatternFormatter = mockk(relaxed = true)),
      DelayMinutesBuilderItem(title = "d", description = null, delayMinutesFormatter = mockk(relaxed = true)),
    )
    items.forEach { it.modifier.update(null) }

    val base = baseReminder()
    items.forEach { item ->
      assertEquals(base.notification, item.modifier.putInto(base).notification)
    }
  }
}
