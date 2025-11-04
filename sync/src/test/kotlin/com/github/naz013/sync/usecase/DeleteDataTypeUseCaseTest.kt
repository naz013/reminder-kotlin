package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.cloudapi.Source
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.DataType
import com.github.naz013.sync.usecase.delete.DeleteDataTypeUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [com.github.naz013.sync.usecase.delete.DeleteDataTypeUseCase].
 *
 * Tests the deletion of cloud files for a specific data type,
 * including interactions with cloud APIs and metadata repository cleanup.
 */
class DeleteDataTypeUseCaseTest {

  private lateinit var remoteFileMetadataRepository: RemoteFileMetadataRepository
  private lateinit var findAllFilesToDeleteUseCase: FindAllFilesToDeleteUseCase
  private lateinit var deleteDataTypeUseCase: DeleteDataTypeUseCase

  private lateinit var mockCloudFileApi: CloudFileApi

  @Before
  fun setUp() {
    remoteFileMetadataRepository = mockk(relaxed = true)
    findAllFilesToDeleteUseCase = mockk()
    mockCloudFileApi = mockk(relaxed = true)

    deleteDataTypeUseCase = DeleteDataTypeUseCase(
      remoteFileMetadataRepository = remoteFileMetadataRepository,
      findAllFilesToDeleteUseCase = findAllFilesToDeleteUseCase
    )
  }

  @Test
  fun invoke_withNoFilesToDelete_shouldReturnEarlyWithoutDeletion() = runBlocking {
    // Arrange - FindAllFilesToDeleteUseCase returns null (no files found)
    val dataType = DataType.Reminders

    coEvery { findAllFilesToDeleteUseCase(dataType) } returns null

    // Act
    deleteDataTypeUseCase(dataType)

    // Assert - No deletion should occur
    coVerify(exactly = 0) { mockCloudFileApi.deleteFile(any()) }
    coVerify(exactly = 0) { remoteFileMetadataRepository.delete(any()) }
  }

  @Test
  fun invoke_withSingleFileInOneSource_shouldDeleteFileAndMetadata() = runBlocking {
    // Arrange - Single reminder file in Google Drive
    val dataType = DataType.Reminders
    val cloudFile = CloudFile(
      id = "gdrive-file-id-123",
      name = "reminder-uuid-123.ta2",
      fileExtension = ".ta2",
      lastModified = 1698765432000L,
      size = 2048,
      version = 1L,
      rev = "rev1"
    )

    every { mockCloudFileApi.source } returns Source.GoogleDrive
    coEvery { mockCloudFileApi.deleteFile(any()) } returns true

    val searchResult = FindAllFilesToDeleteUseCase.SearchResult(
      sources = listOf(
        FindAllFilesToDeleteUseCase.CloudFilesWithSource(
          source = mockCloudFileApi,
          cloudFiles = listOf(cloudFile)
        )
      )
    )

    coEvery { findAllFilesToDeleteUseCase(dataType) } returns searchResult

    // Act
    deleteDataTypeUseCase(dataType)

    // Assert - File should be deleted from cloud and metadata removed
    coVerify(exactly = 1) { mockCloudFileApi.deleteFile("reminder-uuid-123.ta2") }
    coVerify(exactly = 1) { remoteFileMetadataRepository.delete("gdrive-file-id-123") }
  }

  @Test
  fun invoke_withMultipleFilesInOneSource_shouldDeleteAllFiles() = runBlocking {
    // Arrange - Multiple birthday files in Dropbox
    val dataType = DataType.Birthdays
    val cloudFile1 = CloudFile(
      id = "dropbox-file-1",
      name = "birthday-uuid-1.bi2",
      fileExtension = ".bi2",
      lastModified = 1698800000000L,
      size = 1024,
      version = 2L,
      rev = "rev2"
    )
    val cloudFile2 = CloudFile(
      id = "dropbox-file-2",
      name = "birthday-uuid-2.bi2",
      fileExtension = ".bi2",
      lastModified = 1698800001000L,
      size = 1536,
      version = 3L,
      rev = "rev3"
    )
    val cloudFile3 = CloudFile(
      id = "dropbox-file-3",
      name = "birthday-uuid-3.bi2",
      fileExtension = ".bi2",
      lastModified = 1698800002000L,
      size = 2048,
      version = 1L,
      rev = "rev1"
    )

    every { mockCloudFileApi.source } returns Source.Dropbox
    coEvery { mockCloudFileApi.deleteFile(any()) } returns true

    val searchResult = FindAllFilesToDeleteUseCase.SearchResult(
      sources = listOf(
        FindAllFilesToDeleteUseCase.CloudFilesWithSource(
          source = mockCloudFileApi,
          cloudFiles = listOf(cloudFile1, cloudFile2, cloudFile3)
        )
      )
    )

    coEvery { findAllFilesToDeleteUseCase(dataType) } returns searchResult

    // Act
    deleteDataTypeUseCase(dataType)

    // Assert - All three files should be deleted
    coVerify(exactly = 1) { mockCloudFileApi.deleteFile("birthday-uuid-1.bi2") }
    coVerify(exactly = 1) { mockCloudFileApi.deleteFile("birthday-uuid-2.bi2") }
    coVerify(exactly = 1) { mockCloudFileApi.deleteFile("birthday-uuid-3.bi2") }
    coVerify(exactly = 1) { remoteFileMetadataRepository.delete("dropbox-file-1") }
    coVerify(exactly = 1) { remoteFileMetadataRepository.delete("dropbox-file-2") }
    coVerify(exactly = 1) { remoteFileMetadataRepository.delete("dropbox-file-3") }
  }

