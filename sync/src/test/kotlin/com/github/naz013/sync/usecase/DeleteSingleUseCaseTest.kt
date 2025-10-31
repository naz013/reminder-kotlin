package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.cloudapi.Source
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.DataType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [DeleteSingleUseCase].
 *
 * Tests the deletion of a single cloud file by ID and data type,
 * including special handling for Settings, multi-cloud deletion,
 * and error handling scenarios.
 */
class DeleteSingleUseCaseTest {

  private lateinit var remoteFileMetadataRepository: RemoteFileMetadataRepository
  private lateinit var getCloudFileNameUseCase: GetCloudFileNameUseCase
  private lateinit var getAllowedCloudApisUseCase: GetAllowedCloudApisUseCase
  private lateinit var deleteSingleUseCase: DeleteSingleUseCase

  private lateinit var mockCloudFileApi: CloudFileApi

  @Before
  fun setUp() {
    remoteFileMetadataRepository = mockk(relaxed = true)
    getCloudFileNameUseCase = mockk()
    getAllowedCloudApisUseCase = mockk()
    mockCloudFileApi = mockk(relaxed = true)
    deleteSingleUseCase = DeleteSingleUseCase(
      remoteFileMetadataRepository = remoteFileMetadataRepository,
      getCloudFileNameUseCase = getCloudFileNameUseCase,
      getAllowedCloudApisUseCase = getAllowedCloudApisUseCase
    )
  }

  @Test
  fun invoke_withReminderType_shouldDeleteFromAllCloudApisAndMetadata() {
    runBlocking {
      // Arrange - Delete a reminder from Google Drive
      val dataType = DataType.Reminders
      val reminderId = "reminder-uuid-12345"
      val fileName = "$reminderId.ta2"

      every { getCloudFileNameUseCase(dataType, reminderId) } returns fileName
      every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockCloudFileApi)
      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.deleteFile(fileName) } returns true

      // Act
      deleteSingleUseCase(dataType, reminderId)

