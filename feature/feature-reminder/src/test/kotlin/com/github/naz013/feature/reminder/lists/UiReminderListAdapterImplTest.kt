package com.github.naz013.feature.reminder.lists

import androidx.compose.ui.graphics.toArgb
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.reminder.UiReminderCommonAdapter
import com.github.naz013.feature.reminder.build.formatter.factory.PlaceFormatterFactory
import com.github.naz013.logic.reminder.RecurEventManager
import com.github.naz013.ui.common.UnitsConverter
import com.github.naz013.ui.common.theme.ColorProvider
import com.github.naz013.ui.group.UiGroupListAdapter
import com.github.naz013.ui.reminder.ShopItemsFormatter
import com.github.naz013.ui.reminder.UiReminderDueData
import com.github.naz013.ui.routine.RoutineColors
import com.github.naz013.ui.routine.RoutineIconSet
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class UiReminderListAdapterImplTest {

  private val uiReminderCommonAdapter = mockk<UiReminderCommonAdapter>()
  private val adapter = UiReminderListAdapterImpl(
    uiReminderCommonAdapter = uiReminderCommonAdapter,
    unitsConverter = mockk<UnitsConverter>(relaxed = true),
    colorProvider = mockk<ColorProvider>(relaxed = true),
    textProvider = mockk<TextProvider>(relaxed = true),
    shopItemsFormatter = mockk<ShopItemsFormatter>(relaxed = true),
    uiGroupListAdapter = mockk<UiGroupListAdapter>(relaxed = true),
    dateTimeManager = mockk<DateTimeManager>(relaxed = true),
    recurEventManager = mockk<RecurEventManager>(relaxed = true),
    placeFormatterFactory = mockk<PlaceFormatterFactory>(relaxed = true),
  )

  @Before
  fun setUp() {
    every { uiReminderCommonAdapter.getDueV2(any()) } returns UiReminderDueData(
      dateTime = null,
      repeat = "",
      before = null,
      remaining = null,
      formattedTime = null,
      formattedDateTime = null,
    )
  }

  @Test
  fun `resolves the icon drawable from the shared routine icon set when an icon index is set`() {
    val reminder = reminderWith(iconIndex = 2, colorIndex = null)

    val state = adapter.createV2(reminder, group = null).state

    assertEquals(RoutineIconSet.ALL[2], state.iconRes)
  }

  @Test
  fun `resolves the tag color as ARGB from the shared routine palette when a color index is set`() {
    val reminder = reminderWith(iconIndex = null, colorIndex = 4)

    val state = adapter.createV2(reminder, group = null).state

    assertEquals(RoutineColors.ALL[4].toArgb(), state.tagColor)
  }

  @Test
  fun `leaves icon and tag color null when the reminder has neither index`() {
    val reminder = reminderWith(iconIndex = null, colorIndex = null)

    val state = adapter.createV2(reminder, group = null).state

    assertNull(state.iconRes)
    assertNull(state.tagColor)
  }

  @Test
  fun `returns a null icon when the icon index is past the end of the icon set`() {
    val reminder = reminderWith(iconIndex = RoutineIconSet.ALL.size, colorIndex = null)

    val state = adapter.createV2(reminder, group = null).state

    assertNull(state.iconRes)
  }

  @Test
  fun `returns a null tag color when the color index is past the end of the palette`() {
    val reminder = reminderWith(iconIndex = null, colorIndex = RoutineColors.ALL.size)

    val state = adapter.createV2(reminder, group = null).state

    assertNull(state.tagColor)
  }

  private fun reminderWith(iconIndex: Int?, colorIndex: Int?) = ReminderV2(
    uuId = "tagged",
    summary = "Water plants",
    recurrence = RecurrenceRule.Once,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 7, 22, 9, 0)),
    action = ReminderAction.None,
    iconIndex = iconIndex,
    colorIndex = colorIndex,
  )
}
