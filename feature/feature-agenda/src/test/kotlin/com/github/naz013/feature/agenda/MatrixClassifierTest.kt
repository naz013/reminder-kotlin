package com.github.naz013.feature.agenda

import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.ui.agenda.AgendaCategory
import com.github.naz013.ui.agenda.UiAgendaReminder
import com.github.naz013.ui.common.text.UiTextElement
import com.github.naz013.ui.common.text.UiTextFormat
import com.github.naz013.ui.reminder.UiReminderListActions
import com.github.naz013.ui.reminder.UiReminderListState
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class MatrixClassifierTest {

  private val today: LocalDate = LocalDate.of(2026, 8, 2)

  @Test
  fun `high priority reminder due today lands in Do First`() {
    val reminder = reminder(id = "r1", priority = ReminderPriority.HIGH, dateTime = today.atTime(9, 0))

    val result = classifyIntoQuadrants(listOf(reminder), today)

    assertEquals(listOf("r1"), result[EisenhowerQuadrant.DO_FIRST]?.map { it.id })
  }

  @Test
  fun `highest priority overdue reminder lands in Do First even when due date is in the past`() {
    val reminder = reminder(id = "r1", priority = ReminderPriority.HIGHEST, dateTime = today.minusDays(1).atTime(9, 0), isOverdue = true)

    val result = classifyIntoQuadrants(listOf(reminder), today)

    assertEquals(listOf("r1"), result[EisenhowerQuadrant.DO_FIRST]?.map { it.id })
  }

  @Test
  fun `high priority reminder due later lands in Schedule`() {
    val reminder = reminder(id = "r1", priority = ReminderPriority.HIGH, dateTime = today.plusDays(3).atTime(9, 0))

    val result = classifyIntoQuadrants(listOf(reminder), today)

    assertEquals(listOf("r1"), result[EisenhowerQuadrant.SCHEDULE]?.map { it.id })
  }

  @Test
  fun `normal priority reminder due today lands in Delegate`() {
    val reminder = reminder(id = "r1", priority = ReminderPriority.NORMAL, dateTime = today.atTime(9, 0))

    val result = classifyIntoQuadrants(listOf(reminder), today)

    assertEquals(listOf("r1"), result[EisenhowerQuadrant.DELEGATE]?.map { it.id })
  }

  @Test
  fun `low priority reminder due later lands in Someday`() {
    val reminder = reminder(id = "r1", priority = ReminderPriority.LOW, dateTime = today.plusDays(3).atTime(9, 0))

    val result = classifyIntoQuadrants(listOf(reminder), today)

    assertEquals(listOf("r1"), result[EisenhowerQuadrant.SOMEDAY]?.map { it.id })
  }

  @Test
  fun `permanent reminder with no real due date lands in Someday when priority is not high`() {
    val reminder = reminder(id = "r1", priority = ReminderPriority.NORMAL, dateTime = LocalDateTime.of(9999, 12, 28, 0, 0))

    val result = classifyIntoQuadrants(listOf(reminder), today)

    assertEquals(listOf("r1"), result[EisenhowerQuadrant.SOMEDAY]?.map { it.id })
  }

  @Test
  fun `every quadrant key is present even when empty`() {
    val result = classifyIntoQuadrants(emptyList(), today)

    assertEquals(EisenhowerQuadrant.entries.toSet(), result.keys)
    assertEquals(true, result.values.all { it.isEmpty() })
  }

  @Test
  fun `sorts reminders within a quadrant chronologically`() {
    val later = reminder(id = "later", priority = ReminderPriority.HIGH, dateTime = today.atTime(18, 0))
    val earlier = reminder(id = "earlier", priority = ReminderPriority.HIGH, dateTime = today.atTime(8, 0))

    val result = classifyIntoQuadrants(listOf(later, earlier), today)

    assertEquals(listOf("earlier", "later"), result[EisenhowerQuadrant.DO_FIRST]?.map { it.id })
  }

  private fun reminder(
    id: String,
    priority: ReminderPriority,
    dateTime: LocalDateTime,
    isOverdue: Boolean = false,
  ) = UiAgendaReminder(
    id = id,
    dateTime = dateTime,
    category = AgendaCategory.REMINDERS,
    mainText = UiTextElement(id, UiTextFormat(fontSize = 14f)),
    secondaryText = null,
    tertiaryText = null,
    tags = emptyList(),
    actions = UiReminderListActions(),
    state = UiReminderListState(),
    isOverdue = isOverdue,
    priority = priority,
  )
}
