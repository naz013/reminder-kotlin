package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.cloudapi.Source
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.RemoteFileMetadata
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.DataPostProcessor
import com.github.naz013.sync.DataType
import com.github.naz013.sync.SyncDataConverter
import com.github.naz013.sync.SyncResult
import com.github.naz013.sync.local.DataTypeRepositoryCaller
import com.github.naz013.sync.local.DataTypeRepositoryCallerFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

/**
 * Unit tests for [DownloadSingleUseCase].
 *
 * Tests the downloading of single items from cloud storage,
 * including file retrieval, parsing, local storage, metadata tracking,
 * and error handling scenarios.
 */
class DownloadSingleUseCaseTest {

  private lateinit var dataTypeRepositoryCallerFactory: DataTypeRepositoryCallerFactory
  private lateinit var syncDataConverter: SyncDataConverter
  private lateinit var remoteFileMetadataRepository: RemoteFileMetadataRepository
  private lateinit var createRemoteFileMetadataUseCase: CreateRemoteFileMetadataUseCase
  private lateinit var findNewestCloudApiSourceUseCase: FindNewestCloudApiSourceUseCase
  private lateinit var dataPostProcessor: DataPostProcessor
  private lateinit var downloadSingleUseCase: DownloadSingleUseCase

  private lateinit var mockRepositoryCaller: DataTypeRepositoryCaller<Any>
  private lateinit var mockCloudFileApi: CloudFileApi

  @Before
  fun setUp() {
    dataTypeRepositoryCallerFactory = mockk()
    syncDataConverter = mockk()
    remoteFileMetadataRepository = mockk(relaxed = true)
    createRemoteFileMetadataUseCase = mockk()
    findNewestCloudApiSourceUseCase = mockk()
    dataPostProcessor = mockk(relaxed = true)
    mockRepositoryCaller = mockk(relaxed = true)
    mockCloudFileApi = mockk()

    downloadSingleUseCase = DownloadSingleUseCase(
      dataTypeRepositoryCallerFactory = dataTypeRepositoryCallerFactory,
      syncDataConverter = syncDataConverter,
      remoteFileMetadataRepository = remoteFileMetadataRepository,
      createRemoteFileMetadataUseCase = createRemoteFileMetadataUseCase,
      findNewestCloudApiSourceUseCase = findNewestCloudApiSourceUseCase,
      dataPostProcessor = dataPostProcessor
    )
  }

