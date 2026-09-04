package com.github.naz013.feature.calendar.preview

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.GoogleCalendarEvent
import com.github.naz013.repository.GoogleCalendarEventRepository
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.getOrAwaitValue
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class GoogleCalendarEventPreviewViewModelTest : BaseTest() {
  private lateinit var googleCalendarEventRepository: GoogleCalendarEventRepository
  private lateinit var dismissGoogleCalendarEventUseCase: DismissGoogleCalendarEventUseCase
  private lateinit var dateTimeManager: DateTimeManager

  private val event =
    GoogleCalendarEvent(
      deviceEventId = 555L,
      calendarId = 1L,
      calendarName = "Work",
      title = "Standup",
      description = "Daily sync",
      startDateTime = LocalDateTime.of(2026, 3, 5, 9, 0),
      endDateTime = null,
      allDay = false,
      rrule = "",
      uuId = "event-uuid",
      uniqueId = 42,
    )

  @Before
  override fun setUp() {
    super.setUp()
    googleCalendarEventRepository = mockk()
    dismissGoogleCalendarEventUseCase = mockk(relaxed = true)
    dateTimeManager = mockk()
    every { dateTimeManager.utcToLocal(any()) } answers { firstArg() }
    every { dateTimeManager.getFullDateTime(any<LocalDateTime>()) } returns "Mar 5, 2026 9:00 AM"
  }

  private fun viewModel(): GoogleCalendarEventPreviewViewModel =
    GoogleCalendarEventPreviewViewModel(
      id = "event-uuid",
      googleCalendarEventRepository = googleCalendarEventRepository,
      dismissGoogleCalendarEventUseCase = dismissGoogleCalendarEventUseCase,
      dateTimeManager = dateTimeManager,
      dispatcherProvider = mockDispatcherProvider(),
    )

  @Test
  fun `loads the event and populates the state`() =
    runTest {
      coEvery { googleCalendarEventRepository.getById("event-uuid") } returns event

      val vm = viewModel()

      assertEquals("Standup", vm.state.value.title)
      assertEquals("Work", vm.state.value.calendarName)
      assertEquals("Daily sync", vm.state.value.description)
      assertFalse(vm.state.value.isLoading)
    }

  @Test
  fun `navigates back when the event is not found`() =
    runTest {
      coEvery { googleCalendarEventRepository.getById("event-uuid") } returns null

      val vm = viewModel()

      val event = vm.event.getOrAwaitValue()
      assertEquals(GoogleCalendarEventPreviewViewModel.ViewModelEvent.MoveBack, event?.getContentIfNotHandled())
    }

  @Test
  fun `navigates back when the event was already dismissed`() =
    runTest {
      coEvery { googleCalendarEventRepository.getById("event-uuid") } returns event.copy(isDismissed = true)

      val vm = viewModel()

      val navEvent = vm.event.getOrAwaitValue()
      assertEquals(GoogleCalendarEventPreviewViewModel.ViewModelEvent.MoveBack, navEvent?.getContentIfNotHandled())
    }

  @Test
  fun `onDeleteClick shows the delete options`() =
    runTest {
      coEvery { googleCalendarEventRepository.getById("event-uuid") } returns event
      val vm = viewModel()

      vm.onDeleteClick()

      assertTrue(vm.state.value.showDeleteOptions)
    }

  @Test
  fun `onDeleteLocalOnly dismisses the event locally and navigates back`() =
    runTest {
      coEvery { googleCalendarEventRepository.getById("event-uuid") } returns event
      val vm = viewModel()

      vm.onDeleteLocalOnly()

      coVerify { dismissGoogleCalendarEventUseCase(event, alsoDeleteFromDeviceCalendar = false) }
      val navEvent = vm.event.getOrAwaitValue()
      assertEquals(GoogleCalendarEventPreviewViewModel.ViewModelEvent.MoveBack, navEvent?.getContentIfNotHandled())
    }

  @Test
  fun `onDeleteFromDeviceCalendarToo dismisses everywhere and navigates back`() =
    runTest {
      coEvery { googleCalendarEventRepository.getById("event-uuid") } returns event
      val vm = viewModel()

      vm.onDeleteFromDeviceCalendarToo()

      coVerify { dismissGoogleCalendarEventUseCase(event, alsoDeleteFromDeviceCalendar = true) }
      val navEvent = vm.event.getOrAwaitValue()
      assertEquals(GoogleCalendarEventPreviewViewModel.ViewModelEvent.MoveBack, navEvent?.getContentIfNotHandled())
    }
}
