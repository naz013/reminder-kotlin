package com.github.naz013.ui.agenda

import com.github.naz013.ui.common.R
import com.github.naz013.ui.birthday.UiBirthdayListAdapter
import com.github.naz013.ui.common.text.UiTextElement
import com.github.naz013.ui.birthday.UiBirthdayList
import com.github.naz013.ui.common.text.UiTextFormat
import com.github.naz013.ui.reminder.UiReminderList
import com.github.naz013.ui.reminder.UiReminderListActions
import com.github.naz013.ui.reminder.UiReminderListAdapter
import com.github.naz013.ui.reminder.UiReminderListState
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.reminder.v2.LocationSettings
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.SyncMetadata
import com.github.naz013.domain.sync.SyncState
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class UiAgendaItemAdapterTest {
  private val uiReminderListAdapter = mockk<UiReminderListAdapter>()
  private val uiBirthdayListAdapter = mockk<UiBirthdayListAdapter>()
  private val dateTimeManager = mockk<DateTimeManager>()
  private val textProvider = mockk<TextProvider>()

  private lateinit var adapter: UiAgendaItemAdapter

  private val today: LocalDate = LocalDate.now()
  private val tomorrow: LocalDate = today.plusDays(1)

  @Before
  fun setUp() {
    every { dateTimeManager.getHeaderDateFormatted(any()) } answers { firstArg<LocalDate>().toString() }
    every { textProvider.getText(R.string.today) } returns "Today"
    every { textProvider.getText(R.string.tomorrow) } returns "Tomorrow"
    every { textProvider.getText(R.string.permanent) } returns "Permanent"
    every { textProvider.getText(R.string.disabled) } returns "Turned off"
    every { textProvider.getText(R.string.location) } returns "Location"
    every { textProvider.getText(R.string.shopping_lists) } returns "Shopping lists"
    every { textProvider.getText(R.string.pinned) } returns "Pinned"

    adapter = UiAgendaItemAdapter(uiReminderListAdapter, uiBirthdayListAdapter, dateTimeManager, textProvider)
  }

  private fun reminderV2(
    uuId: String,
    isLocation: Boolean = false,
    isShopping: Boolean = false,
    isPinned: Boolean = false,
  ) = ReminderV2(
    uuId = uuId,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
    sync = SyncMetadata(syncState = SyncState.Synced),
    location = if (isLocation) LocationSettings() else null,
    action = if (isShopping) ReminderAction.Shopping else ReminderAction.None,
    isPinned = isPinned,
  )

  @Test
  fun `returns empty list when there are no reminders or birthdays`() {
    val result = adapter.convertV2(emptyList(), emptyMap(), emptyList())

    assertEquals(emptyList<UiAgendaItem>(), result)
  }

  @Test
  fun `merges and sorts reminders and birthdays chronologically under a shared day header`() {
    val laterReminder = reminderV2("later-reminder")
    val earlierBirthday = Birthday(uuId = "earlier-birthday", syncState = SyncState.Synced)
    every { uiReminderListAdapter.createV2(laterReminder, null) } returns
      uiReminderList(id = "r1", dueDateTime = today.atTime(18, 0))
    every { uiBirthdayListAdapter.convert(earlierBirthday, any()) } returns
      uiBirthdayList(id = "b1", nextBirthdayDate = today.atTime(9, 0))

    val result = adapter.convertV2(listOf(laterReminder), emptyMap(), listOf(earlierBirthday))

    assertEquals(3, result.size)
    assertTrue(result[0] is UiAgendaHeader)
    assertEquals("Today", (result[0] as UiAgendaHeader).text)
    assertEquals("b1", result[1].id)
    assertEquals("r1", result[2].id)
  }

  @Test
  fun `inserts a new header only when the day bucket changes`() {
    val todayReminder = reminderV2("today-reminder")
    val tomorrowReminder = reminderV2("tomorrow-reminder")
    every { uiReminderListAdapter.createV2(todayReminder, null) } returns
      uiReminderList(id = "today-item", dueDateTime = today.atTime(9, 0))
    every { uiReminderListAdapter.createV2(tomorrowReminder, null) } returns
      uiReminderList(id = "tomorrow-item", dueDateTime = tomorrow.atTime(9, 0))

    val result = adapter.convertV2(listOf(todayReminder, tomorrowReminder), emptyMap(), emptyList())

    assertEquals(4, result.size)
    assertEquals("Today", (result[0] as UiAgendaHeader).text)
    assertEquals("today-item", result[1].id)
    assertEquals("Tomorrow", (result[2] as UiAgendaHeader).text)
    assertEquals("tomorrow-item", result[3].id)
  }

  @Test
  fun `buckets active reminders without a due date under a Permanent header after dated items`() {
    val datedReminder = reminderV2("dated-reminder")
    val permanentReminder = reminderV2("permanent-reminder")
    every { uiReminderListAdapter.createV2(datedReminder, null) } returns
      uiReminderList(id = "dated", dueDateTime = today.atTime(10, 0))
    every { uiReminderListAdapter.createV2(permanentReminder, null) } returns
      uiReminderList(id = "permanent", dueDateTime = null, isActive = true)

    val result = adapter.convertV2(listOf(datedReminder, permanentReminder), emptyMap(), emptyList())

    assertEquals(4, result.size)
    assertEquals("Today", (result[0] as UiAgendaHeader).text)
    assertEquals("dated", result[1].id)
    assertEquals("Permanent", (result[2] as UiAgendaHeader).text)
    assertEquals("permanent", result[3].id)
  }

  @Test
  fun `buckets disabled reminders without a due date under a Disabled header`() {
    val disabledReminder = reminderV2("disabled-reminder")
    every { uiReminderListAdapter.createV2(disabledReminder, null) } returns
      uiReminderList(id = "disabled", dueDateTime = null, isActive = false)

    val result = adapter.convertV2(listOf(disabledReminder), emptyMap(), emptyList())

    assertEquals(2, result.size)
    assertEquals("Turned off", (result[0] as UiAgendaHeader).text)
    assertEquals("disabled", result[1].id)
  }

  @Test
  fun `orders due-date, permanent, location, no-date shopping and disabled buckets in that sequence`() {
    val datedReminder = reminderV2("dated-reminder")
    val permanentReminder = reminderV2("permanent-reminder")
    val locationReminder = reminderV2("location-reminder", isLocation = true)
    val shoppingNoDateReminder = reminderV2("shopping-reminder", isShopping = true)
    val disabledReminder = reminderV2("disabled-reminder")
    every { uiReminderListAdapter.createV2(datedReminder, null) } returns
      uiReminderList(id = "dated", dueDateTime = today.atTime(10, 0))
    every { uiReminderListAdapter.createV2(permanentReminder, null) } returns
      uiReminderList(id = "permanent", dueDateTime = null, isActive = true)
    every { uiReminderListAdapter.createV2(locationReminder, null) } returns
      uiReminderList(id = "location", dueDateTime = null, isActive = true)
    every { uiReminderListAdapter.createV2(shoppingNoDateReminder, null) } returns
      uiReminderList(id = "shopping-no-date", dueDateTime = null, isActive = true)
    every { uiReminderListAdapter.createV2(disabledReminder, null) } returns
      uiReminderList(id = "disabled", dueDateTime = null, isActive = false)

    val result =
      adapter.convertV2(
        listOf(disabledReminder, shoppingNoDateReminder, locationReminder, permanentReminder, datedReminder),
        emptyMap(),
        emptyList(),
      )

    assertEquals(
      listOf(
        "Today",
        "dated",
        "Permanent",
        "permanent",
        "Location",
        "location",
        "Shopping lists",
        "shopping-no-date",
        "Turned off",
        "disabled",
      ),
      result.map { item -> if (item is UiAgendaHeader) item.text else item.id },
    )
  }

  @Test
  fun `keeps a location reminder in the Location bucket even when it has a due date`() {
    val locationReminder = reminderV2("location-reminder", isLocation = true)
    every { uiReminderListAdapter.createV2(locationReminder, null) } returns
      uiReminderList(id = "location", dueDateTime = today.atTime(10, 0), isActive = true)

    val result = adapter.convertV2(listOf(locationReminder), emptyMap(), emptyList())

    assertEquals(2, result.size)
    assertEquals("Location", (result[0] as UiAgendaHeader).text)
    assertEquals("location", result[1].id)
  }

  @Test
  fun `puts a pinned reminder in its own header above the regular chronological list`() {
    val pinnedReminder = reminderV2("pinned-reminder", isPinned = true)
    val regularReminder = reminderV2("regular-reminder")
    every { uiReminderListAdapter.createV2(pinnedReminder, null) } returns
      uiReminderList(id = "pinned", dueDateTime = today.atTime(20, 0), isActive = true, isPinned = true)
    every { uiReminderListAdapter.createV2(regularReminder, null) } returns
      uiReminderList(id = "regular", dueDateTime = today.atTime(9, 0))

    val result = adapter.convertV2(listOf(pinnedReminder, regularReminder), emptyMap(), emptyList())

    assertEquals(
      listOf("Pinned", "pinned", "Today", "regular"),
      result.map { item -> if (item is UiAgendaHeader) item.text else item.id },
    )
  }

  @Test
  fun `keeps a pinned but disabled reminder in the Pinned section instead of the Disabled bucket`() {
    val pinnedDisabledReminder = reminderV2("pinned-disabled-reminder", isPinned = true)
    every { uiReminderListAdapter.createV2(pinnedDisabledReminder, null) } returns
      uiReminderList(id = "pinned-disabled", dueDateTime = null, isActive = false, isPinned = true)

    val result = adapter.convertV2(listOf(pinnedDisabledReminder), emptyMap(), emptyList())

    assertEquals(
      listOf("Pinned", "pinned-disabled"),
      result.map { item -> if (item is UiAgendaHeader) item.text else item.id },
    )
  }

  @Test
  fun `does not add a Pinned header when no reminder is pinned`() {
    val regularReminder = reminderV2("regular-reminder")
    every { uiReminderListAdapter.createV2(regularReminder, null) } returns
      uiReminderList(id = "regular", dueDateTime = today.atTime(9, 0))

    val result = adapter.convertV2(listOf(regularReminder), emptyMap(), emptyList())

    assertEquals(listOf("Today", "regular"), result.map { item -> if (item is UiAgendaHeader) item.text else item.id })
  }

  @Test
  fun `findTodayScrollTargetId returns the Today header id when one exists`() {
    val todayReminder = reminderV2("today-reminder")
    val tomorrowReminder = reminderV2("tomorrow-reminder")
    every { uiReminderListAdapter.createV2(todayReminder, null) } returns
      uiReminderList(id = "today-item", dueDateTime = today.atTime(9, 0))
    every { uiReminderListAdapter.createV2(tomorrowReminder, null) } returns
      uiReminderList(id = "tomorrow-item", dueDateTime = tomorrow.atTime(9, 0))
    val items = adapter.convertV2(listOf(todayReminder, tomorrowReminder), emptyMap(), emptyList())

    val targetId = adapter.findTodayScrollTargetId(items)

    assertEquals(items[0].id, targetId)
  }

  @Test
  fun `findTodayScrollTargetId falls back to the first item due today or later when there is no Today section`() {
    val tomorrowReminder = reminderV2("tomorrow-reminder")
    every { uiReminderListAdapter.createV2(tomorrowReminder, null) } returns
      uiReminderList(id = "tomorrow-item", dueDateTime = tomorrow.atTime(9, 0))
    val items = adapter.convertV2(listOf(tomorrowReminder), emptyMap(), emptyList())

    val targetId = adapter.findTodayScrollTargetId(items)

    assertEquals("Tomorrow", (items[0] as UiAgendaHeader).text)
    assertEquals(items[0].id, targetId)
  }

  @Test
  fun `findTodayScrollTargetId skips the pinned bucket even when a pinned reminder is due today`() {
    val pinnedReminder = reminderV2("pinned-reminder", isPinned = true)
    val regularReminder = reminderV2("regular-reminder")
    every { uiReminderListAdapter.createV2(pinnedReminder, null) } returns
      uiReminderList(id = "pinned", dueDateTime = today.atTime(20, 0), isActive = true, isPinned = true)
    every { uiReminderListAdapter.createV2(regularReminder, null) } returns
      uiReminderList(id = "regular", dueDateTime = today.atTime(9, 0))
    val items = adapter.convertV2(listOf(pinnedReminder, regularReminder), emptyMap(), emptyList())

    val targetId = adapter.findTodayScrollTargetId(items)

    val todayHeader = items.first { it is UiAgendaHeader && it.text == "Today" }
    assertEquals(todayHeader.id, targetId)
  }

  @Test
  fun `findTodayScrollTargetId returns null when there are no items`() {
    val targetId = adapter.findTodayScrollTargetId(emptyList())

    assertEquals(null, targetId)
  }

  private fun textElement(text: String) = UiTextElement(text = text, textFormat = UiTextFormat(fontSize = 14f))

  private fun uiReminderList(
    id: String,
    dueDateTime: LocalDateTime?,
    isActive: Boolean = true,
    isPinned: Boolean = false,
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
      state = UiReminderListState(isActive = isActive, isRemoved = false, isGps = false, isPinned = isPinned),
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
