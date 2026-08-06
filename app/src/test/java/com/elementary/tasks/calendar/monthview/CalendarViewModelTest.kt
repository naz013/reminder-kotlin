package com.elementary.tasks.calendar.monthview

import com.elementary.tasks.BaseTest
import com.elementary.tasks.calendar.monthview.monthgrid.MonthGridFactory
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class CalendarViewModelTest : BaseTest() {
  private val dateTimeManager = mockk<DateTimeManager>()
  private val prefs = mockk<Prefs>()
  private val monthGridFactory = mockk<MonthGridFactory>()
  private val loadMonthEventsUseCase = mockk<LoadMonthEventsUseCase>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)

  private lateinit var viewModel: CalendarViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { prefs.startDay } returns 0
    every { dateTimeManager.formatCalendarWeekday(any()) } returns "Mon"
    every { dateTimeManager.formatCalendarMonthYear(any()) } returns "July 2026"
    every { dateTimeManager.toMillis(any<LocalDateTime>()) } returns 1L

    viewModel =
      CalendarViewModel(
        dispatcherProvider = mockDispatcherProvider(),
        dateTimeManager = dateTimeManager,
        prefs = prefs,
        monthGridFactory = monthGridFactory,
        loadMonthEventsUseCase = loadMonthEventsUseCase,
        analyticsEventSender = analyticsEventSender,
        textProvider = textProvider,
      )
  }

  @Test
  fun `initializes title and weekday labels on creation`() {
    assertEquals("July 2026", viewModel.state.value.title)
    assertEquals(7, viewModel.state.value.weekdayLabels.size)
  }

  @Test
  fun `monthForPosition offsets from the center position by whole months`() {
    val center = CalendarViewModel.CENTER_POSITION
    assertEquals(viewModel.initDate.plusMonths(1), viewModel.monthForPosition(center + 1))
    assertEquals(viewModel.initDate.minusMonths(1), viewModel.monthForPosition(center - 1))
  }

  @Test
  fun `onPageSettled recomputes the title for the new month`() {
    every { dateTimeManager.formatCalendarMonthYear(any()) } returns "August 2026"

    viewModel.onPageSettled(CalendarViewModel.CENTER_POSITION + 1)

    assertEquals("August 2026", viewModel.state.value.title)
  }

  @Test
  fun `onDayClick posts OpenDayView navigation event`() {
    val date = LocalDate.of(2026, 7, 15)

    viewModel.onDayClick(date)

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(CalendarViewModel.NavigationEvent.OpenDayView(1L), event)
  }

  @Test
  fun `onAddReminderClick posts OpenNewReminder navigation event`() {
    val date = LocalDate.of(2026, 7, 15)

    viewModel.onAddReminderClick(date)

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(CalendarViewModel.NavigationEvent.OpenNewReminder(1L), event)
  }

  @Test
  fun `onAddBirthdayClick posts OpenNewBirthday navigation event`() {
    val date = LocalDate.of(2026, 7, 15)

    viewModel.onAddBirthdayClick(date)

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(CalendarViewModel.NavigationEvent.OpenNewBirthday(date), event)
  }

  @Test
  fun `onSettingsClick posts OpenSettings navigation event`() {
    every { textProvider.getString(any()) } returns "Settings"

    viewModel.onSettingsClick()

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(CalendarViewModel.NavigationEvent.OpenSettings("Settings"), event)
  }

  @Test
  fun `refresh skips the first call and bumps the signal afterwards`() {
    assertEquals(0, viewModel.refreshSignal.value)

    viewModel.refresh()
    assertEquals(0, viewModel.refreshSignal.value)

    viewModel.refresh()
    assertEquals(1, viewModel.refreshSignal.value)
  }

  @Test
  fun `loadMonthEvents delegates to the use case`() =
    runTest {
      val date = LocalDate.of(2026, 7, 1)
      coEvery { loadMonthEventsUseCase(date) } returns mapOf(date to listOf(1))

      val result = viewModel.loadMonthEvents(date)

      assertEquals(mapOf(date to listOf(1)), result)
    }
}
