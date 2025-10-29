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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

/**
 * Unit tests for [DownloadUseCase].
 *
 * Tests the batch downloading of multiple files from cloud storage,
 * including file retrieval, parsing, local storage, metadata tracking,
 * post-processing, and error handling for multiple files across multiple sources.
 */
class DownloadUseCaseTest {

  private lateinit var dataTypeRepositoryCallerFactory: DataTypeRepositoryCallerFactory
  private lateinit var syncDataConverter: SyncDataConverter
  private lateinit var remoteFileMetadataRepository: RemoteFileMetadataRepository
  private lateinit var createRemoteFileMetadataUseCase: CreateRemoteFileMetadataUseCase
  private lateinit var findAllFilesToDownloadUseCase: FindAllFilesToDownloadUseCase
  private lateinit var getLocalUuIdUseCase: GetLocalUuIdUseCase
  private lateinit var dataPostProcessor: DataPostProcessor
  private lateinit var downloadUseCase: DownloadUseCase

  private lateinit var mockRepositoryCaller: DataTypeRepositoryCaller<Any>
  private lateinit var mockCloudFileApi: CloudFileApi

  @Before
  fun setUp() {
    dataTypeRepositoryCallerFactory = mockk()
    syncDataConverter = mockk()
    remoteFileMetadataRepository = mockk(relaxed = true)
    createRemoteFileMetadataUseCase = mockk()
    findAllFilesToDownloadUseCase = mockk()
    getLocalUuIdUseCase = mockk()
    dataPostProcessor = mockk(relaxed = true)
    mockRepositoryCaller = mockk(relaxed = true)
    mockCloudFileApi = mockk()

    downloadUseCase = DownloadUseCase(
      dataTypeRepositoryCallerFactory = dataTypeRepositoryCallerFactory,
      syncDataConverter = syncDataConverter,
      remoteFileMetadataRepository = remoteFileMetadataRepository,
      createRemoteFileMetadataUseCase = createRemoteFileMetadataUseCase,
      findAllFilesToDownloadUseCase = findAllFilesToDownloadUseCase,
      getLocalUuIdUseCase = getLocalUuIdUseCase,
      dataPostProcessor = dataPostProcessor
    )
  }

