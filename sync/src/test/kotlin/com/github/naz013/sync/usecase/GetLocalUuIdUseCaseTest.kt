package com.github.naz013.sync.usecase

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [GetLocalUuIdUseCase].
 *
 * Tests the extraction of local UUID/ID from different domain entities,
 * including validation of supported types and error handling for unsupported types.
 */
class GetLocalUuIdUseCaseTest {

  private lateinit var getLocalUuIdUseCase: GetLocalUuIdUseCase

  @Before
  fun setUp() {
    getLocalUuIdUseCase = GetLocalUuIdUseCase()
  }

  @Test
  fun `invoke with reminder should return uuId`() {
    // Arrange
    val reminderId = "reminder-uuid-12345"
    val reminder = mockk<Reminder>()
    every { reminder.uuId } returns reminderId

    // Act
    val result = getLocalUuIdUseCase(reminder)

    // Assert
    assertEquals(reminderId, result)
  }

  @Test
  fun `invoke with birthday should return uuId`() {
    // Arrange
    val birthdayId = "birthday-uuid-67890"
    val birthday = mockk<Birthday>()
    every { birthday.uuId } returns birthdayId

    // Act
    val result = getLocalUuIdUseCase(birthday)

    // Assert
    assertEquals(birthdayId, result)
  }

  @Test
  fun `invoke with note with images should return note key`() {
    // Arrange
    val noteKey = "note-key-abc123"
    val note = mockk<Note>()
    every { note.key } returns noteKey
    val noteWithImages = NoteWithImages(note = note, images = emptyList())

    // Act
    val result = getLocalUuIdUseCase(noteWithImages)

    // Assert
    assertEquals(noteKey, result)
  }

  @Test
  fun `invoke with reminder group should return groupUuId`() {
    // Arrange
    val groupId = "group-uuid-xyz789"
    val reminderGroup = mockk<ReminderGroup>()
    every { reminderGroup.groupUuId } returns groupId

    // Act
    val result = getLocalUuIdUseCase(reminderGroup)

    // Assert
    assertEquals(groupId, result)
  }

  @Test
  fun `invoke with place should return id`() {
    // Arrange
    val placeId = "place-id-location-456"
    val place = mockk<Place>()
    every { place.id } returns placeId

    // Act
    val result = getLocalUuIdUseCase(place)

    // Assert
    assertEquals(placeId, result)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `invoke with note with images having null note should throw exception`() {
    // Arrange
    val noteWithImages = NoteWithImages(note = null, images = emptyList())

    // Act - Should throw exception
    getLocalUuIdUseCase(noteWithImages)

    // Assert - Exception expected
  }

  @Test
  fun `invoke with note with images having null note should throw exception with message`() {
    // Arrange
    val noteWithImages = NoteWithImages(note = null, images = emptyList())

    // Act & Assert
    var exceptionThrown = false
    var exceptionMessage = ""
    try {
      getLocalUuIdUseCase(noteWithImages)
    } catch (e: IllegalArgumentException) {
      exceptionThrown = true
      exceptionMessage = e.message ?: ""
    }

    assertEquals(true, exceptionThrown)
    assertEquals("Note key is null", exceptionMessage)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `invoke with unsupported type should throw exception`() {
    // Arrange - String is not a supported domain type
    val unsupportedObject = "Some random string"

    // Act - Should throw exception
    getLocalUuIdUseCase(unsupportedObject)

    // Assert - Exception expected
  }

  @Test
  fun `invoke with unsupported type should throw exception with descriptive message`() {
    // Arrange - Integer is not a supported domain type
    val unsupportedObject = 12345

    // Act & Assert
    var exceptionThrown = false
    var exceptionMessage = ""
    try {
      getLocalUuIdUseCase(unsupportedObject)
    } catch (e: IllegalArgumentException) {
      exceptionThrown = true
      exceptionMessage = e.message ?: ""
    }

    assertEquals(true, exceptionThrown)
    assertEquals(true, exceptionMessage.startsWith("Unsupported type:"))
    assertEquals(true, exceptionMessage.contains("Integer"))
  }

  @Test
  fun `invoke with reminder having uuid with hyphens should preserve format`() {
    // Arrange - Standard UUID format
    val uuidWithHyphens = "550e8400-e29b-41d4-a716-446655440000"
    val reminder = mockk<Reminder>()
    every { reminder.uuId } returns uuidWithHyphens

    // Act
    val result = getLocalUuIdUseCase(reminder)

    // Assert - Should preserve exact format including hyphens
    assertEquals(uuidWithHyphens, result)
  }

  @Test
  fun `invoke with birthday having empty uuId should return empty string`() {
    // Arrange - Birthday with empty uuId (edge case)
    val birthday = mockk<Birthday>()
    every { birthday.uuId } returns ""

    // Act
    val result = getLocalUuIdUseCase(birthday)

    // Assert - Should return empty string as-is
    assertEquals("", result)
  }

  @Test
  fun `invoke with place having numeric id should return as string`() {
    // Arrange - Place with numeric-looking ID
    val numericId = "123456789"
    val place = mockk<Place>()
    every { place.id } returns numericId

    // Act
    val result = getLocalUuIdUseCase(place)

    // Assert
    assertEquals(numericId, result)
  }

  @Test
  fun `invoke with reminder group having special characters in id should preserve them`() {
    // Arrange - Group with special characters in ID
    val specialGroupId = "group_2024.backup-v2"
    val reminderGroup = mockk<ReminderGroup>()
    every { reminderGroup.groupUuId } returns specialGroupId

    // Act
    val result = getLocalUuIdUseCase(reminderGroup)

    // Assert
    assertEquals(specialGroupId, result)
  }

  @Test
  fun `invoke with all supported types should return correct id field`() {
    // Arrange - Test all supported types to verify field mapping
    val reminderId = "reminder-1"
    val birthdayId = "birthday-1"
    val noteKey = "note-1"
    val groupId = "group-1"
    val placeId = "place-1"

    val reminder = mockk<Reminder>()
    every { reminder.uuId } returns reminderId

    val birthday = mockk<Birthday>()
    every { birthday.uuId } returns birthdayId

    val note = mockk<Note>()
    every { note.key } returns noteKey
    val noteWithImages = NoteWithImages(note = note, images = emptyList())

    val reminderGroup = mockk<ReminderGroup>()
    every { reminderGroup.groupUuId } returns groupId

    val place = mockk<Place>()
    every { place.id } returns placeId

    // Act & Assert - Verify each type returns correct ID
    assertEquals(reminderId, getLocalUuIdUseCase(reminder))
    assertEquals(birthdayId, getLocalUuIdUseCase(birthday))
    assertEquals(noteKey, getLocalUuIdUseCase(noteWithImages))
    assertEquals(groupId, getLocalUuIdUseCase(reminderGroup))
    assertEquals(placeId, getLocalUuIdUseCase(place))
  }

  @Test
  fun `invoke multiple times with same object should return same id`() {
    // Arrange
    val reminderId = "consistent-reminder-id"
    val reminder = mockk<Reminder>()
    every { reminder.uuId } returns reminderId

    // Act - Call multiple times
    val result1 = getLocalUuIdUseCase(reminder)
    val result2 = getLocalUuIdUseCase(reminder)
    val result3 = getLocalUuIdUseCase(reminder)

    // Assert - All results should be identical
    assertEquals(reminderId, result1)
    assertEquals(result1, result2)
    assertEquals(result2, result3)
  }
}

