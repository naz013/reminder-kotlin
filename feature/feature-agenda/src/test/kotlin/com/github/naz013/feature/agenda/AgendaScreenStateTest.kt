package com.github.naz013.feature.agenda

import com.github.naz013.ui.agenda.AgendaCategory
import com.github.naz013.ui.agenda.UiAgendaHeader
import com.github.naz013.ui.agenda.UiAgendaItem
import com.github.naz013.ui.agenda.UiAgendaReminder
import com.github.naz013.ui.common.text.UiTextElement
import com.github.naz013.ui.common.text.UiTextFormat
import com.github.naz013.ui.reminder.UiReminderListActions
import com.github.naz013.ui.reminder.UiReminderListState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.threeten.bp.LocalDateTime

class AgendaScreenStateTest {

  @Test
  fun `withoutItems returns the same state instance when nothing is hidden`() {
    val state = readyState(header("h1"), reminder("r1"))

    val result = state.withoutItems(emptySet())

    assertSame(state, result)
  }

  @Test
  fun `withoutItems removes only the hidden reminder and keeps the rest of the list`() {
    val state = readyState(header("h1"), reminder("r1"), reminder("r2"))

    val result = state.withoutItems(setOf("r1"))

    assertEquals(listOf("h1", "r2"), readyIds(result))
  }

  @Test
  fun `withoutItems drops a header whose only item was hidden`() {
    val state = readyState(header("h1"), reminder("r1"), header("h2"), reminder("r2"))

    val result = state.withoutItems(setOf("r1"))

    assertEquals(listOf("h2", "r2"), readyIds(result))
  }

  @Test
  fun `withoutItems drops a trailing header left with nothing after it`() {
    val state = readyState(header("h1"), reminder("r1"), reminder("r2"), header("h2"))

    val result = state.withoutItems(setOf("r2"))

    assertEquals(listOf("h1", "r1"), readyIds(result))
  }

  @Test
  fun `withoutItems keeps a header that still has an item after it`() {
    val state = readyState(header("h1"), reminder("r1"), reminder("r2"))

    val result = state.withoutItems(setOf("r2"))

    assertEquals(listOf("h1", "r1"), readyIds(result))
  }

  @Test
  fun `withoutItems switches to Empty when only headers would remain`() {
    val state = readyState(header("h1"), reminder("r1"), header("h2"), reminder("r2"))

    val result = state.withoutItems(setOf("r1", "r2"))

    assertEquals(ListState.Empty, result.listState)
  }

  @Test
  fun `withoutItems switches to Empty when every item is hidden`() {
    val state = readyState(reminder("r1"))

    val result = state.withoutItems(setOf("r1"))

    assertEquals(ListState.Empty, result.listState)
  }

  @Test
  fun `withoutItems leaves a Loading list state untouched`() {
    val state = AgendaScreenState(listState = ListState.Loading)

    val result = state.withoutItems(setOf("r1"))

    assertEquals(ListState.Loading, result.listState)
  }

  private fun readyState(vararg items: UiAgendaItem) = AgendaScreenState(listState = ListState.Ready(items.toList()))

  private fun readyIds(state: AgendaScreenState): List<String> =
    (state.listState as ListState.Ready).items.map { it.id }

  private fun header(id: String): UiAgendaHeader =
    UiAgendaHeader(id = id, dateTime = LocalDateTime.now(), text = id)

  private fun reminder(id: String): UiAgendaReminder =
    UiAgendaReminder(
      id = id,
      dateTime = LocalDateTime.now(),
      category = AgendaCategory.REMINDERS,
      mainText = UiTextElement(id, UiTextFormat(fontSize = 14f)),
      secondaryText = null,
      tertiaryText = null,
      tags = emptyList(),
      actions = UiReminderListActions(),
      state = UiReminderListState(),
    )
}
