package com.github.naz013.feature.settings.calendar.usecase

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.GoogleCalendarEvent
import com.github.naz013.domain.occurance.EventOccurrence
import com.github.naz013.domain.occurance.OccurrenceType
import com.github.naz013.repository.EventOccurrenceRepository
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

class CalculateGoogleCalendarEventOccurrencesUseCaseTest {
  private lateinit var eventOccurrenceRepository: EventOccurrenceRepository
  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var useCase: CalculateGoogleCalendarEventOccurrencesUseCase

  @Before
  fun setUp() {
    eventOccurrenceRepository = mockk(relaxed = true)
    dateTimeManager = mockk()
    useCase = CalculateGoogleCalendarEventOccurrencesUseCase(eventOccurrenceRepository, dateTimeManager)

    // Identity conversions so the test can work in plain LocalDateTime without a real timezone.
    every { dateTimeManager.utcToLocal(any()) } answers { firstArg() }
    every { dateTimeManager.toMillis(any<LocalDateTime>()) } answers {
      firstArg<LocalDateTime>().toInstant(ZoneOffset.UTC).toEpochMilli()
    }
    every { dateTimeManager.fromMillis(any()) } answers {
      LocalDateTime.ofInstant(Instant.ofEpochMilli(firstArg()), ZoneOffset.UTC)
    }
  }

  private fun event(rrule: String = ""): GoogleCalendarEvent =
    GoogleCalendarEvent(
      deviceEventId = 1L,
      calendarId = 1L,
      calendarName = "Work",
      title = "Standup",
      description = "",
      startDateTime = LocalDateTime.of(2026, 3, 5, 9, 0),
      endDateTime = null,
      allDay = false,
      rrule = rrule,
      uuId = "event-uuid",
    )

  @Test
  fun `invoke clears existing occurrences before saving new ones`() =
    runTest {
      useCase(event())

      coVerify(ordering = Ordering.ORDERED) {
        eventOccurrenceRepository.deleteByEventId("event-uuid")
        eventOccurrenceRepository.saveAll(any())
      }
    }

  @Test
  fun `invoke saves a single occurrence for a non-recurring event`() =
    runTest {
      val saved = mutableListOf<List<EventOccurrence>>()
      coEvery { eventOccurrenceRepository.saveAll(capture(saved)) } returns Unit

      useCase(event())

      val occurrences = saved.single()
      assertEquals(1, occurrences.size)
      assertEquals("event-uuid", occurrences.first().eventId)
      assertEquals(OccurrenceType.CalendarEvent, occurrences.first().type)
      assertEquals(LocalDateTime.of(2026, 3, 5, 9, 0).toLocalDate(), occurrences.first().date)
    }

  @Test
  fun `invoke expands a recurring event into multiple bounded occurrences`() =
    runTest {
      val saved = mutableListOf<List<EventOccurrence>>()
      coEvery { eventOccurrenceRepository.saveAll(capture(saved)) } returns Unit

      useCase(event(rrule = "FREQ=DAILY;COUNT=5"))

      val occurrences = saved.single()
      // The original start plus 4 more daily occurrences from COUNT=5.
      assertEquals(5, occurrences.size)
      assertTrue(occurrences.all { it.eventId == "event-uuid" })
      assertTrue(occurrences.all { it.type == OccurrenceType.CalendarEvent })
    }

  @Test
  fun `invoke ignores an unparseable recurrence rule and still saves the initial occurrence`() =
    runTest {
      val saved = mutableListOf<List<EventOccurrence>>()
      coEvery { eventOccurrenceRepository.saveAll(capture(saved)) } returns Unit

      useCase(event(rrule = "not a valid rrule"))

      assertEquals(1, saved.single().size)
    }
}
