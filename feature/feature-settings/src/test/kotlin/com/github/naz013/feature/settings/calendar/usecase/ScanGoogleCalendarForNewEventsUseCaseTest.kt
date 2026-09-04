package com.github.naz013.feature.settings.calendar.usecase

import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.GoogleCalendarEvent
import com.github.naz013.feature.settings.calendar.CalendarSettingsPreferences
import com.github.naz013.googlecalendar.CalendarItem
import com.github.naz013.googlecalendar.EventItem
import com.github.naz013.googlecalendar.GoogleCalendarApi
import com.github.naz013.notification.NotificationApi
import com.github.naz013.repository.EventOccurrenceRepository
import com.github.naz013.repository.GoogleCalendarEventRepository
import com.github.naz013.scheduler.JobSchedulerApi
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ScanGoogleCalendarForNewEventsUseCaseTest {
  private lateinit var prefs: CalendarSettingsPreferences
  private lateinit var googleCalendarApi: GoogleCalendarApi
  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var googleCalendarEventRepository: GoogleCalendarEventRepository
  private lateinit var eventOccurrenceRepository: EventOccurrenceRepository
  private lateinit var calculateGoogleCalendarEventOccurrencesUseCase: CalculateGoogleCalendarEventOccurrencesUseCase
  private lateinit var jobSchedulerApi: JobSchedulerApi
  private lateinit var notificationApi: NotificationApi
  private lateinit var appWidgetUpdater: AppWidgetUpdater
  private lateinit var useCase: ScanGoogleCalendarForNewEventsUseCase

  @Before
  fun setUp() {
    prefs = mockk(relaxed = true)
    googleCalendarApi = mockk()
    dateTimeManager = mockk()
    googleCalendarEventRepository = mockk(relaxed = true)
    eventOccurrenceRepository = mockk(relaxed = true)
    calculateGoogleCalendarEventOccurrencesUseCase = mockk(relaxed = true)
    jobSchedulerApi = mockk(relaxed = true)
    notificationApi = mockk(relaxed = true)
    appWidgetUpdater = mockk(relaxed = true)

    every { prefs.scanGoogleCalendarEvents } returns true
    every { prefs.selectedGoogleCalendarIds } returns setOf(CALENDAR_ID)
    coEvery { googleCalendarEventRepository.getVisible() } returns emptyList()
    coEvery { googleCalendarEventRepository.knownDeviceEventIds() } returns emptyList()
    every { googleCalendarApi.getCalendarById(CALENDAR_ID) } returns CalendarItem("Work", CALENDAR_ID)
    // Identity-ish conversions so the use case's date-time math is exercised without a real timezone.
    every { dateTimeManager.fromMillis(any()) } answers {
      LocalDateTime.ofEpochSecond(firstArg<Long>() / 1000, 0, org.threeten.bp.ZoneOffset.UTC)
    }
    every { dateTimeManager.localToUtc(any()) } answers { firstArg() }
    every { dateTimeManager.getCurrentDateTime() } returns NOW
    every { dateTimeManager.utcToLocal(any()) } answers { firstArg() }

    useCase =
      ScanGoogleCalendarForNewEventsUseCase(
        prefs = prefs,
        googleCalendarApi = googleCalendarApi,
        dateTimeManager = dateTimeManager,
        googleCalendarEventRepository = googleCalendarEventRepository,
        eventOccurrenceRepository = eventOccurrenceRepository,
        calculateGoogleCalendarEventOccurrencesUseCase = calculateGoogleCalendarEventOccurrencesUseCase,
        jobSchedulerApi = jobSchedulerApi,
        notificationApi = notificationApi,
        appWidgetUpdater = appWidgetUpdater,
        dispatcherProvider = mockDispatcherProvider(),
      )
  }

  private fun eventItem(
    id: Long,
    dtStart: Long,
    rrule: String = "",
  ): EventItem =
    EventItem(
      title = "Standup",
      description = "",
      rrule = rrule,
      rDate = "",
      calendarId = CALENDAR_ID,
      allDay = 0,
      dtStart = dtStart,
      dtEnd = 0,
      id = id,
    )

  @Test
  fun `invoke does nothing when scanning is disabled in preferences`() =
    runTest {
      every { prefs.scanGoogleCalendarEvents } returns false

      useCase()

      coVerify(exactly = 0) { googleCalendarApi.getEvents(any()) }
    }

  @Test
  fun `invoke does nothing when no calendars are selected`() =
    runTest {
      every { prefs.selectedGoogleCalendarIds } returns emptySet()

      useCase()

      coVerify(exactly = 0) { googleCalendarApi.getEvents(any()) }
    }

  @Test
  fun `invoke imports a new event as a Google Calendar event, never as a reminder`() =
    runTest {
      every { googleCalendarApi.getEvents(listOf(CALENDAR_ID)) } returns
        listOf(eventItem(id = 100L, dtStart = FUTURE_MILLIS))

      useCase()

      val savedEvents = mutableListOf<GoogleCalendarEvent>()
      coVerify { googleCalendarEventRepository.save(capture(savedEvents)) }
      assert(savedEvents.single().deviceEventId == 100L)
      coVerify { calculateGoogleCalendarEventOccurrencesUseCase(any()) }
    }

  @Test
  fun `invoke skips events already known, including previously dismissed ones`() =
    runTest {
      coEvery { googleCalendarEventRepository.knownDeviceEventIds() } returns listOf(100L)
      every { googleCalendarApi.getEvents(listOf(CALENDAR_ID)) } returns
        listOf(eventItem(id = 100L, dtStart = 0L))

      useCase()

      coVerify(exactly = 0) { googleCalendarEventRepository.save(any()) }
    }

  @Test
  fun `invoke schedules a notification for a future event`() =
    runTest {
      val futureLocal = NOW.plusHours(1)
      every { dateTimeManager.fromMillis(FUTURE_MILLIS) } returns futureLocal
      every { googleCalendarApi.getEvents(listOf(CALENDAR_ID)) } returns
        listOf(eventItem(id = 100L, dtStart = FUTURE_MILLIS))

      useCase()

      coVerify(exactly = 1) { jobSchedulerApi.scheduleGoogleCalendarEvent(any()) }
    }

  @Test
  fun `invoke does not schedule a notification for a past event`() =
    runTest {
      val pastLocal = NOW.minusHours(1)
      every { dateTimeManager.fromMillis(PAST_MILLIS) } returns pastLocal
      every { googleCalendarApi.getEvents(listOf(CALENDAR_ID)) } returns
        listOf(eventItem(id = 100L, dtStart = PAST_MILLIS))

      useCase()

      coVerify(exactly = 0) { jobSchedulerApi.scheduleGoogleCalendarEvent(any()) }
    }

  @Test
  fun `invoke cleans up a tracked event that no longer exists on the device`() =
    runTest {
      val tracked =
        GoogleCalendarEvent(
          deviceEventId = 999L,
          calendarId = CALENDAR_ID,
          calendarName = "Work",
          title = "Gone",
          description = "",
          startDateTime = NOW,
          endDateTime = null,
          allDay = false,
          rrule = "",
          uuId = "tracked-uuid",
          uniqueId = 42,
        )
      coEvery { googleCalendarEventRepository.getVisible() } returns listOf(tracked)
      every { googleCalendarApi.getEvents(listOf(CALENDAR_ID)) } returns emptyList()

      useCase()

      coVerify { eventOccurrenceRepository.deleteByEventId("tracked-uuid") }
      coVerify { googleCalendarEventRepository.deleteByDeviceEventId(999L) }
      coVerify { jobSchedulerApi.cancelGoogleCalendarEvent(42) }
      coVerify { notificationApi.cancel(42) }
    }

  companion object {
    private const val CALENDAR_ID = 7L
    private val NOW: LocalDateTime = LocalDateTime.of(2026, 3, 5, 12, 0)
    private const val FUTURE_MILLIS = 2_000_000_000_000L
    private const val PAST_MILLIS = 1_000L
  }
}