      // Assert - Should delete from cloud and remove metadata
      coVerify(exactly = 1) { mockCloudFileApi.deleteFile(fileName) }
      coVerify(exactly = 1) { remoteFileMetadataRepository.deleteByLocalUuId(reminderId) }
      every { getCloudFileNameUseCase(dataType, reminderId) }
    }
  }

  @Test
  fun invoke_withBirthdayType_shouldDeleteWithCorrectFileExtension() {
    runBlocking {
      // Arrange - Delete a birthday file (.bi2 extension)
      val dataType = DataType.Birthdays
      val birthdayId = "birthday-uuid-67890"
      val fileName = "$birthdayId.bi2"

      every { getCloudFileNameUseCase(dataType, birthdayId) } returns fileName
      every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockCloudFileApi)
      coEvery { mockCloudFileApi.deleteFile(fileName) } returns true

      // Act
      deleteSingleUseCase(dataType, birthdayId)

      // Assert - Correct file name with .bi2 extension
      coVerify(exactly = 1) { mockCloudFileApi.deleteFile(fileName) }
      coVerify(exactly = 1) { remoteFileMetadataRepository.deleteByLocalUuId(birthdayId) }
    }
  }

  @Test
  fun invoke_withSettingsType_shouldReturnEarlyWithoutDeletion() {
    runBlocking {
      // Arrange - Settings should NOT be deleted from cloud
      val dataType = DataType.Settings
      val settingsId = "settings-uuid"

      // Act
      deleteSingleUseCase(dataType, settingsId)

      // Assert - No cloud deletion or metadata removal should occur
      coVerify(exactly = 0) { mockCloudFileApi.deleteFile(any()) }
      coVerify(exactly = 0) { remoteFileMetadataRepository.deleteByLocalUuId(any()) }
      coVerify(exactly = 0) { getCloudFileNameUseCase(any(), any()) }
    }
  }

  @Test
  fun invoke_withMultipleCloudApis_shouldDeleteFromAllSources() {
    runBlocking {
      // Arrange - Note file exists on both Google Drive and Dropbox
      val dataType = DataType.Notes
      val noteId = "note-key-abc123"
      val fileName = "$noteId.no2"
      val mockGDriveApi = mockk<CloudFileApi>(relaxed = true)
      val mockDropboxApi = mockk<CloudFileApi>(relaxed = true)

      every { getCloudFileNameUseCase(dataType, noteId) } returns fileName
      every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockGDriveApi, mockDropboxApi)
      every { mockGDriveApi.source } returns Source.GoogleDrive
      every { mockDropboxApi.source } returns Source.Dropbox
      coEvery { mockGDriveApi.deleteFile(fileName) } returns true
      coEvery { mockDropboxApi.deleteFile(fileName) } returns true

      // Act
      deleteSingleUseCase(dataType, noteId)

      // Assert - Should delete from both cloud providers
      coVerify(exactly = 1) { mockGDriveApi.deleteFile(fileName) }
      coVerify(exactly = 1) { mockDropboxApi.deleteFile(fileName) }
      coVerify(exactly = 1) { remoteFileMetadataRepository.deleteByLocalUuId(noteId) }
    }
  }

  @Test
  fun invoke_withNoCloudApis_shouldOnlyDeleteMetadata() {
    runBlocking {
      // Arrange - No cloud APIs configured (empty list)
      val dataType = DataType.Reminders
      val reminderId = "reminder-uuid-999"
      val fileName = "$reminderId.ta2"

      every { getCloudFileNameUseCase(dataType, reminderId) } returns fileName
      every { getAllowedCloudApisUseCase.invoke() } returns emptyList()

      // Act
      deleteSingleUseCase(dataType, reminderId)

      // Assert - No cloud deletion, but metadata should still be removed
      coVerify(exactly = 0) { mockCloudFileApi.deleteFile(any()) }
      coVerify(exactly = 1) { remoteFileMetadataRepository.deleteByLocalUuId(reminderId) }
    }
  }

  @Test
  fun invoke_withGroupType_shouldHandleGroupFileExtension() {
    runBlocking {
      // Arrange - Delete a reminder group (.gr2 extension)
      val dataType = DataType.Groups
      val groupId = "group-uuid-xyz789"
      val fileName = "$groupId.gr2"

      every { getCloudFileNameUseCase(dataType, groupId) } returns fileName
      every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockCloudFileApi)
      coEvery { mockCloudFileApi.deleteFile(fileName) } returns true

      // Act
      deleteSingleUseCase(dataType, groupId)

      // Assert - Correct file name with .gr2 extension
      coVerify(exactly = 1) { mockCloudFileApi.deleteFile(fileName) }
      coVerify(exactly = 1) { remoteFileMetadataRepository.deleteByLocalUuId(groupId) }
    }
  }

  @Test
  fun invoke_withPlaceType_shouldHandlePlaceFileExtension() {
    runBlocking {
      // Arrange - Delete a place (.pl2 extension)
      val dataType = DataType.Places
      val placeId = "place-id-location-456"
      val fileName = "$placeId.pl2"

      every { getCloudFileNameUseCase(dataType, placeId) } returns fileName
      every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockCloudFileApi)
      coEvery { mockCloudFileApi.deleteFile(fileName) } returns true

      // Act
      deleteSingleUseCase(dataType, placeId)

      // Assert - Correct file name with .pl2 extension
      coVerify(exactly = 1) { mockCloudFileApi.deleteFile(fileName) }
      coVerify(exactly = 1) { remoteFileMetadataRepository.deleteByLocalUuId(placeId) }
    }
  }

  @Test
  fun invoke_whenCloudApiDeleteFails_shouldPropagateException() {
    // Arrange - Cloud API throws exception during deletion
    val dataType = DataType.Reminders
    val reminderId = "failing-reminder-id"
    val fileName = "$reminderId.ta2"

    every { getCloudFileNameUseCase(dataType, reminderId) } returns fileName
    every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockCloudFileApi)
    coEvery { mockCloudFileApi.deleteFile(fileName) } throws RuntimeException("Network error during deletion")

    // Act & Assert - Should propagate exception
    var exceptionThrown = false
    try {
      runBlocking {
        deleteSingleUseCase(dataType, reminderId)
      }
    } catch (e: RuntimeException) {
      exceptionThrown = true
      assert(e.message?.contains("Network error") == true)
    }
    assert(exceptionThrown) { "Expected RuntimeException to be thrown" }
  }

  @Test
  fun invoke_whenGetCloudFileNameFails_shouldPropagateException() {
    // Arrange - GetCloudFileNameUseCase throws exception
    val dataType = DataType.Notes
    val noteId = "invalid-note-id"

    every {
      getCloudFileNameUseCase(
        dataType,
        noteId
      )
    } throws IllegalArgumentException("Invalid ID format")
    every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockCloudFileApi)

    // Act & Assert - Should propagate exception
    var exceptionThrown = false
    try {
      runBlocking {
        deleteSingleUseCase(dataType, noteId)
      }
    } catch (e: IllegalArgumentException) {
      exceptionThrown = true
      assert(e.message?.contains("Invalid ID") == true)
    }
    assert(exceptionThrown) { "Expected IllegalArgumentException to be thrown" }
  }

  @Test
  fun invoke_whenMetadataDeleteFails_shouldPropagateException() {
    // Arrange - Repository throws exception during metadata deletion
    val dataType = DataType.Birthdays
    val birthdayId = "birthday-with-metadata-error"
    val fileName = "$birthdayId.bi2"

    every { getCloudFileNameUseCase(dataType, birthdayId) } returns fileName
    every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockCloudFileApi)
    coEvery { mockCloudFileApi.deleteFile(fileName) } returns true
    coEvery { remoteFileMetadataRepository.deleteByLocalUuId(birthdayId) } throws
      RuntimeException("Database error")

    // Act & Assert - Should propagate exception
    var exceptionThrown = false
    try {
      runBlocking {
        deleteSingleUseCase(dataType, birthdayId)
      }
    } catch (e: RuntimeException) {
      exceptionThrown = true
      assert(e.message?.contains("Database error") == true)
    }
    assert(exceptionThrown) { "Expected RuntimeException to be thrown" }
  }

  @Test
  fun invoke_withSpecialCharactersInId_shouldHandleCorrectly() {
    runBlocking {
      // Arrange - ID with special characters (UUID format)
      val dataType = DataType.Reminders
      val reminderId = "550e8400-e29b-41d4-a716-446655440000"  // Standard UUID
      val fileName = "$reminderId.ta2"

      every { getCloudFileNameUseCase(dataType, reminderId) } returns fileName
      every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockCloudFileApi)
      coEvery { mockCloudFileApi.deleteFile(fileName) } returns true

      // Act
      deleteSingleUseCase(dataType, reminderId)

      // Assert - Should handle UUID format correctly
      coVerify(exactly = 1) { mockCloudFileApi.deleteFile(fileName) }
      coVerify(exactly = 1) { remoteFileMetadataRepository.deleteByLocalUuId(reminderId) }
    }
  }

  @Test
  fun invoke_withDifferentDataTypes_shouldDeleteSequentially() {
    runBlocking {
      // Arrange - Test multiple data types in sequence
      val reminderId = "reminder-1"
      val birthdayId = "birthday-1"
      val noteId = "note-1"

      every { getCloudFileNameUseCase(DataType.Reminders, reminderId) } returns "$reminderId.ta2"
      every { getCloudFileNameUseCase(DataType.Birthdays, birthdayId) } returns "$birthdayId.bi2"
      every { getCloudFileNameUseCase(DataType.Notes, noteId) } returns "$noteId.no2"
      every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockCloudFileApi)
      coEvery { mockCloudFileApi.deleteFile(any()) } returns true

      // Act - Delete different types
      deleteSingleUseCase(DataType.Reminders, reminderId)
      deleteSingleUseCase(DataType.Birthdays, birthdayId)
      deleteSingleUseCase(DataType.Notes, noteId)

      // Assert - All three should be deleted with correct file names
      coVerify(exactly = 1) { mockCloudFileApi.deleteFile("$reminderId.ta2") }
      coVerify(exactly = 1) { mockCloudFileApi.deleteFile("$birthdayId.bi2") }
      coVerify(exactly = 1) { mockCloudFileApi.deleteFile("$noteId.no2") }
      coVerify(exactly = 3) { remoteFileMetadataRepository.deleteByLocalUuId(any()) }
    }
  }
}