  @Test
  fun `invoke with reminder should download parse and save successfully`() {
    runBlocking {
      // Arrange - Download a reminder from Google Drive
      val dataType = DataType.Reminders
      val reminderId = "reminder-uuid-12345"
      val reminder = Reminder(
        summary = "Doctor appointment",
        uuId = reminderId,
        groupUuId = "default-group",
        eventTime = "2025-10-30 14:00",
        reminderType = 0
      )
      val cloudFile = CloudFile(
        id = "gdrive-file-id-123",
        name = "$reminderId.ta2",
        fileExtension = ".ta2",
        lastModified = 1698765432000L,
        size = 2048,
        version = 3L,
        rev = "rev3"
      )
      val inputStream = ByteArrayInputStream("reminder data".toByteArray())
      val remoteFileMetadata = RemoteFileMetadata(
        id = "gdrive-file-id-123",
        name = "$reminderId.ta2",
        lastModified = 1698765432000L,
        size = 2048,
        source = "GDRIVE",
        localUuId = reminderId,
        fileExtension = ".ta2",
        version = 3L,
        rev = "rev3"
      )

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { findNewestCloudApiSourceUseCase(dataType, reminderId) } returns
        FindNewestCloudApiSourceUseCase.SearchResult(mockCloudFileApi, cloudFile)
      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.downloadFile(cloudFile) } returns inputStream
      coEvery { syncDataConverter.parse(inputStream, Reminder::class.java) } returns reminder
      coEvery { createRemoteFileMetadataUseCase(Source.GoogleDrive.value, cloudFile, reminder) } returns remoteFileMetadata

      // Act
      val result = downloadSingleUseCase(dataType, reminderId)

      // Assert - Should successfully download and save
      assertTrue(result is SyncResult.Success)
      val successResult = result as SyncResult.Success
      assertTrue(successResult.success)
      assertEquals(1, successResult.downloaded.size)
      assertEquals(dataType, successResult.downloaded[0].dataType)
      assertEquals(reminderId, successResult.downloaded[0].id)

      coVerify(exactly = 1) { mockCloudFileApi.downloadFile(cloudFile) }
      coVerify(exactly = 1) { syncDataConverter.parse(inputStream, Reminder::class.java) }
      coVerify(exactly = 1) { mockRepositoryCaller.insertOrUpdate(reminder) }
      coVerify(exactly = 1) { dataPostProcessor.process(dataType, reminder) }
      coVerify(exactly = 1) { remoteFileMetadataRepository.save(remoteFileMetadata) }
      coVerify(exactly = 1) { mockRepositoryCaller.updateSyncState(reminderId, SyncState.Synced) }
    }
  }

  @Test
  fun `invoke when no cloud file found should return skipped`() {
    runBlocking {
      // Arrange - FindNewestCloudApiSourceUseCase returns null (no file found)
      val dataType = DataType.Reminders
      val reminderId = "non-existent-reminder"

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { findNewestCloudApiSourceUseCase(dataType, reminderId) } returns null

      // Act
      val result = downloadSingleUseCase(dataType, reminderId)

      // Assert - Should skip download
      assertTrue(result is SyncResult.Skipped)
      coVerify(exactly = 0) { mockCloudFileApi.downloadFile(any()) }
      coVerify(exactly = 0) { mockRepositoryCaller.insertOrUpdate(any()) }
    }
  }

  @Test
  fun `invoke when download fails should return skipped`() {
    runBlocking {
      // Arrange - Cloud API returns null stream (download failed)
      val dataType = DataType.Birthdays
      val birthdayId = "birthday-uuid-67890"
      val cloudFile = CloudFile(
        id = "dropbox-file-id",
        name = "$birthdayId.gr2",
        fileExtension = ".gr2",
        lastModified = 1698800000000L,
        size = 1024,
        version = 1L,
        rev = "rev1"
      )

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { findNewestCloudApiSourceUseCase(dataType, birthdayId) } returns
        FindNewestCloudApiSourceUseCase.SearchResult(mockCloudFileApi, cloudFile)
      coEvery { mockCloudFileApi.downloadFile(cloudFile) } returns null

      // Act
      val result = downloadSingleUseCase(dataType, birthdayId)

      // Assert - Should skip when download fails
      assertTrue(result is SyncResult.Skipped)
      coVerify(exactly = 1) { mockCloudFileApi.downloadFile(cloudFile) }
      coVerify(exactly = 0) { syncDataConverter.parse(any(), any()) }
      coVerify(exactly = 0) { mockRepositoryCaller.insertOrUpdate(any()) }
    }
  }

  @Test
  fun `invoke with birthday type should use correct class for parsing`() {
    runBlocking {
      // Arrange - Download a birthday
      val dataType = DataType.Birthdays
      val birthdayId = "birthday-uuid-99999"
      val birthday = Birthday(
        name = "John Doe",
        uuId = birthdayId,
        date = "1990-05-15",
        day = 15,
        month = 5,
        showedYear = 1990,
        syncState = SyncState.Synced
      )
      val cloudFile = CloudFile(
        id = "cloud-id",
        name = "$birthdayId.gr2",
        fileExtension = ".gr2",
        lastModified = 1698850000000L,
        size = 512,
        version = 1L,
        rev = "rev1"
      )
      val inputStream = ByteArrayInputStream("birthday data".toByteArray())
      val remoteFileMetadata = RemoteFileMetadata(
        id = "cloud-id",
        name = "$birthdayId.gr2",
        lastModified = 1698850000000L,
        size = 512,
        source = "GDRIVE",
        localUuId = birthdayId,
        fileExtension = ".gr2",
        version = 1L,
        rev = "rev1"
      )

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { findNewestCloudApiSourceUseCase(dataType, birthdayId) } returns
        FindNewestCloudApiSourceUseCase.SearchResult(mockCloudFileApi, cloudFile)
      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.downloadFile(cloudFile) } returns inputStream
      coEvery { syncDataConverter.parse(inputStream, Birthday::class.java) } returns birthday
      coEvery { createRemoteFileMetadataUseCase(Source.GoogleDrive.value, cloudFile, birthday) } returns remoteFileMetadata

      // Act
      val result = downloadSingleUseCase(dataType, birthdayId)

      // Assert - Should use Birthday class for parsing
      assertTrue(result is SyncResult.Success)
      coVerify(exactly = 1) { syncDataConverter.parse(inputStream, Birthday::class.java) }
      coVerify(exactly = 1) { mockRepositoryCaller.insertOrUpdate(birthday) }
    }
  }

  @Test
  fun `invoke should call data post processor after saving`() {
    runBlocking {
      // Arrange - Ensure post-processing happens after save
      val dataType = DataType.Reminders
      val reminderId = "reminder-post-process"
      val reminder = Reminder(
        summary = "Test",
        uuId = reminderId,
        groupUuId = "group1",
        eventTime = "2025-11-01 09:00",
        reminderType = 1
      )
      val cloudFile = CloudFile(
        id = "file-id",
        name = "$reminderId.ta2",
        fileExtension = ".ta2",
        lastModified = 1699000000000L,
        size = 1024,
        version = 1L,
        rev = "rev1"
      )
      val inputStream = ByteArrayInputStream("data".toByteArray())
      val remoteFileMetadata = RemoteFileMetadata(
        id = "file-id",
        name = "$reminderId.ta2",
        lastModified = 1699000000000L,
        size = 1024,
        source = "GDRIVE",
        localUuId = reminderId,
        fileExtension = ".ta2",
        version = 1L,
        rev = "rev1"
      )

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { findNewestCloudApiSourceUseCase(dataType, reminderId) } returns
        FindNewestCloudApiSourceUseCase.SearchResult(mockCloudFileApi, cloudFile)
      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.downloadFile(cloudFile) } returns inputStream
      coEvery { syncDataConverter.parse(inputStream, Reminder::class.java) } returns reminder
      coEvery { createRemoteFileMetadataUseCase(Source.GoogleDrive.value, cloudFile, reminder) } returns remoteFileMetadata

      // Act
      downloadSingleUseCase(dataType, reminderId)

      // Assert - Post-processor should be called
      coVerify(exactly = 1) { dataPostProcessor.process(dataType, reminder) }
      coVerify(exactly = 1) { mockRepositoryCaller.insertOrUpdate(reminder) }
    }
  }

  @Test
  fun `invoke should save remote file metadata after successful download`() {
    runBlocking {
      // Arrange - Verify metadata tracking
      val dataType = DataType.Notes
      val noteId = "note-key-abc123"
      val note = mockk<Any>()
      val cloudFile = CloudFile(
        id = "note-cloud-id",
        name = "$noteId.no2",
        fileExtension = ".no2",
        lastModified = 1699050000000L,
        size = 768,
        version = 2L,
        rev = "note-rev-2"
      )
      val inputStream = ByteArrayInputStream("note content".toByteArray())
      val remoteFileMetadata = RemoteFileMetadata(
        id = "note-cloud-id",
        name = "$noteId.no2",
        lastModified = 1699050000000L,
        size = 768,
        source = "DROPBOX",
        localUuId = noteId,
        fileExtension = ".no2",
        version = 2L,
        rev = "note-rev-2"
      )

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { findNewestCloudApiSourceUseCase(dataType, noteId) } returns
        FindNewestCloudApiSourceUseCase.SearchResult(mockCloudFileApi, cloudFile)
      every { mockCloudFileApi.source } returns Source.Dropbox
      coEvery { mockCloudFileApi.downloadFile(cloudFile) } returns inputStream
      coEvery { syncDataConverter.parse<Any>(any(), any()) } returns note
      coEvery { createRemoteFileMetadataUseCase(Source.Dropbox.value, cloudFile, note) } returns remoteFileMetadata

      // Act
      downloadSingleUseCase(dataType, noteId)

      // Assert - Metadata should be saved
      coVerify(exactly = 1) { createRemoteFileMetadataUseCase(Source.Dropbox.value, cloudFile, note) }
      coVerify(exactly = 1) { remoteFileMetadataRepository.save(remoteFileMetadata) }
    }
  }

  @Test
  fun `invoke should update sync state to synced after successful download`() {
    runBlocking {
      // Arrange - Verify sync state update
      val dataType = DataType.Groups
      val groupId = "group-uuid-xyz789"
      val group = mockk<Any>()
      val cloudFile = CloudFile(
        id = "group-file-id",
        name = "$groupId.bi2",
        fileExtension = ".bi2",
        lastModified = 1699100000000L,
        size = 256,
        version = 1L,
        rev = "rev1"
      )
      val inputStream = ByteArrayInputStream("group data".toByteArray())
      val remoteFileMetadata = mockk<RemoteFileMetadata>()

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { findNewestCloudApiSourceUseCase(dataType, groupId) } returns
        FindNewestCloudApiSourceUseCase.SearchResult(mockCloudFileApi, cloudFile)
      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.downloadFile(cloudFile) } returns inputStream
      coEvery { syncDataConverter.parse<Any>(any(), any()) } returns group
      coEvery { createRemoteFileMetadataUseCase(any(), any(), any()) } returns remoteFileMetadata

      // Act
      downloadSingleUseCase(dataType, groupId)

      // Assert - Sync state should be updated to Synced
      coVerify(exactly = 1) { mockRepositoryCaller.updateSyncState(groupId, SyncState.Synced) }
    }
  }

  @Test
  fun `invoke when parse fails should propagate exception`() {
    runBlocking {
      // Arrange - Parser throws exception
      val dataType = DataType.Places
      val placeId = "place-id-location-456"
      val cloudFile = CloudFile(
        id = "place-file-id",
        name = "$placeId.pl2",
        fileExtension = ".pl2",
        lastModified = 1699150000000L,
        size = 128,
        version = 1L,
        rev = "rev1"
      )
      val inputStream = ByteArrayInputStream("corrupted data".toByteArray())

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { findNewestCloudApiSourceUseCase(dataType, placeId) } returns
        FindNewestCloudApiSourceUseCase.SearchResult(mockCloudFileApi, cloudFile)
      coEvery { mockCloudFileApi.downloadFile(cloudFile) } returns inputStream
      coEvery { syncDataConverter.parse<Any>(any(), any()) } throws RuntimeException("Parse error: Invalid JSON")

      // Act & Assert - Exception should propagate
      var exceptionThrown = false
      try {
        downloadSingleUseCase(dataType, placeId)
      } catch (e: RuntimeException) {
        exceptionThrown = true
        assertTrue(e.message?.contains("Parse error") == true)
      }
      assertTrue("Expected RuntimeException to be thrown", exceptionThrown)

      // Verify no further processing occurred
      coVerify(exactly = 0) { mockRepositoryCaller.insertOrUpdate(any()) }
      coVerify(exactly = 0) { remoteFileMetadataRepository.save(any()) }
    }
  }

  @Test
  fun `invoke with all data types should use correct class mapping`() {
    runBlocking {
      // Arrange - Test that each data type maps to correct class
      val testCases = listOf(
        DataType.Reminders to Reminder::class.java,
        DataType.Birthdays to Birthday::class.java
      )

      for ((dataType, expectedClass) in testCases) {
        val id = "test-id-${dataType.name}"
        val cloudFile = CloudFile(
          id = "cloud-id",
          name = "$id${dataType.fileExtension}",
          fileExtension = dataType.fileExtension,
          lastModified = 1699200000000L,
          size = 512,
          version = 1L,
          rev = "rev1"
        )
        val inputStream = ByteArrayInputStream("data".toByteArray())
        val parsedObject = mockk<Any>()
        val remoteFileMetadata = mockk<RemoteFileMetadata>()

        every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
        coEvery { findNewestCloudApiSourceUseCase(dataType, id) } returns
          FindNewestCloudApiSourceUseCase.SearchResult(mockCloudFileApi, cloudFile)
        every { mockCloudFileApi.source } returns Source.GoogleDrive
        coEvery { mockCloudFileApi.downloadFile(cloudFile) } returns inputStream
        coEvery { syncDataConverter.parse(inputStream, expectedClass) } returns parsedObject
        coEvery { createRemoteFileMetadataUseCase(any(), any(), any()) } returns remoteFileMetadata

        // Act
        val result = downloadSingleUseCase(dataType, id)

        // Assert - Correct class should be used for parsing
        assertTrue(result is SyncResult.Success)
        coVerify(exactly = 1) { syncDataConverter.parse(inputStream, expectedClass) }
      }
    }
  }

  @Test
  fun `invoke should return success result with correct downloaded information`() {
    runBlocking {
      // Arrange - Verify result structure
      val dataType = DataType.Reminders
      val reminderId = "reminder-result-test"
      val reminder = mockk<Reminder>()
      val cloudFile = CloudFile(
        id = "result-file-id",
        name = "$reminderId.ta2",
        fileExtension = ".ta2",
        lastModified = 1699250000000L,
        size = 1024,
        version = 5L,
        rev = "rev5"
      )
      val inputStream = ByteArrayInputStream("data".toByteArray())
      val remoteFileMetadata = mockk<RemoteFileMetadata>()

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { findNewestCloudApiSourceUseCase(dataType, reminderId) } returns
        FindNewestCloudApiSourceUseCase.SearchResult(mockCloudFileApi, cloudFile)
      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.downloadFile(cloudFile) } returns inputStream
      coEvery { syncDataConverter.parse<Reminder>(any(), any()) } returns reminder
      coEvery { createRemoteFileMetadataUseCase(any(), any(), any()) } returns remoteFileMetadata

      // Act
      val result = downloadSingleUseCase(dataType, reminderId)

      // Assert - Result should contain correct information
      assertNotNull(result)
      assertTrue(result is SyncResult.Success)
      val successResult = result as SyncResult.Success
      assertTrue(successResult.success)
      assertEquals(1, successResult.downloaded.size)

      val downloadedItem = successResult.downloaded[0]
      assertEquals(dataType, downloadedItem.dataType)
      assertEquals(reminderId, downloadedItem.id)
    }
  }
}