  @Test
  fun `invoke with single file should download parse and save successfully`() {
    runBlocking {
      // Arrange - Single reminder file from Google Drive
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
      val searchResult = FindAllFilesToDownloadUseCase.SearchResult(
        sources = listOf(
          FindAllFilesToDownloadUseCase.CloudFilesWithSource(
            source = mockCloudFileApi,
            cloudFiles = listOf(cloudFile)
          )
        )
      )

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { findAllFilesToDownloadUseCase(dataType) } returns searchResult
      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.downloadFile(cloudFile) } returns inputStream
      coEvery { syncDataConverter.parse(inputStream, Reminder::class.java) } returns reminder
      every { getLocalUuIdUseCase(reminder) } returns reminderId
      coEvery { createRemoteFileMetadataUseCase(Source.GoogleDrive.value, cloudFile, reminder) } returns remoteFileMetadata

      // Act
      val result = downloadUseCase(dataType)

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
  fun `invoke when no files to download should return skipped`() {
    runBlocking {
      // Arrange - FindAllFilesToDownloadUseCase returns null
      val dataType = DataType.Reminders

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { findAllFilesToDownloadUseCase(dataType) } returns null

      // Act
      val result = downloadUseCase(dataType)

      // Assert - Should skip download
      assertTrue(result is SyncResult.Skipped)
      coVerify(exactly = 0) { mockCloudFileApi.downloadFile(any()) }
      coVerify(exactly = 0) { mockRepositoryCaller.insertOrUpdate(any()) }
    }
  }

  @Test
  fun `invoke with multiple files should download all successfully`() {
    runBlocking {
      // Arrange - Three birthday files from Google Drive
      val dataType = DataType.Birthdays
      val birthday1 = Birthday(name = "John", uuId = "b1", syncState = SyncState.Synced)
      val birthday2 = Birthday(name = "Jane", uuId = "b2", syncState = SyncState.Synced)
      val birthday3 = Birthday(name = "Bob", uuId = "b3", syncState = SyncState.Synced)

      val cloudFile1 = CloudFile(id = "cf1", name = "b1.gr2", fileExtension = ".gr2", lastModified = 1000L, size = 100, version = 1L, rev = "r1")
      val cloudFile2 = CloudFile(id = "cf2", name = "b2.gr2", fileExtension = ".gr2", lastModified = 2000L, size = 200, version = 1L, rev = "r2")
      val cloudFile3 = CloudFile(id = "cf3", name = "b3.gr2", fileExtension = ".gr2", lastModified = 3000L, size = 300, version = 1L, rev = "r3")

      val stream1 = ByteArrayInputStream("data1".toByteArray())
      val stream2 = ByteArrayInputStream("data2".toByteArray())
      val stream3 = ByteArrayInputStream("data3".toByteArray())

      val metadata1 = mockk<RemoteFileMetadata>()
      val metadata2 = mockk<RemoteFileMetadata>()
      val metadata3 = mockk<RemoteFileMetadata>()

      val searchResult = FindAllFilesToDownloadUseCase.SearchResult(
        sources = listOf(
          FindAllFilesToDownloadUseCase.CloudFilesWithSource(
            source = mockCloudFileApi,
            cloudFiles = listOf(cloudFile1, cloudFile2, cloudFile3)
          )
        )
      )

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { findAllFilesToDownloadUseCase(dataType) } returns searchResult
      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.downloadFile(cloudFile1) } returns stream1
      coEvery { mockCloudFileApi.downloadFile(cloudFile2) } returns stream2
      coEvery { mockCloudFileApi.downloadFile(cloudFile3) } returns stream3
      coEvery { syncDataConverter.parse(stream1, Birthday::class.java) } returns birthday1
      coEvery { syncDataConverter.parse(stream2, Birthday::class.java) } returns birthday2
      coEvery { syncDataConverter.parse(stream3, Birthday::class.java) } returns birthday3
      every { getLocalUuIdUseCase(birthday1) } returns "b1"
      every { getLocalUuIdUseCase(birthday2) } returns "b2"
      every { getLocalUuIdUseCase(birthday3) } returns "b3"
      coEvery { createRemoteFileMetadataUseCase(any(), any(), any()) } returnsMany listOf(metadata1, metadata2, metadata3)

      // Act
      val result = downloadUseCase(dataType)

      // Assert - All three files should be downloaded
      assertTrue(result is SyncResult.Success)
      val successResult = result as SyncResult.Success
      assertEquals(3, successResult.downloaded.size)

      coVerify(exactly = 3) { mockCloudFileApi.downloadFile(any()) }
      coVerify(exactly = 3) { mockRepositoryCaller.insertOrUpdate(any()) }
      coVerify(exactly = 3) { remoteFileMetadataRepository.save(any()) }
      coVerify(exactly = 3) { mockRepositoryCaller.updateSyncState(any(), SyncState.Synced) }
    }
  }

  @Test
  fun `invoke with multiple sources should download from all sources`() {
    runBlocking {
      // Arrange - Files from both Google Drive and Dropbox
      val dataType = DataType.Reminders
      val reminder1 = Reminder(summary = "Test1", uuId = "r1")
      val reminder2 = Reminder(summary = "Test2", uuId = "r2")

      val gdriveFile = CloudFile(id = "gd1", name = "r1.ta2", fileExtension = ".ta2", lastModified = 1000L, size = 100, version = 1L, rev = "gr1")
      val dropboxFile = CloudFile(id = "db1", name = "r2.ta2", fileExtension = ".ta2", lastModified = 2000L, size = 200, version = 1L, rev = "dr1")

      val mockGDriveApi = mockk<CloudFileApi>()
      val mockDropboxApi = mockk<CloudFileApi>()

      val stream1 = ByteArrayInputStream("gdrive data".toByteArray())
      val stream2 = ByteArrayInputStream("dropbox data".toByteArray())

      val metadata1 = mockk<RemoteFileMetadata>()
      val metadata2 = mockk<RemoteFileMetadata>()

      val searchResult = FindAllFilesToDownloadUseCase.SearchResult(
        sources = listOf(
          FindAllFilesToDownloadUseCase.CloudFilesWithSource(mockGDriveApi, listOf(gdriveFile)),
          FindAllFilesToDownloadUseCase.CloudFilesWithSource(mockDropboxApi, listOf(dropboxFile))
        )
      )

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { findAllFilesToDownloadUseCase(dataType) } returns searchResult
      every { mockGDriveApi.source } returns Source.GoogleDrive
      every { mockDropboxApi.source } returns Source.Dropbox
      coEvery { mockGDriveApi.downloadFile(gdriveFile) } returns stream1
      coEvery { mockDropboxApi.downloadFile(dropboxFile) } returns stream2
      coEvery { syncDataConverter.parse(stream1, Reminder::class.java) } returns reminder1
      coEvery { syncDataConverter.parse(stream2, Reminder::class.java) } returns reminder2
      every { getLocalUuIdUseCase(reminder1) } returns "r1"
      every { getLocalUuIdUseCase(reminder2) } returns "r2"
      coEvery { createRemoteFileMetadataUseCase(Source.GoogleDrive.value, gdriveFile, reminder1) } returns metadata1
      coEvery { createRemoteFileMetadataUseCase(Source.Dropbox.value, dropboxFile, reminder2) } returns metadata2

      // Act
      val result = downloadUseCase(dataType)

      // Assert - Should download from both sources
      assertTrue(result is SyncResult.Success)
      val successResult = result as SyncResult.Success
      assertEquals(2, successResult.downloaded.size)

      coVerify(exactly = 1) { mockGDriveApi.downloadFile(gdriveFile) }
      coVerify(exactly = 1) { mockDropboxApi.downloadFile(dropboxFile) }
    }
  }

