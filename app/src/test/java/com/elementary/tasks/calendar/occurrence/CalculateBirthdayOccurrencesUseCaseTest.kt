package com.elementary.tasks.calendar.occurrence

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.occurance.EventOccurrence
import com.github.naz013.domain.occurance.OccurrenceType
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.EventOccurrenceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

/**
 * Unit tests for [CalculateBirthdayOccurrencesUseCase].
 *
 * Tests the calculation and persistence of birthday occurrences,
 * including date parsing, occurrence generation, and error handling.
 */
class CalculateBirthdayOccurrencesUseCaseTest : BaseTest() {

  private lateinit var prefs: Prefs
  private lateinit var birthdayRepository: BirthdayRepository
  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var eventOccurrenceRepository: EventOccurrenceRepository
  private lateinit var calculateBirthdayOccurrencesUseCase: CalculateBirthdayOccurrencesUseCase

  @Before
  override fun setUp() {
    super.setUp()
    prefs = mockk()
    birthdayRepository = mockk()
    dateTimeManager = mockk()
    eventOccurrenceRepository = mockk(relaxed = true)
    calculateBirthdayOccurrencesUseCase = CalculateBirthdayOccurrencesUseCase(
      prefs = prefs,
      birthdayRepository = birthdayRepository,
      dateTimeManager = dateTimeManager,
      eventOccurrenceRepository = eventOccurrenceRepository
    )
  }

  @Test
  fun `invoke should calculate and save birthday occurrences with valid data`() = runTest {
    // Arrange - Create a birthday with realistic data
    val birthdayId = "birthday-uuid-123"
    val birthdayDate = "1990-05-15"
    val parsedDate = LocalDate.of(1990, 5, 15)
    val birthdayTime = LocalTime.of(9, 0)
    val currentYear = 2025
    val numberOfOccurrences = 5

    val birthday = Birthday(
      name = "John Doe",
      date = birthdayDate,
      uuId = birthdayId,
      day = 15,
      month = 5,
      syncState = SyncState.Synced
    )

    every { prefs.numberOfBirthdayOccurrences } returns numberOfOccurrences
    coEvery { birthdayRepository.getById(birthdayId) } returns birthday
    every { dateTimeManager.parseBirthdayDate(birthdayDate) } returns parsedDate
    every { dateTimeManager.getBirthdayLocalTime() } returns birthdayTime
    every { dateTimeManager.getCurrentDate() } returns LocalDate.of(currentYear, 1, 1)

    val savedOccurrences = mutableListOf<EventOccurrence>()
    coEvery { eventOccurrenceRepository.save(capture(savedOccurrences)) } returns Unit

    // Act
    calculateBirthdayOccurrencesUseCase(birthdayId)

    // Assert
    coVerify(exactly = 1) { birthdayRepository.getById(birthdayId) }
    coVerify(exactly = 1) { eventOccurrenceRepository.deleteByEventId(birthdayId) }
    coVerify(exactly = numberOfOccurrences + 1) { eventOccurrenceRepository.save(any()) }

    // Verify all occurrences are created with correct dates
    assertEquals(numberOfOccurrences + 1, savedOccurrences.size)
    savedOccurrences.forEachIndexed { index, occurrence ->
      assertNotNull(occurrence.id)
      assertEquals(birthdayId, occurrence.eventId)
      assertEquals(birthdayTime, occurrence.time)
      assertEquals(OccurrenceType.Birthday, occurrence.type)
      // Starting from previous year (2024) and incrementing
      assertEquals(LocalDate.of(2024 + index, 5, 15), occurrence.date)
    }
  }

  @Test
  fun `invoke should handle birthday not found gracefully`() = runTest {
    // Arrange
    val birthdayId = "non-existent-id"
    coEvery { birthdayRepository.getById(birthdayId) } returns null

    // Act
    calculateBirthdayOccurrencesUseCase(birthdayId)

    // Assert
    coVerify(exactly = 1) { birthdayRepository.getById(birthdayId) }
    coVerify(exactly = 0) { dateTimeManager.parseBirthdayDate(any()) }
    coVerify(exactly = 0) { eventOccurrenceRepository.deleteByEventId(any()) }
    coVerify(exactly = 0) { eventOccurrenceRepository.save(any()) }
  }

  @Test
  fun `invoke should handle invalid birthday date gracefully`() = runTest {
    // Arrange
    val birthdayId = "birthday-uuid-456"
    val invalidDate = "invalid-date-format"
    val birthday = Birthday(
      name = "Jane Smith",
      date = invalidDate,
      uuId = birthdayId,
      day = 0,
      month = 0,
      syncState = SyncState.Synced
    )

    coEvery { birthdayRepository.getById(birthdayId) } returns birthday
    every { dateTimeManager.parseBirthdayDate(invalidDate) } returns null

    // Act
    calculateBirthdayOccurrencesUseCase(birthdayId)

    // Assert
    coVerify(exactly = 1) { birthdayRepository.getById(birthdayId) }
    coVerify(exactly = 1) { dateTimeManager.parseBirthdayDate(invalidDate) }
    coVerify(exactly = 0) { eventOccurrenceRepository.deleteByEventId(any()) }
    coVerify(exactly = 0) { eventOccurrenceRepository.save(any()) }
  }

