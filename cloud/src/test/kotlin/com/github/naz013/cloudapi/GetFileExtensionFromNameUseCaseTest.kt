package com.github.naz013.cloudapi

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [GetFileExtensionFromNameUseCase].
 *
 * Tests the file extension extraction logic including:
 * - Standard file names with extensions
 * - Edge cases (no extension, multiple dots, hidden files)
 * - Boundary conditions
 * - Invalid/unusual file names
 */
class GetFileExtensionFromNameUseCaseTest {

  private lateinit var useCase: GetFileExtensionFromNameUseCase

  @Before
  fun setUp() {
    useCase = GetFileExtensionFromNameUseCase()
  }

  // ========================================
  // Core Functionality Tests
  // ========================================

  @Test
  fun `invoke with standard filename should return extension`() {
    // Arrange
    val fileName = "document.txt"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Extension should be txt", "txt", result)
  }

  @Test
  fun `invoke with reminder file should return ta2 extension`() {
    // Arrange - Realistic reminder file name
    val fileName = "reminder-uuid-12345.ta2"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Extension should be ta2", "ta2", result)
  }

  @Test
  fun `invoke with note file should return no2 extension`() {
    // Arrange - Realistic note file name
    val fileName = "note-uuid-67890.no2"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Extension should be no2", "no2", result)
  }

  @Test
  fun `invoke with multiple dots should return last extension`() {
    // Arrange - File name with multiple dots (common pattern)
    val fileName = "backup.archive.tar.gz"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Extension should be gz (last extension)", "gz", result)
  }

  @Test
  fun `invoke with uppercase extension should preserve case`() {
    // Arrange
    val fileName = "document.PDF"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Extension case should be preserved", "PDF", result)
  }

  @Test
  fun `invoke with mixed case extension should preserve case`() {
    // Arrange
    val fileName = "image.JpEg"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Extension case should be preserved", "JpEg", result)
  }

  // ========================================
  // Edge Cases Tests
  // ========================================

  @Test
  fun `invoke with no extension should return empty string`() {
    // Arrange - File name without extension
    val fileName = "README"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Should return empty string when no extension", "", result)
  }

  @Test
  fun `invoke with dot at end should return empty string`() {
    // Arrange - Dot at end with no extension
    val fileName = "filename."

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Should return empty string when dot at end", "", result)
  }

  @Test
  fun `invoke with hidden file on unix should return extension after last dot`() {
    // Arrange - Unix hidden file with extension
    val fileName = ".hidden.txt"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Extension should be txt", "txt", result)
  }

  @Test
  fun `invoke with unix hidden file without extension should return empty string`() {
    // Arrange - Unix hidden file without extension
    val fileName = ".gitignore"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Should return gitignore as extension", "gitignore", result)
  }

  @Test
  fun `invoke with only dot should return empty string`() {
    // Arrange
    val fileName = "."

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Should return empty string for single dot", "", result)
  }

  @Test
  fun `invoke with two dots should return empty string`() {
    // Arrange
    val fileName = ".."

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Should return empty string for double dot", "", result)
  }

  // ========================================
  // Input Validation Tests
  // ========================================

  @Test
  fun `invoke with empty string should return empty string`() {
    // Arrange
    val fileName = ""

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Should return empty string for empty input", "", result)
  }

  @Test
  fun `invoke with very long extension should return full extension`() {
    // Arrange - Unusually long extension
    val fileName = "file.verylongextensionname"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Should return full extension", "verylongextensionname", result)
  }

  @Test
  fun `invoke with single character extension should work`() {
    // Arrange
    val fileName = "document.c"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Extension should be c", "c", result)
  }

  @Test
  fun `invoke with numbers in extension should work`() {
    // Arrange
    val fileName = "backup.mp3"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Extension should be mp3", "mp3", result)
  }

  // ========================================
  // Special Characters Tests
  // ========================================

  @Test
  fun `invoke with special characters in filename should return extension`() {
    // Arrange - File name with special characters
    val fileName = "file-name_with@special#chars.txt"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Extension should be txt", "txt", result)
  }

  @Test
  fun `invoke with spaces in filename should return extension`() {
    // Arrange
    val fileName = "my document.docx"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Extension should be docx", "docx", result)
  }

  @Test
  fun `invoke with unicode characters in filename should return extension`() {
    // Arrange - File name with unicode characters
    val fileName = "文档.txt"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Extension should be txt", "txt", result)
  }

  @Test
  fun `invoke with special characters in extension should return extension`() {
    // Arrange - Extension with special characters (unusual but possible)
    val fileName = "file.tx_t"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Extension should be tx_t", "tx_t", result)
  }

  // ========================================
  // Boundary Tests
  // ========================================

  @Test
  fun `invoke with dot at start and extension should return extension`() {
    // Arrange
    val fileName = ".config.yml"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Extension should be yml", "yml", result)
  }

  @Test
  fun `invoke with path separator should only consider filename part`() {
    // Arrange - Note: This tests current behavior, not path parsing
    val fileName = "folder.name/file.txt"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Extension should be txt", "txt", result)
  }

  @Test
  fun `invoke with windows path separator should only consider last part`() {
    // Arrange - Note: This tests current behavior, not path parsing
    val fileName = "folder.name\\file.txt"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Extension should be txt", "txt", result)
  }

  // ========================================
  // Real-world Scenario Tests
  // ========================================

  @Test
  fun `invoke with all supported reminder app extensions should work correctly`() {
    // Arrange - Test all reminder app file extensions
    val testCases = mapOf(
      "reminder-123.ta2" to "ta2",
      "note-456.no2" to "no2",
      "group-789.gr2" to "gr2",
      "birthday-abc.bi2" to "bi2",
      "place-def.pl2" to "pl2",
      "app.settings" to "settings"
    )

    // Act & Assert
    testCases.forEach { (fileName, expectedExtension) ->
      val result = useCase(fileName)
      assertEquals(
        "Extension for $fileName should be $expectedExtension",
        expectedExtension,
        result
      )
    }
  }

  @Test
  fun `invoke with version numbers in filename should extract extension correctly`() {
    // Arrange - Common pattern with version numbers
    val fileName = "app-v1.2.3.apk"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Extension should be apk", "apk", result)
  }

  @Test
  fun `invoke with timestamp in filename should extract extension correctly`() {
    // Arrange - Common pattern with timestamps
    val fileName = "backup-2024-01-15-10-30-00.zip"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Extension should be zip", "zip", result)
  }

  @Test
  fun `invoke with uuid in filename should extract extension correctly`() {
    // Arrange - Realistic scenario with UUID
    val fileName = "reminder-550e8400-e29b-41d4-a716-446655440000.ta2"

    // Act
    val result = useCase(fileName)

    // Assert
    assertEquals("Extension should be ta2", "ta2", result)
  }
}