  @Test
  fun `invoke when one file fails to download should continue with others`() {
    runBlocking {
      // Arrange - Three files, middle one fails
      val dataType = DataType.Reminders
      val reminder1 = Reminder(summary = "Test1", uuId = "r1")
      val reminder3 = Reminder(summary = "Test3", uuId = "r3")

      val cloudFile1 = CloudFile(id = "cf1", name = "r1.ta2", fileExtension = ".ta2", lastModified = 1000L, size = 100, version = 1L, rev = "r1")
      val cloudFile2 = CloudFile(id = "cf2", name = "r2.ta2", fileExtension = ".ta2", lastModified = 2000L, size = 200, version = 1L, rev = "r2")
      val cloudFile3 = CloudFile(id = "cf3", name = "r3.ta2", fileExtension = ".ta2", lastModified = 3000L, size = 300, version = 1L, rev = "r3")

      val stream1 = ByteArrayInputStream("data1".toByteArray())
      val stream3 = ByteArrayInputStream("data3".toByteArray())

      val metadata1 = mockk<RemoteFileMetadata>()
      val metadata3 = mockk<RemoteFileMetadata>()

      val searchResult = FindAllFilesToDownloadUseCase.SearchResult(
        sources = listOf(
          FindAllFilesToDownloadUseCase.CloudFilesWithSource(
            source = mockCloudFileApi,
            cloudFiles = listOf(cloudFile1, cloudFile2, cloudFile3)
          )
        )
      )

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { findAllFilesToDownloadUseCase(dataType) } returns searchResult
      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.downloadFile(cloudFile1) } returns stream1
      coEvery { mockCloudFileApi.downloadFile(cloudFile2) } returns null  // Download fails
      coEvery { mockCloudFileApi.downloadFile(cloudFile3) } returns stream3
      coEvery { syncDataConverter.parse(stream1, Reminder::class.java) } returns reminder1
      coEvery { syncDataConverter.parse(stream3, Reminder::class.java) } returns reminder3
      every { getLocalUuIdUseCase(reminder1) } returns "r1"
      every { getLocalUuIdUseCase(reminder3) } returns "r3"
      coEvery { createRemoteFileMetadataUseCase(any(), cloudFile1, reminder1) } returns metadata1
      coEvery { createRemoteFileMetadataUseCase(any(), cloudFile3, reminder3) } returns metadata3

      // Act
      val result = downloadUseCase(dataType)

      // Assert - Should download 2 out of 3 files
      assertTrue(result is SyncResult.Success)
      val successResult = result as SyncResult.Success
      assertEquals(2, successResult.downloaded.size)

      // Verify file 2 was skipped
      coVerify(exactly = 0) { syncDataConverter.parse(any(), match { it == cloudFile2 }) }
      coVerify(exactly = 2) { mockRepositoryCaller.insertOrUpdate(any()) }
    }
  }

  @Test
  fun `invoke should return skipped when all downloads fail`() {
    runBlocking {
      // Arrange - All downloads return null
      val dataType = DataType.Notes
      val cloudFile1 = CloudFile(id = "cf1", name = "n1.no2", fileExtension = ".no2", lastModified = 1000L, size = 100, version = 1L, rev = "r1")
      val cloudFile2 = CloudFile(id = "cf2", name = "n2.no2", fileExtension = ".no2", lastModified = 2000L, size = 200, version = 1L, rev = "r2")

      val searchResult = FindAllFilesToDownloadUseCase.SearchResult(
        sources = listOf(
          FindAllFilesToDownloadUseCase.CloudFilesWithSource(
            source = mockCloudFileApi,
            cloudFiles = listOf(cloudFile1, cloudFile2)
          )
        )
      )

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { findAllFilesToDownloadUseCase(dataType) } returns searchResult
      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.downloadFile(any()) } returns null

      // Act
      val result = downloadUseCase(dataType)

      // Assert - Should return skipped when no files downloaded
      assertTrue(result is SyncResult.Skipped)
      coVerify(exactly = 0) { mockRepositoryCaller.insertOrUpdate(any()) }
    }
  }

  @Test
  fun `invoke should call post processor for each downloaded file`() {
    runBlocking {
      // Arrange - Two files
      val dataType = DataType.Reminders
      val reminder1 = Reminder(summary = "Test1", uuId = "r1")
      val reminder2 = Reminder(summary = "Test2", uuId = "r2")

      val cloudFile1 = CloudFile(id = "cf1", name = "r1.ta2", fileExtension = ".ta2", lastModified = 1000L, size = 100, version = 1L, rev = "r1")
      val cloudFile2 = CloudFile(id = "cf2", name = "r2.ta2", fileExtension = ".ta2", lastModified = 2000L, size = 200, version = 1L, rev = "r2")

      val stream1 = ByteArrayInputStream("data1".toByteArray())
      val stream2 = ByteArrayInputStream("data2".toByteArray())

      val searchResult = FindAllFilesToDownloadUseCase.SearchResult(
        sources = listOf(
          FindAllFilesToDownloadUseCase.CloudFilesWithSource(
            source = mockCloudFileApi,
            cloudFiles = listOf(cloudFile1, cloudFile2)
          )
        )
      )

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { findAllFilesToDownloadUseCase(dataType) } returns searchResult
      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.downloadFile(cloudFile1) } returns stream1
      coEvery { mockCloudFileApi.downloadFile(cloudFile2) } returns stream2
      coEvery { syncDataConverter.parse(stream1, Reminder::class.java) } returns reminder1
      coEvery { syncDataConverter.parse(stream2, Reminder::class.java) } returns reminder2
      every { getLocalUuIdUseCase(reminder1) } returns "r1"
      every { getLocalUuIdUseCase(reminder2) } returns "r2"
      coEvery { createRemoteFileMetadataUseCase(any(), any(), any()) } returns mockk()

      // Act
      downloadUseCase(dataType)

      // Assert - Post processor should be called for both files
      coVerify(exactly = 1) { dataPostProcessor.process(dataType, reminder1) }
      coVerify(exactly = 1) { dataPostProcessor.process(dataType, reminder2) }
    }
  }

  @Test
  fun `invoke should update sync state to synced for each file`() {
    runBlocking {
      // Arrange - Two reminders
      val dataType = DataType.Reminders
      val reminder1 = Reminder(summary = "Test1", uuId = "r1")
      val reminder2 = Reminder(summary = "Test2", uuId = "r2")

      val cloudFile1 = CloudFile(id = "cf1", name = "r1.ta2", fileExtension = ".ta2", lastModified = 1000L, size = 100, version = 1L, rev = "r1")
      val cloudFile2 = CloudFile(id = "cf2", name = "r2.ta2", fileExtension = ".ta2", lastModified = 2000L, size = 200, version = 1L, rev = "r2")

      val stream1 = ByteArrayInputStream("data1".toByteArray())
      val stream2 = ByteArrayInputStream("data2".toByteArray())

      val searchResult = FindAllFilesToDownloadUseCase.SearchResult(
        sources = listOf(
          FindAllFilesToDownloadUseCase.CloudFilesWithSource(
            source = mockCloudFileApi,
            cloudFiles = listOf(cloudFile1, cloudFile2)
          )
        )
      )

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { findAllFilesToDownloadUseCase(dataType) } returns searchResult
      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.downloadFile(cloudFile1) } returns stream1
      coEvery { mockCloudFileApi.downloadFile(cloudFile2) } returns stream2
      coEvery { syncDataConverter.parse(stream1, Reminder::class.java) } returns reminder1
      coEvery { syncDataConverter.parse(stream2, Reminder::class.java) } returns reminder2
      every { getLocalUuIdUseCase(reminder1) } returns "r1"
      every { getLocalUuIdUseCase(reminder2) } returns "r2"
      coEvery { createRemoteFileMetadataUseCase(any(), any(), any()) } returns mockk()

      // Act
      downloadUseCase(dataType)

      // Assert - Sync state should be updated for both files
      coVerify(exactly = 1) { mockRepositoryCaller.updateSyncState("r1", SyncState.Synced) }
      coVerify(exactly = 1) { mockRepositoryCaller.updateSyncState("r2", SyncState.Synced) }
    }
  }

  @Test
  fun `invoke when parse fails should propagate exception and stop processing`() {
    runBlocking {
      // Arrange - First file parses successfully, second fails
      val dataType = DataType.Places
      val cloudFile1 = CloudFile(id = "cf1", name = "p1.pl2", fileExtension = ".pl2", lastModified = 1000L, size = 100, version = 1L, rev = "r1")
      val cloudFile2 = CloudFile(id = "cf2", name = "p2.pl2", fileExtension = ".pl2", lastModified = 2000L, size = 200, version = 1L, rev = "r2")

      val stream1 = ByteArrayInputStream("data1".toByteArray())
      val stream2 = ByteArrayInputStream("corrupted".toByteArray())

      val place1 = mockk<Any>()

      val searchResult = FindAllFilesToDownloadUseCase.SearchResult(
        sources = listOf(
          FindAllFilesToDownloadUseCase.CloudFilesWithSource(
            source = mockCloudFileApi,
            cloudFiles = listOf(cloudFile1, cloudFile2)
          )
        )
      )

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { findAllFilesToDownloadUseCase(dataType) } returns searchResult
      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.downloadFile(cloudFile1) } returns stream1
      coEvery { mockCloudFileApi.downloadFile(cloudFile2) } returns stream2
      coEvery { syncDataConverter.parse<Any>(stream1, any()) } returns place1
      coEvery { syncDataConverter.parse<Any>(stream2, any()) } throws RuntimeException("Parse error")
      every { getLocalUuIdUseCase(place1) } returns "p1"
      coEvery { createRemoteFileMetadataUseCase(any(), any(), any()) } returns mockk()

      // Act & Assert - Exception should propagate
      var exceptionThrown = false
      try {
        downloadUseCase(dataType)
      } catch (e: RuntimeException) {
        exceptionThrown = true
        assertTrue(e.message?.contains("Parse error") == true)
      }
      assertTrue("Expected RuntimeException to be thrown", exceptionThrown)

      // First file should have been processed
      coVerify(exactly = 1) { mockRepositoryCaller.insertOrUpdate(place1) }
    }
  }

  @Test
  fun `invoke with large number of files should process all sequentially`() {
    runBlocking {
      // Arrange - 10 birthday files
      val dataType = DataType.Birthdays
      val cloudFiles = (1..10).map { index ->
        CloudFile(
          id = "cf$index",
          name = "b$index.gr2",
          fileExtension = ".gr2",
          lastModified = 1000L * index,
          size = 100 * index,
          version = 1L,
          rev = "r$index"
        )
      }
      val birthdays = (1..10).map { index ->
        Birthday(name = "Person$index", uuId = "b$index", syncState = SyncState.Synced)
      }
      val streams = (1..10).map { index ->
        ByteArrayInputStream("data$index".toByteArray())
      }

      val searchResult = FindAllFilesToDownloadUseCase.SearchResult(
        sources = listOf(
          FindAllFilesToDownloadUseCase.CloudFilesWithSource(
            source = mockCloudFileApi,
            cloudFiles = cloudFiles
          )
        )
      )

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { findAllFilesToDownloadUseCase(dataType) } returns searchResult
      every { mockCloudFileApi.source } returns Source.GoogleDrive

      cloudFiles.forEachIndexed { index, cloudFile ->
        coEvery { mockCloudFileApi.downloadFile(cloudFile) } returns streams[index]
        coEvery { syncDataConverter.parse(streams[index], Birthday::class.java) } returns birthdays[index]
        every { getLocalUuIdUseCase(birthdays[index]) } returns "b${index + 1}"
        coEvery { createRemoteFileMetadataUseCase(any(), cloudFile, birthdays[index]) } returns mockk()
      }

      // Act
      val result = downloadUseCase(dataType)

      // Assert - All 10 files should be downloaded
      assertTrue(result is SyncResult.Success)
      val successResult = result as SyncResult.Success
      assertEquals(10, successResult.downloaded.size)

      coVerify(exactly = 10) { mockCloudFileApi.downloadFile(any()) }
      coVerify(exactly = 10) { mockRepositoryCaller.insertOrUpdate(any()) }
      coVerify(exactly = 10) { remoteFileMetadataRepository.save(any()) }
    }
  }
}