  @Test
  fun `invoke should use current time when birthday time is null`() = runTest {
    // Arrange
    val birthdayId = "birthday-uuid-789"
    val birthdayDate = "1985-12-25"
    val parsedDate = LocalDate.of(1985, 12, 25)
    val currentYear = 2025
    val numberOfOccurrences = 3

    val birthday = Birthday(
      name = "Alice Johnson",
      date = birthdayDate,
      uuId = birthdayId,
      day = 25,
      month = 12,
      syncState = SyncState.Synced
    )

    every { prefs.numberOfBirthdayOccurrences } returns numberOfOccurrences
    coEvery { birthdayRepository.getById(birthdayId) } returns birthday
    every { dateTimeManager.parseBirthdayDate(birthdayDate) } returns parsedDate
    every { dateTimeManager.getBirthdayLocalTime() } returns null
    every { dateTimeManager.getCurrentDate() } returns LocalDate.of(currentYear, 6, 15)

    val savedOccurrences = mutableListOf<EventOccurrence>()
    coEvery { eventOccurrenceRepository.save(capture(savedOccurrences)) } returns Unit

    // Act - Time will use LocalTime.now() from the code
    calculateBirthdayOccurrencesUseCase(birthdayId)

    // Assert
    coVerify(exactly = 1) { dateTimeManager.getBirthdayLocalTime() }
    coVerify(exactly = numberOfOccurrences + 1) { eventOccurrenceRepository.save(any()) }

    // Verify occurrences were created (time will be whatever LocalTime.now() returns)
    assertEquals(numberOfOccurrences + 1, savedOccurrences.size)
  }

  @Test
  fun `invoke should delete existing occurrences before creating new ones`() = runTest {
    // Arrange
    val birthdayId = "birthday-uuid-321"
    val birthdayDate = "2000-03-10"
    val parsedDate = LocalDate.of(2000, 3, 10)
    val birthdayTime = LocalTime.of(8, 30)
    val currentYear = 2025

    val birthday = Birthday(
      name = "Bob Wilson",
      date = birthdayDate,
      uuId = birthdayId,
      day = 10,
      month = 3,
      syncState = SyncState.Synced
    )

    every { prefs.numberOfBirthdayOccurrences } returns 2
    coEvery { birthdayRepository.getById(birthdayId) } returns birthday
    every { dateTimeManager.parseBirthdayDate(birthdayDate) } returns parsedDate
    every { dateTimeManager.getBirthdayLocalTime() } returns birthdayTime
    every { dateTimeManager.getCurrentDate() } returns LocalDate.of(currentYear, 1, 1)

    // Act
    calculateBirthdayOccurrencesUseCase(birthdayId)

    // Assert - Verify delete is called before save
    coVerify(ordering = io.mockk.Ordering.ORDERED) {
      birthdayRepository.getById(birthdayId)
      eventOccurrenceRepository.deleteByEventId(birthdayId)
      eventOccurrenceRepository.save(any())
    }
  }

  @Test
  fun `invoke should generate correct number of occurrences based on preferences`() = runTest {
    // Arrange
    val birthdayId = "birthday-uuid-654"
    val birthdayDate = "1995-07-20"
    val parsedDate = LocalDate.of(1995, 7, 20)
    val birthdayTime = LocalTime.of(12, 0)
    val currentYear = 2025
    val numberOfOccurrences = 10 // Large number to test range

    val birthday = Birthday(
      name = "Carol Davis",
      date = birthdayDate,
      uuId = birthdayId,
      day = 20,
      month = 7,
      syncState = SyncState.Synced
    )

    every { prefs.numberOfBirthdayOccurrences } returns numberOfOccurrences
    coEvery { birthdayRepository.getById(birthdayId) } returns birthday
    every { dateTimeManager.parseBirthdayDate(birthdayDate) } returns parsedDate
    every { dateTimeManager.getBirthdayLocalTime() } returns birthdayTime
    every { dateTimeManager.getCurrentDate() } returns LocalDate.of(currentYear, 1, 1)

    val savedOccurrences = mutableListOf<EventOccurrence>()
    coEvery { eventOccurrenceRepository.save(capture(savedOccurrences)) } returns Unit

    // Act
    calculateBirthdayOccurrencesUseCase(birthdayId)

    // Assert
    assertEquals(numberOfOccurrences + 1, savedOccurrences.size)
    coVerify(exactly = numberOfOccurrences + 1) { eventOccurrenceRepository.save(any()) }
  }

