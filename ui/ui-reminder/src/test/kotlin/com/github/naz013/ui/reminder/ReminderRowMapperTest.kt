package com.github.naz013.ui.reminder

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ReminderRowMapperTest {
  private val dateTimeManager = mockk<DateTimeManager>()
  private val shopItemsFormatter = mockk<ShopItemsFormatter>(relaxed = true)
  private val now = LocalDateTime.of(2026, 7, 24, 12, 0)

  private lateinit var mapper: ReminderRowMapper

  @Before
  fun setUp() {
    every { dateTimeManager.getCurrentDateTime() } returns now
    every { dateTimeManager.utcToLocal(any()) } answers { firstArg() }
    mapper = ReminderRowMapper(dateTimeManager, shopItemsFormatter)
  }

  private fun reminder(
    due: LocalDateTime? = now.plusHours(1),
    isActive: Boolean = true,
    action: ReminderAction = ReminderAction.None,
  ) = ReminderV2(
    uuId = "1",
    schedule = ReminderSchedule(startDateTime = now, eventDateTime = due),
    isActive = isActive,
    action = action,
  )

  @Test
  fun `marks an active reminder due before now as overdue`() {
    val fields = mapper.map(reminder(due = now.minusHours(1)), null)

    assertTrue(fields.isOverdue)
  }

  @Test
  fun `marks an active reminder due exactly now as overdue`() {
    val fields = mapper.map(reminder(due = now), null)

    assertTrue(fields.isOverdue)
  }

  @Test
  fun `does not mark an active reminder due in the future as overdue`() {
    val fields = mapper.map(reminder(due = now.plusMinutes(1)), null)

    assertFalse(fields.isOverdue)
  }

  @Test
  fun `does not mark an inactive past reminder as overdue`() {
    val fields = mapper.map(reminder(due = now.minusHours(1), isActive = false), null)

    assertFalse(fields.isOverdue)
  }

  @Test
  fun `does not mark a reminder without due time as overdue`() {
    val fields = mapper.map(reminder(due = null), null)

    assertNull(fields.dueDateTime)
    assertFalse(fields.isOverdue)
  }

  @Test
  fun `returns group name and color`() {
    val fields = mapper.map(reminder(), GroupV2(uuId = "g", title = "Work", color = 4))

    assertEquals("Work", fields.groupName)
    assertEquals(4, fields.groupColor)
  }

  @Test
  fun `returns call target as secondary text`() {
    val fields = mapper.map(reminder(action = ReminderAction.Call(target = "123")), null)

    assertEquals("123", fields.secondaryText)
  }

  @Test
  fun `returns null secondary text when there is no action`() {
    val fields = mapper.map(reminder(), null)

    assertNull(fields.secondaryText)
  }
}
