package com.github.naz013.sync.usecase

import com.github.naz013.sync.DataType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [GetCloudFileNameUseCase].
 *
 * Tests the construction of cloud file names by combining IDs with data type file extensions.
 */
class GetCloudFileNameUseCaseTest {

  private lateinit var getCloudFileNameUseCase: GetCloudFileNameUseCase

  @Before
  fun setUp() {
    getCloudFileNameUseCase = GetCloudFileNameUseCase()
  }

  @Test
  fun `invoke with reminder id should return name with ta2 extension`() {
    // Arrange
    val dataType = DataType.Reminders
    val reminderId = "reminder-uuid-12345"

    // Act
    val result = getCloudFileNameUseCase(dataType, reminderId)

    // Assert
    assertEquals("reminder-uuid-12345.ta2", result)
  }

  @Test
  fun `invoke with birthday id should return name with bi2 extension`() {
    // Arrange
    val dataType = DataType.Birthdays
    val birthdayId = "birthday-uuid-67890"

    // Act
    val result = getCloudFileNameUseCase(dataType, birthdayId)

    // Assert
    assertEquals("birthday-uuid-67890.bi2", result)
  }

  @Test
  fun `invoke with note id should return name with no2 extension`() {
    // Arrange
    val dataType = DataType.Notes
    val noteId = "note-key-abc123"

    // Act
    val result = getCloudFileNameUseCase(dataType, noteId)

    // Assert
    assertEquals("note-key-abc123.no2", result)
  }

  @Test
  fun `invoke with group id should return name with gr2 extension`() {
    // Arrange
    val dataType = DataType.Groups
    val groupId = "group-uuid-xyz789"

    // Act
    val result = getCloudFileNameUseCase(dataType, groupId)

    // Assert
    assertEquals("group-uuid-xyz789.gr2", result)
  }

  @Test
  fun `invoke with place id should return name with pl2 extension`() {
    // Arrange
    val dataType = DataType.Places
    val placeId = "place-id-location-456"

    // Act
    val result = getCloudFileNameUseCase(dataType, placeId)

    // Assert
    assertEquals("place-id-location-456.pl2", result)
  }

  @Test
  fun `invoke with settings id should return name with settings extension`() {
    // Arrange
    val dataType = DataType.Settings
    val settingsId = "app"

    // Act
    val result = getCloudFileNameUseCase(dataType, settingsId)

    // Assert
    assertEquals("app.settings", result)
  }

  @Test
  fun `invoke with uuid format id should preserve hyphens in filename`() {
    // Arrange - Standard UUID format with hyphens
    val dataType = DataType.Reminders
    val uuidId = "550e8400-e29b-41d4-a716-446655440000"

    // Act
    val result = getCloudFileNameUseCase(dataType, uuidId)

    // Assert
    assertEquals("550e8400-e29b-41d4-a716-446655440000.ta2", result)
  }

  @Test
  fun `invoke with short id should work correctly`() {
    // Arrange - Very short ID (single character)
    val dataType = DataType.Birthdays
    val shortId = "1"

    // Act
    val result = getCloudFileNameUseCase(dataType, shortId)

    // Assert
    assertEquals("1.bi2", result)
  }

  @Test
  fun `invoke with long id should work correctly`() {
    // Arrange - Very long ID
    val dataType = DataType.Notes
    val longId = "this-is-a-very-long-identifier-with-many-characters-that-might-be-used-in-some-scenarios"

    // Act
    val result = getCloudFileNameUseCase(dataType, longId)

    // Assert
    assertEquals("this-is-a-very-long-identifier-with-many-characters-that-might-be-used-in-some-scenarios.no2", result)
  }

  @Test
  fun `invoke with id containing special characters should preserve them`() {
    // Arrange - ID with special characters (underscores, dots, etc.)
    val dataType = DataType.Groups
    val specialId = "group_2024.backup-v2"

    // Act
    val result = getCloudFileNameUseCase(dataType, specialId)

    // Assert
    assertEquals("group_2024.backup-v2.gr2", result)
  }

  @Test
  fun `invoke with empty string id should return only extension`() {
    // Arrange - Empty string ID
    val dataType = DataType.Places
    val emptyId = ""

    // Act
    val result = getCloudFileNameUseCase(dataType, emptyId)

    // Assert
    assertEquals(".pl2", result)
  }

  @Test
  fun `invoke with numeric id should work correctly`() {
    // Arrange - Purely numeric ID
    val dataType = DataType.Reminders
    val numericId = "123456789"

    // Act
    val result = getCloudFileNameUseCase(dataType, numericId)

    // Assert
    assertEquals("123456789.ta2", result)
  }

  @Test
  fun `invoke with id containing spaces should preserve spaces`() {
    // Arrange - ID with spaces (though unusual)
    val dataType = DataType.Notes
    val idWithSpaces = "note with spaces"

    // Act
    val result = getCloudFileNameUseCase(dataType, idWithSpaces)

    // Assert
    assertEquals("note with spaces.no2", result)
  }

  @Test
  fun `invoke multiple times with same parameters should return same result`() {
    // Arrange
    val dataType = DataType.Birthdays
    val birthdayId = "consistent-id"

    // Act - Call multiple times
    val result1 = getCloudFileNameUseCase(dataType, birthdayId)
    val result2 = getCloudFileNameUseCase(dataType, birthdayId)
    val result3 = getCloudFileNameUseCase(dataType, birthdayId)

    // Assert - All results should be identical
    assertEquals("consistent-id.bi2", result1)
    assertEquals(result1, result2)
    assertEquals(result2, result3)
  }

  @Test
  fun `invoke with all data types should use correct extension for each`() {
    // Arrange - Test all data types with the same ID to verify extension mapping
    val testId = "test-id"
    val expectedResults = mapOf(
      DataType.Reminders to "test-id.ta2",
      DataType.Birthdays to "test-id.bi2",
      DataType.Notes to "test-id.no2",
      DataType.Groups to "test-id.gr2",
      DataType.Places to "test-id.pl2",
      DataType.Settings to "test-id.settings"
    )

    // Act & Assert - Verify each data type produces correct extension
    expectedResults.forEach { (dataType, expectedFileName) ->
      val result = getCloudFileNameUseCase(dataType, testId)
      assertEquals("Failed for $dataType", expectedFileName, result)
    }
  }
}

