package com.github.naz013.feature.reminder.quickadd

import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.reminder.build.BuildReminderNavKey
import com.github.naz013.logic.quickadd.QuickAddParser
import com.github.naz013.logic.quickadd.QuickAddResult
import com.github.naz013.logic.reminder.usecase.SaveReminderUseCase
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.ui.common.locale.LocalePreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class QuickAddViewModelTest : BaseTest() {
  private val quickAddParser = mockk<QuickAddParser>()
  private val saveReminderUseCase = mockk<SaveReminderUseCase>(relaxed = true)
  private val dateTimeManager = mockk<DateTimeManager>(relaxed = true)
  private val localePreferences = mockk<LocalePreferences>()
  private val textProvider = mockk<TextProvider>(relaxed = true)

  private lateinit var viewModel: QuickAddViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { localePreferences.appLanguage } returns 1 // en, see Language.getScreenLanguage
    every { dateTimeManager.localToUtc(any()) } answers { firstArg() }

    viewModel =
      QuickAddViewModel(
        quickAddParser = quickAddParser,
        saveReminderUseCase = saveReminderUseCase,
        dateTimeManager = dateTimeManager,
        localePreferences = localePreferences,
        textProvider = textProvider,
        dispatcherProvider = mockDispatcherProvider(),
      )
  }

  @Test
  fun `onInputChanged with a fully understood phrase enables save and exposes matched ranges`() {
    val result =
      QuickAddResult.Parsed(
        title = "pay rent",
        startDateTime = LocalDateTime.of(2025, 1, 1, 9, 0),
        recurrence = RecurrenceRule.Monthly(dayOfMonth = 1),
        matchedRanges = listOf(9..23),
      )
    every { quickAddParser.parse("pay rent every 1st at 9am", "en", any()) } returns result

    viewModel.onInputChanged("pay rent every 1st at 9am")

    val state = viewModel.state.value
    assertEquals("pay rent every 1st at 9am", state.input)
    assertEquals(listOf(9..23), state.matchedRanges)
    assertTrue(state.canSave)
    assertEquals("pay rent", state.preview?.title)
    assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), state.preview?.startDateTime)
    assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1), state.preview?.recurrence)
  }

  @Test
  fun `onInputChanged with unrecognized text disables save and shows no preview`() {
    every { quickAddParser.parse("buy milk", "en", any()) } returns
      QuickAddResult.PartiallyParsed(
        title = "buy milk",
        startDateTime = null,
        recurrence = null,
        matchedRanges = emptyList(),
      )

    viewModel.onInputChanged("buy milk")

    assertFalse(viewModel.state.value.canSave)
    assertTrue(viewModel.state.value.matchedRanges.isEmpty())
    assertEquals(null, viewModel.state.value.preview)
  }

  @Test
  fun `onSaveClick with a fully understood phrase saves the reminder and dismisses`() =
    runTest {
      val startDateTime = LocalDateTime.of(2025, 1, 1, 9, 0)
      val result =
        QuickAddResult.Parsed(
          title = "pay rent",
          startDateTime = startDateTime,
          recurrence = RecurrenceRule.Monthly(dayOfMonth = 1),
          matchedRanges = listOf(9..23),
        )
      every { quickAddParser.parse("pay rent every 1st at 9am", "en", any()) } returns result
      viewModel.onInputChanged("pay rent every 1st at 9am")

      val savedReminder = slot<ReminderV2>()
      coEvery { saveReminderUseCase(capture(savedReminder)) } returns Unit

      viewModel.onSaveClick()

      coVerify { saveReminderUseCase(any()) }
      assertEquals("pay rent", savedReminder.captured.summary)
      assertEquals(RecurrenceRule.Monthly(dayOfMonth = 1), savedReminder.captured.recurrence)
      assertEquals(startDateTime, savedReminder.captured.schedule.startDateTime)
      assertEquals(QuickAddViewModel.ViewModelEvent.Dismiss, viewModel.event.value?.peekContent())
    }

  @Test
  fun `onDateSelected overrides the previewed date while keeping the parsed title and time`() {
    every { quickAddParser.parse("pay rent every 1st at 9am", "en", any()) } returns
      QuickAddResult.Parsed(
        title = "pay rent",
        startDateTime = LocalDateTime.of(2025, 1, 1, 9, 0),
        recurrence = RecurrenceRule.Monthly(dayOfMonth = 1),
        matchedRanges = listOf(9..23),
      )
    viewModel.onInputChanged("pay rent every 1st at 9am")

    viewModel.onDateSelected(LocalDate.of(2025, 2, 15))

    val preview = viewModel.state.value.preview
    assertEquals(LocalDateTime.of(2025, 2, 15, 9, 0), preview?.startDateTime)
    assertEquals("pay rent", preview?.title)
    assertTrue(viewModel.state.value.canSave)
  }

  @Test
  fun `onTimeSelected overrides the previewed time while keeping the date`() {
    every { quickAddParser.parse("pay rent every 1st at 9am", "en", any()) } returns
      QuickAddResult.Parsed(
        title = "pay rent",
        startDateTime = LocalDateTime.of(2025, 1, 1, 9, 0),
        recurrence = RecurrenceRule.Monthly(dayOfMonth = 1),
        matchedRanges = listOf(9..23),
      )
    viewModel.onInputChanged("pay rent every 1st at 9am")

    viewModel.onTimeSelected(LocalTime.of(18, 30))

    assertEquals(LocalDateTime.of(2025, 1, 1, 18, 30), viewModel.state.value.preview?.startDateTime)
  }

  @Test
  fun `onRepeatOptionSelected WEEKLY derives the weekday from the current preview date`() {
    // 2025-01-06 is a Monday.
    every { quickAddParser.parse("gym tomorrow at 6pm", "en", any()) } returns
      QuickAddResult.Parsed(
        title = "gym",
        startDateTime = LocalDateTime.of(2025, 1, 6, 18, 0),
        recurrence = RecurrenceRule.Once,
        matchedRanges = listOf(4..19),
      )
    viewModel.onInputChanged("gym tomorrow at 6pm")

    viewModel.onRepeatOptionSelected(QuickAddRepeatOption.WEEKLY)

    // weekdays is a 7-element bitmask (index 0=Sunday..6=Saturday) - see ByWeekdaysDecomposer/
    // WeekDaysProtocol, not a list of selected day indices; index 1 (Monday) is set here.
    assertEquals(
      RecurrenceRule.Weekly(weekdays = listOf(0, 1, 0, 0, 0, 0, 0)),
      viewModel.state.value.preview?.recurrence,
    )
  }

  @Test
  fun `onRepeatOptionSelected NONE clears recurrence back to a one-off`() {
    every { quickAddParser.parse("pay rent every 1st at 9am", "en", any()) } returns
      QuickAddResult.Parsed(
        title = "pay rent",
        startDateTime = LocalDateTime.of(2025, 1, 1, 9, 0),
        recurrence = RecurrenceRule.Monthly(dayOfMonth = 1),
        matchedRanges = listOf(9..23),
      )
    viewModel.onInputChanged("pay rent every 1st at 9am")

    viewModel.onRepeatOptionSelected(QuickAddRepeatOption.NONE)

    assertEquals(RecurrenceRule.Once, viewModel.state.value.preview?.recurrence)
  }

  @Test
  fun `onInputChanged clears manual date and recurrence overrides from the previous phrase`() {
    every { quickAddParser.parse("pay rent every 1st at 9am", "en", any()) } returns
      QuickAddResult.Parsed(
        title = "pay rent",
        startDateTime = LocalDateTime.of(2025, 1, 1, 9, 0),
        recurrence = RecurrenceRule.Monthly(dayOfMonth = 1),
        matchedRanges = listOf(9..23),
      )
    viewModel.onInputChanged("pay rent every 1st at 9am")
    viewModel.onDateSelected(LocalDate.of(2025, 3, 3))

    every { quickAddParser.parse("buy milk tomorrow at 8am", "en", any()) } returns
      QuickAddResult.Parsed(
        title = "buy milk",
        startDateTime = LocalDateTime.of(2025, 1, 7, 8, 0),
        recurrence = RecurrenceRule.Once,
        matchedRanges = listOf(9..24),
      )
    viewModel.onInputChanged("buy milk tomorrow at 8am")

    assertEquals(LocalDateTime.of(2025, 1, 7, 8, 0), viewModel.state.value.preview?.startDateTime)
  }

  @Test
  fun `onSaveClick saves using a manually-overridden date and recurrence`() =
    runTest {
      every { quickAddParser.parse("pay rent every 1st at 9am", "en", any()) } returns
        QuickAddResult.Parsed(
          title = "pay rent",
          startDateTime = LocalDateTime.of(2025, 1, 1, 9, 0),
          recurrence = RecurrenceRule.Monthly(dayOfMonth = 1),
          matchedRanges = listOf(9..23),
        )
      viewModel.onInputChanged("pay rent every 1st at 9am")
      viewModel.onDateSelected(LocalDate.of(2025, 2, 15))
      viewModel.onRepeatOptionSelected(QuickAddRepeatOption.NONE)

      val savedReminder = slot<ReminderV2>()
      coEvery { saveReminderUseCase(capture(savedReminder)) } returns Unit

      viewModel.onSaveClick()

      coVerify { saveReminderUseCase(any()) }
      assertEquals(LocalDateTime.of(2025, 2, 15, 9, 0), savedReminder.captured.schedule.startDateTime)
      assertEquals(RecurrenceRule.Once, savedReminder.captured.recurrence)
    }

  @Test
  fun `onSaveClick with a schedule-only phrase hands off a blank title and the recognized schedule`() {
    val startDateTime = LocalDateTime.of(2025, 1, 7, 9, 0)
    every { quickAddParser.parse("tomorrow at 9am", "en", any()) } returns
      QuickAddResult.PartiallyParsed(
        title = "",
        startDateTime = startDateTime,
        recurrence = null,
        matchedRanges = listOf(0..15),
      )
    every { dateTimeManager.toMillis(startDateTime) } returns 999L
    viewModel.onInputChanged("tomorrow at 9am")

    viewModel.onSaveClick()

    assertEquals(
      QuickAddViewModel.ViewModelEvent.OpenFullEditor(
        text = "",
        quickAddDeepLink = BuildReminderNavKey.Main.QuickAddDeepLink(startDateTimeMillis = 999L),
      ),
      viewModel.event.value?.peekContent(),
    )
  }

  /** [QuickAddResult] never actually produces a [QuickAddResult.PartiallyParsed] carrying both a
   * non-blank title and a resolved schedule at once (see `QuickAddParser`'s two real
   * `PartiallyParsed` shapes) - but nothing about the ViewModel's save/hand-off decision should
   * depend on which sealed case it came from, only on whether the preview it builds is complete. */
  @Test
  fun `onSaveClick saves directly whenever the preview has both a title and a schedule, regardless of parse shape`() =
    runTest {
      val startDateTime = LocalDateTime.of(2025, 1, 2, 9, 0)
      every { quickAddParser.parse("call mom tomorrow", "en", any()) } returns
        QuickAddResult.PartiallyParsed(
          title = "call mom",
          startDateTime = startDateTime,
          recurrence = null,
          matchedRanges = listOf(9..17),
        )
      viewModel.onInputChanged("call mom tomorrow")

      val savedReminder = slot<ReminderV2>()
      coEvery { saveReminderUseCase(capture(savedReminder)) } returns Unit

      viewModel.onSaveClick()

      coVerify { saveReminderUseCase(any()) }
      assertEquals("call mom", savedReminder.captured.summary)
      assertEquals(startDateTime, savedReminder.captured.schedule.startDateTime)
      assertEquals(QuickAddViewModel.ViewModelEvent.Dismiss, viewModel.event.value?.peekContent())
    }

  @Test
  fun `onSaveClick with nothing recognized hands the raw input to the full editor with no schedule`() {
    every { quickAddParser.parse("buy milk", "en", any()) } returns
      QuickAddResult.PartiallyParsed(
        title = "buy milk",
        startDateTime = null,
        recurrence = null,
        matchedRanges = emptyList(),
      )
    viewModel.onInputChanged("buy milk")

    viewModel.onSaveClick()

    assertEquals(
      QuickAddViewModel.ViewModelEvent.OpenFullEditor(text = "buy milk", quickAddDeepLink = null),
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onEditManuallyClick hands the recognized title and schedule to the full editor`() {
    val startDateTime = LocalDateTime.of(2025, 1, 1, 9, 0)
    every { quickAddParser.parse("pay rent every 1st at 9am", "en", any()) } returns
      QuickAddResult.Parsed(
        title = "pay rent",
        startDateTime = startDateTime,
        recurrence = RecurrenceRule.Monthly(dayOfMonth = 1, repeatInterval = 1),
        matchedRanges = listOf(9..23),
      )
    every { dateTimeManager.toMillis(startDateTime) } returns 555L
    viewModel.onInputChanged("pay rent every 1st at 9am")

    viewModel.onEditManuallyClick()

    val expectedDeepLink =
      BuildReminderNavKey.Main.QuickAddDeepLink(
        startDateTimeMillis = 555L,
        recurrenceType = BuildReminderNavKey.Main.QuickAddDeepLink.RecurrenceType.MONTHLY,
        interval = 1,
        dayOfMonth = 1,
      )
    assertEquals(
      QuickAddViewModel.ViewModelEvent.OpenFullEditor(text = "pay rent", quickAddDeepLink = expectedDeepLink),
      viewModel.event.value?.peekContent(),
    )
  }
}
