package com.github.naz013.feature.calendar.preview

import com.github.naz013.domain.GoogleCalendarEvent
import com.github.naz013.googlecalendar.GoogleCalendarApi
import com.github.naz013.notification.NotificationApi
import com.github.naz013.repository.EventOccurrenceRepository
import com.github.naz013.repository.GoogleCalendarEventRepository
import com.github.naz013.scheduler.JobSchedulerApi
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class DismissGoogleCalendarEventUseCaseTest {
  private lateinit var googleCalendarEventRepository: GoogleCalendarEventRepository
  private lateinit var eventOccurrenceRepository: EventOccurrenceRepository
  private lateinit var jobSchedulerApi: JobSchedulerApi
  private lateinit var notificationApi: NotificationApi
  private lateinit var googleCalendarApi: GoogleCalendarApi
  private lateinit var useCase: DismissGoogleCalendarEventUseCase

  private val event =
    GoogleCalendarEvent(
      deviceEventId = 555L,
      calendarId = 1L,
      calendarName = "Work",
      title = "Standup",
      description = "",
      startDateTime = LocalDateTime.of(2026, 3, 5, 9, 0),
      endDateTime = null,
      allDay = false,
      rrule = "",
      uuId = "event-uuid",
      uniqueId = 42,
    )

  @Before
  fun setUp() {
    googleCalendarEventRepository = mockk(relaxed = true)
    eventOccurrenceRepository = mockk(relaxed = true)
    jobSchedulerApi = mockk(relaxed = true)
    notificationApi = mockk(relaxed = true)
    googleCalendarApi = mockk(relaxed = true)
    useCase =
      DismissGoogleCalendarEventUseCase(
        googleCalendarEventRepository,
        eventOccurrenceRepository,
        jobSchedulerApi,
        notificationApi,
        googleCalendarApi,
      )
  }

  @Test
  fun `invoke always clears occurrences, cancels the alarm and notification, and tombstones the event`() =
    runTest {
      useCase(event, alsoDeleteFromDeviceCalendar = false)

      coVerify { eventOccurrenceRepository.deleteByEventId("event-uuid") }
      coVerify { jobSchedulerApi.cancelGoogleCalendarEvent(42) }
      coVerify { notificationApi.cancel(42) }
      coVerify { googleCalendarEventRepository.markDismissed("event-uuid") }
    }

  @Test
  fun `invoke does not touch the device calendar when local-only delete is requested`() =
    runTest {
      useCase(event, alsoDeleteFromDeviceCalendar = false)

      coVerify(exactly = 0) { googleCalendarApi.deleteEvent(any()) }
    }

  @Test
  fun `invoke also deletes the device calendar event when requested`() =
    runTest {
      useCase(event, alsoDeleteFromDeviceCalendar = true)

      coVerify { googleCalendarApi.deleteEvent(555L) }
    }
}