  @Test
  fun invoke_withFilesInMultipleSources_shouldDeleteFromAllSources() = runBlocking {
    // Arrange - Note files in both Google Drive and Dropbox
    val dataType = DataType.Notes
    val mockGDriveApi = mockk<CloudFileApi>(relaxed = true)
    val mockDropboxApi = mockk<CloudFileApi>(relaxed = true)

    val gdriveFile = CloudFile(
      id = "gdrive-note-1",
      name = "note-key-1.no2",
      fileExtension = ".no2",
      lastModified = 1698850000000L,
      size = 512,
      version = 1L,
      rev = "gdrive-rev1"
    )
    val dropboxFile = CloudFile(
      id = "dropbox-note-1",
      name = "note-key-1.no2",
      fileExtension = ".no2",
      lastModified = 1698850001000L,
      size = 512,
      version = 1L,
      rev = "dropbox-rev1"
    )

    every { mockGDriveApi.source } returns Source.GoogleDrive
    every { mockDropboxApi.source } returns Source.Dropbox
    coEvery { mockGDriveApi.deleteFile(any()) } returns true
    coEvery { mockDropboxApi.deleteFile(any()) } returns true

    val searchResult = FindAllFilesToDeleteUseCase.SearchResult(
      sources = listOf(
        FindAllFilesToDeleteUseCase.CloudFilesWithSource(
          source = mockGDriveApi,
          cloudFiles = listOf(gdriveFile)
        ),
        FindAllFilesToDeleteUseCase.CloudFilesWithSource(
          source = mockDropboxApi,
          cloudFiles = listOf(dropboxFile)
        )
      )
    )

    coEvery { findAllFilesToDeleteUseCase(dataType) } returns searchResult

    // Act
    deleteDataTypeUseCase(dataType)

    // Assert - Files should be deleted from both sources
    coVerify(exactly = 1) { mockGDriveApi.deleteFile("note-key-1.no2") }
    coVerify(exactly = 1) { mockDropboxApi.deleteFile("note-key-1.no2") }
    coVerify(exactly = 1) { remoteFileMetadataRepository.delete("gdrive-note-1") }
    coVerify(exactly = 1) { remoteFileMetadataRepository.delete("dropbox-note-1") }
  }

  @Test
  fun invoke_withDifferentDataTypes_shouldHandleCorrectly() = runBlocking {
    // Arrange - Test with different data types (Groups and Places)

    // Test 1: Groups
    val groupFile = CloudFile(
      id = "group-file-1",
      name = "group-uuid-1.gr2",
      fileExtension = ".gr2",
      lastModified = 1698900000000L,
      size = 768,
      version = 2L,
      rev = "rev2"
    )
    every { mockCloudFileApi.source } returns Source.GoogleDrive
    coEvery { mockCloudFileApi.deleteFile(any()) } returns true

    val groupSearchResult = FindAllFilesToDeleteUseCase.SearchResult(
      sources = listOf(
        FindAllFilesToDeleteUseCase.CloudFilesWithSource(
          source = mockCloudFileApi,
          cloudFiles = listOf(groupFile)
        )
      )
    )
    coEvery { findAllFilesToDeleteUseCase(DataType.Groups) } returns groupSearchResult

    // Act - Delete Groups
    deleteDataTypeUseCase(DataType.Groups)

    // Assert
    coVerify(exactly = 1) { mockCloudFileApi.deleteFile("group-uuid-1.gr2") }
    coVerify(exactly = 1) { remoteFileMetadataRepository.delete("group-file-1") }

    // Test 2: Places
    val placeFile = CloudFile(
      id = "place-file-1",
      name = "place-id-1.pl2",
      fileExtension = ".pl2",
      lastModified = 1698950000000L,
      size = 256,
      version = 1L,
      rev = "rev1"
    )

    val placeSearchResult = FindAllFilesToDeleteUseCase.SearchResult(
      sources = listOf(
        FindAllFilesToDeleteUseCase.CloudFilesWithSource(
          source = mockCloudFileApi,
          cloudFiles = listOf(placeFile)
        )
      )
    )
    coEvery { findAllFilesToDeleteUseCase(DataType.Places) } returns placeSearchResult

    // Act - Delete Places
    deleteDataTypeUseCase(DataType.Places)

    // Assert
    coVerify(exactly = 1) { mockCloudFileApi.deleteFile("place-id-1.pl2") }
    coVerify(exactly = 1) { remoteFileMetadataRepository.delete("place-file-1") }
  }