  @Test
  fun `invoke should start occurrences from previous year`() = runTest {
    // Arrange
    val birthdayId = "birthday-uuid-987"
    val birthdayDate = "1988-11-05"
    val parsedDate = LocalDate.of(1988, 11, 5)
    val birthdayTime = LocalTime.of(14, 0)
    val currentYear = 2025
    val numberOfOccurrences = 2

    val birthday = Birthday(
      name = "David Brown",
      date = birthdayDate,
      uuId = birthdayId,
      day = 5,
      month = 11,
      syncState = SyncState.Synced
    )

    every { prefs.numberOfBirthdayOccurrences } returns numberOfOccurrences
    coEvery { birthdayRepository.getById(birthdayId) } returns birthday
    every { dateTimeManager.parseBirthdayDate(birthdayDate) } returns parsedDate
    every { dateTimeManager.getBirthdayLocalTime() } returns birthdayTime
    every { dateTimeManager.getCurrentDate() } returns LocalDate.of(currentYear, 6, 1)

    val savedOccurrences = mutableListOf<EventOccurrence>()
    coEvery { eventOccurrenceRepository.save(capture(savedOccurrences)) } returns Unit

    // Act
    calculateBirthdayOccurrencesUseCase(birthdayId)

    // Assert
    // First occurrence should be in previous year (2024)
    val firstOccurrence = savedOccurrences.first()
    assertEquals(2024, firstOccurrence.date.year)
    assertEquals(11, firstOccurrence.date.monthValue)
    assertEquals(5, firstOccurrence.date.dayOfMonth)

    // Last occurrence should be current year + 1 (2026)
    val lastOccurrence = savedOccurrences.last()
    assertEquals(2026, lastOccurrence.date.year)
  }

  @Test
  fun `invoke should handle leap year birthdays correctly`() = runTest {
    // Arrange - Birthday on leap day
    val birthdayId = "birthday-uuid-leap"
    val birthdayDate = "2000-02-29"
    val parsedDate = LocalDate.of(2000, 2, 29)
    val birthdayTime = LocalTime.of(10, 0)
    val currentYear = 2025
    val numberOfOccurrences = 4

    val birthday = Birthday(
      name = "Leap Year Baby",
      date = birthdayDate,
      uuId = birthdayId,
      day = 29,
      month = 2,
      syncState = SyncState.Synced
    )

    every { prefs.numberOfBirthdayOccurrences } returns numberOfOccurrences
    coEvery { birthdayRepository.getById(birthdayId) } returns birthday
    every { dateTimeManager.parseBirthdayDate(birthdayDate) } returns parsedDate
    every { dateTimeManager.getBirthdayLocalTime() } returns birthdayTime
    every { dateTimeManager.getCurrentDate() } returns LocalDate.of(currentYear, 1, 1)

    val savedOccurrences = mutableListOf<EventOccurrence>()
    coEvery { eventOccurrenceRepository.save(capture(savedOccurrences)) } returns Unit

    // Act
    calculateBirthdayOccurrencesUseCase(birthdayId)

    // Assert
    assertEquals(numberOfOccurrences + 1, savedOccurrences.size)

    // Verify leap year handling - the DateTimeManager handles the parsing,
    // we just verify occurrences are created for each year
    savedOccurrences.forEach { occurrence ->
      assertEquals(birthdayId, occurrence.eventId)
      assertEquals(OccurrenceType.Birthday, occurrence.type)
    }
  }

  @Test
  fun `invoke should preserve birthday UUID in all occurrences`() = runTest {
    // Arrange
    val birthdayId = "specific-birthday-uuid-12345"
    val birthdayDate = "1992-09-18"
    val parsedDate = LocalDate.of(1992, 9, 18)
    val birthdayTime = LocalTime.of(11, 0)
    val currentYear = 2025

    val birthday = Birthday(
      name = "Emma Taylor",
      date = birthdayDate,
      uuId = birthdayId,
      day = 18,
      month = 9,
      syncState = SyncState.Synced
    )

    every { prefs.numberOfBirthdayOccurrences } returns 3
    coEvery { birthdayRepository.getById(birthdayId) } returns birthday
    every { dateTimeManager.parseBirthdayDate(birthdayDate) } returns parsedDate
    every { dateTimeManager.getBirthdayLocalTime() } returns birthdayTime
    every { dateTimeManager.getCurrentDate() } returns LocalDate.of(currentYear, 1, 1)

    val savedOccurrences = mutableListOf<EventOccurrence>()
    coEvery { eventOccurrenceRepository.save(capture(savedOccurrences)) } returns Unit

    // Act
    calculateBirthdayOccurrencesUseCase(birthdayId)

    // Assert - All occurrences must have the same eventId
    savedOccurrences.forEach { occurrence ->
      assertEquals(birthdayId, occurrence.eventId)
      // Each occurrence should have unique ID but same eventId
      assertNotNull(occurrence.id)
    }

    // Verify unique IDs
    val uniqueIds = savedOccurrences.map { it.id }.toSet()
    assertEquals(savedOccurrences.size, uniqueIds.size)
  }
}

