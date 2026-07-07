package com.elementary.tasks.home.eventsview

import com.elementary.tasks.R
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayListAdapter
import com.elementary.tasks.core.data.ui.UiTextElement
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.text.UiTextFormat
import com.elementary.tasks.reminder.lists.data.UiReminderList
import com.elementary.tasks.reminder.lists.data.UiReminderListActions
import com.elementary.tasks.reminder.lists.data.UiReminderListAdapter
import com.elementary.tasks.reminder.lists.data.UiReminderListState
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.SyncState
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class UiEventItemAdapterTest {
  private val uiReminderListAdapter = mockk<UiReminderListAdapter>()
  private val uiBirthdayListAdapter = mockk<UiBirthdayListAdapter>()
  private val dateTimeManager = mockk<DateTimeManager>()
  private val textProvider = mockk<TextProvider>()

  private lateinit var adapter: UiEventItemAdapter

  private val today: LocalDate = LocalDate.now()
  private val tomorrow: LocalDate = today.plusDays(1)

  @Before
  fun setUp() {
    every { dateTimeManager.getHeaderDateFormatted(any()) } answers { firstArg<LocalDate>().toString() }
    every { textProvider.getText(R.string.today) } returns "Today"
    every { textProvider.getText(R.string.tomorrow) } returns "Tomorrow"
    every { textProvider.getText(R.string.permanent) } returns "Permanent"
    every { textProvider.getText(R.string.disabled) } returns "Turned off"

    adapter = UiEventItemAdapter(uiReminderListAdapter, uiBirthdayListAdapter, dateTimeManager, textProvider)
  }

  @Test
  fun `returns empty list when there are no reminders or birthdays`() {
    val result = adapter.convert(emptyList(), emptyList())

    assertEquals(emptyList<UiEventItem>(), result)
  }

  @Test
  fun `merges and sorts reminders and birthdays chronologically under a shared day header`() {
    val laterReminder = Reminder(uuId = "later-reminder")
    val earlierBirthday = Birthday(uuId = "earlier-birthday", syncState = SyncState.Synced)
    every { uiReminderListAdapter.create(laterReminder) } returns
      uiReminderList(id = "r1", dueDateTime = today.atTime(18, 0))
    every { uiBirthdayListAdapter.convert(earlierBirthday, any()) } returns
      uiBirthdayList(id = "b1", nextBirthdayDate = today.atTime(9, 0))

    val result = adapter.convert(listOf(laterReminder), listOf(earlierBirthday))

    assertEquals(3, result.size)
    assertTrue(result[0] is UiEventHeader)
    assertEquals("Today", (result[0] as UiEventHeader).text)
    assertEquals("b1", result[1].id)
    assertEquals("r1", result[2].id)
  }

  @Test
  fun `inserts a new header only when the day bucket changes`() {
    val todayReminder = Reminder(uuId = "today-reminder")
    val tomorrowReminder = Reminder(uuId = "tomorrow-reminder")
    every { uiReminderListAdapter.create(todayReminder) } returns
      uiReminderList(id = "today-item", dueDateTime = today.atTime(9, 0))
    every { uiReminderListAdapter.create(tomorrowReminder) } returns
      uiReminderList(id = "tomorrow-item", dueDateTime = tomorrow.atTime(9, 0))

    val result = adapter.convert(listOf(todayReminder, tomorrowReminder), emptyList())

    assertEquals(4, result.size)
    assertEquals("Today", (result[0] as UiEventHeader).text)
    assertEquals("today-item", result[1].id)
    assertEquals("Tomorrow", (result[2] as UiEventHeader).text)
    assertEquals("tomorrow-item", result[3].id)
  }

  @Test
  fun `buckets active reminders without a due date under a Permanent header after dated items`() {
    val datedReminder = Reminder(uuId = "dated-reminder")
    val permanentReminder = Reminder(uuId = "permanent-reminder")
    every { uiReminderListAdapter.create(datedReminder) } returns
      uiReminderList(id = "dated", dueDateTime = today.atTime(10, 0))
    every { uiReminderListAdapter.create(permanentReminder) } returns
      uiReminderList(id = "permanent", dueDateTime = null, isActive = true)

    val result = adapter.convert(listOf(datedReminder, permanentReminder), emptyList())

    assertEquals(4, result.size)
    assertEquals("Today", (result[0] as UiEventHeader).text)
    assertEquals("dated", result[1].id)
    assertEquals("Permanent", (result[2] as UiEventHeader).text)
    assertEquals("permanent", result[3].id)
  }

  @Test
  fun `buckets disabled reminders without a due date under a Disabled header`() {
    val disabledReminder = Reminder(uuId = "disabled-reminder")
    every { uiReminderListAdapter.create(disabledReminder) } returns
      uiReminderList(id = "disabled", dueDateTime = null, isActive = false)

    val result = adapter.convert(listOf(disabledReminder), emptyList())

    assertEquals(2, result.size)
    assertEquals("Turned off", (result[0] as UiEventHeader).text)
    assertEquals("disabled", result[1].id)
  }

  private fun textElement(text: String) = UiTextElement(text = text, textFormat = UiTextFormat(fontSize = 14f))

  private fun uiReminderList(
    id: String,
    dueDateTime: LocalDateTime?,
    isActive: Boolean = true,
  ): UiReminderList =
    UiReminderList(
      id = id,
      noteId = null,
      dueDateTime = dueDateTime,
      mainText = textElement(id),
      secondaryText = null,
      tertiaryText = null,
      tags = emptyList(),
      actions = UiReminderListActions(canToggle = true, canOpen = true, canEdit = true, canDelete = true, canSkip = false),
      state = UiReminderListState(isActive = isActive, isRemoved = false, isGps = false),
    )

  private fun uiBirthdayList(
    id: String,
    nextBirthdayDate: LocalDateTime,
  ): UiBirthdayList =
    UiBirthdayList(
      uuId = id,
      name = id,
      color = 0,
      contrastColor = 0,
      nextBirthdayDate = nextBirthdayDate,
    )
}