  @Test(expected = Exception::class)
  fun invoke_whenCloudApiDeleteFails_shouldPropagateException() {
    // Arrange - Cloud API deletion throws exception
    val dataType = DataType.Reminders
    val cloudFile = CloudFile(
      id = "failing-file-id",
      name = "failing-reminder.ta2",
      fileExtension = ".ta2",
      lastModified = 1699000000000L,
      size = 1024,
      version = 1L,
      rev = "rev1"
    )

    every { mockCloudFileApi.source } returns Source.GoogleDrive
    coEvery { mockCloudFileApi.deleteFile(any()) } throws RuntimeException("Network error during deletion")

    val searchResult = FindAllFilesToDeleteUseCase.SearchResult(
      sources = listOf(
        FindAllFilesToDeleteUseCase.CloudFilesWithSource(
          source = mockCloudFileApi,
          cloudFiles = listOf(cloudFile)
        )
      )
    )

    coEvery { findAllFilesToDeleteUseCase(dataType) } returns searchResult

    // Act - Should throw exception
    runBlocking {
      deleteDataTypeUseCase(dataType)
    }

    // Assert - Exception should be thrown
  }

  @Test
  fun invoke_withSettingsDataType_shouldDeleteSettingsFiles() = runBlocking {
    // Arrange - Settings files (special case with "app" prefix)
    val dataType = DataType.Settings
    val settingsFile = CloudFile(
      id = "settings-file-id",
      name = "app.settings",
      fileExtension = ".settings",
      lastModified = 1699050000000L,
      size = 128,
      version = 5L,
      rev = "settings-rev-5"
    )

    every { mockCloudFileApi.source } returns Source.GoogleDrive
    coEvery { mockCloudFileApi.deleteFile(any()) } returns true

    val searchResult = FindAllFilesToDeleteUseCase.SearchResult(
      sources = listOf(
        FindAllFilesToDeleteUseCase.CloudFilesWithSource(
          source = mockCloudFileApi,
          cloudFiles = listOf(settingsFile)
        )
      )
    )

    coEvery { findAllFilesToDeleteUseCase(dataType) } returns searchResult

    // Act
    deleteDataTypeUseCase(dataType)

    // Assert - Settings file should be deleted
    coVerify(exactly = 1) { mockCloudFileApi.deleteFile("app.settings") }
    coVerify(exactly = 1) { remoteFileMetadataRepository.delete("settings-file-id") }
  }

  @Test
  fun invoke_withLargeNumberOfFiles_shouldDeleteAllSequentially() = runBlocking {
    // Arrange - Simulate deleting many files (e.g., 10 reminders)
    val dataType = DataType.Reminders
    val cloudFiles = (1..10).map { index ->
      CloudFile(
        id = "file-id-$index",
        name = "reminder-uuid-$index.ta2",
        fileExtension = ".ta2",
        lastModified = 1699100000000L + index,
        size = 1024 * index,
        version = index.toLong(),
        rev = "rev-$index"
      )
    }

    every { mockCloudFileApi.source } returns Source.GoogleDrive
    coEvery { mockCloudFileApi.deleteFile(any()) } returns true

    val searchResult = FindAllFilesToDeleteUseCase.SearchResult(
      sources = listOf(
        FindAllFilesToDeleteUseCase.CloudFilesWithSource(
          source = mockCloudFileApi,
          cloudFiles = cloudFiles
        )
      )
    )

    coEvery { findAllFilesToDeleteUseCase(dataType) } returns searchResult

    // Act
    deleteDataTypeUseCase(dataType)

    // Assert - All 10 files should be deleted
    cloudFiles.forEach { cloudFile ->
      coVerify(exactly = 1) { mockCloudFileApi.deleteFile(cloudFile.name) }
      coVerify(exactly = 1) { remoteFileMetadataRepository.delete(cloudFile.id) }
    }

    // Verify total counts
    coVerify(exactly = 10) { mockCloudFileApi.deleteFile(any()) }
    coVerify(exactly = 10) { remoteFileMetadataRepository.delete(any()) }
  }
}
