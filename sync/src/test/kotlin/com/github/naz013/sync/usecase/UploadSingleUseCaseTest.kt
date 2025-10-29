package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.cloudapi.Source
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.sync.RemoteFileMetadata
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.CloudApiProvider
import com.github.naz013.sync.DataType
import com.github.naz013.sync.SyncDataConverter
import com.github.naz013.sync.local.DataTypeRepositoryCaller
import com.github.naz013.sync.local.DataTypeRepositoryCallerFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * Unit tests for [UploadSingleUseCase].
 *
 * Tests the complete upload workflow for a single item including:
 * - Data retrieval from local repository
 * - Sync state management (Uploading, Synced, FailedToUpload)
 * - Cloud file creation and upload to all providers
 * - Remote metadata tracking
 * - Error handling and state rollback
 */
class UploadSingleUseCaseTest {

  private lateinit var dataTypeRepositoryCallerFactory: DataTypeRepositoryCallerFactory
  private lateinit var syncDataConverter: SyncDataConverter
  private lateinit var createCloudFileUseCase: CreateCloudFileUseCase
  private lateinit var remoteFileMetadataRepository: RemoteFileMetadataRepository
  private lateinit var createRemoteFileMetadataUseCase: CreateRemoteFileMetadataUseCase
  private lateinit var cloudApiProvider: CloudApiProvider
  private lateinit var uploadSingleUseCase: UploadSingleUseCase

  private lateinit var mockRepositoryCaller: DataTypeRepositoryCaller<Any>
  private lateinit var mockGDriveApi: CloudFileApi
  private lateinit var mockDropboxApi: CloudFileApi

  @Before
  fun setUp() {
    dataTypeRepositoryCallerFactory = mockk()
    syncDataConverter = mockk()
    createCloudFileUseCase = mockk()
    remoteFileMetadataRepository = mockk(relaxed = true)
    createRemoteFileMetadataUseCase = mockk()
    cloudApiProvider = mockk()
    mockRepositoryCaller = mockk(relaxed = true)
    mockGDriveApi = mockk(relaxUnitFun = true)
    mockDropboxApi = mockk(relaxUnitFun = true)

    uploadSingleUseCase = UploadSingleUseCase(
      dataTypeRepositoryCallerFactory = dataTypeRepositoryCallerFactory,
      syncDataConverter = syncDataConverter,
      createCloudFileUseCase = createCloudFileUseCase,
      remoteFileMetadataRepository = remoteFileMetadataRepository,
      createRemoteFileMetadataUseCase = createRemoteFileMetadataUseCase,
      cloudApiProvider = cloudApiProvider
    )
  }

  @Test
  fun `invoke with valid reminder should upload to single cloud provider and update state to synced`() {
    runBlocking {
      // Arrange - Single reminder upload to Google Drive
      val dataType = DataType.Reminders
      val reminderId = "reminder-uuid-12345"
      val reminder = mockk<Reminder>()
      val cloudFile = CloudFile(
        id = "gdrive-file-id",
        name = "$reminderId.ta2",
        fileExtension = ".ta2",
        lastModified = 1698765432000L,
        size = 2048,
        version = 1L,
        rev = "rev1"
      )
      val uploadedFile = cloudFile.copy(id = "uploaded-id", version = 2L)
      val inputStream = ByteArrayInputStream("reminder data".toByteArray())
      val remoteMetadata = RemoteFileMetadata(
        id = "uploaded-id",
        name = "$reminderId.ta2",
        lastModified = 1698765432000L,
        size = 2048,
        source = "GDRIVE",
        localUuId = reminderId,
        fileExtension = ".ta2",
        version = 2L,
        rev = "rev1"
      )

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { mockRepositoryCaller.getById(reminderId) } returns reminder
      coEvery { createCloudFileUseCase(dataType, reminder) } returns cloudFile
      every { cloudApiProvider.getAllowedCloudApis() } returns listOf(mockGDriveApi)
      every { mockGDriveApi.source } returns Source.GoogleDrive
      coEvery { syncDataConverter.create(reminder) } returns inputStream
      coEvery { mockGDriveApi.uploadFile(inputStream, cloudFile) } returns uploadedFile
      coEvery { createRemoteFileMetadataUseCase(Source.GoogleDrive.value, uploadedFile, reminder) } returns remoteMetadata

      // Act
      uploadSingleUseCase(dataType, reminderId)

      // Assert - Complete workflow executed
      coVerify(exactly = 1) { mockRepositoryCaller.getById(reminderId) }
      coVerify(exactly = 1) { mockRepositoryCaller.updateSyncState(reminderId, SyncState.Uploading) }
      coVerify(exactly = 1) { createCloudFileUseCase(dataType, reminder) }
      coVerify(exactly = 1) { syncDataConverter.create(reminder) }
      coVerify(exactly = 1) { mockGDriveApi.uploadFile(inputStream, cloudFile) }
      coVerify(exactly = 1) { createRemoteFileMetadataUseCase(Source.GoogleDrive.value, uploadedFile, reminder) }
      coVerify(exactly = 1) { remoteFileMetadataRepository.save(remoteMetadata) }
      coVerify(exactly = 1) { mockRepositoryCaller.updateSyncState(reminderId, SyncState.Synced) }
    }
  }

  @Test
  fun `invoke when data not found should update state to failed and throw exception`() {
    runBlocking {
      // Arrange - Item doesn't exist in database
      val dataType = DataType.Birthdays
      val birthdayId = "non-existent-birthday"

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { mockRepositoryCaller.getById(birthdayId) } returns null

      // Act & Assert
      var exceptionThrown = false
      var exceptionMessage = ""
      try {
        uploadSingleUseCase(dataType, birthdayId)
      } catch (e: IllegalArgumentException) {
        exceptionThrown = true
        exceptionMessage = e.message ?: ""
      }

      // Assert - Exception thrown with proper message
      assertTrue(exceptionThrown)
      assertEquals("No data found for id: $birthdayId", exceptionMessage)

      // Verify state updated to FailedToUpload
      coVerify(exactly = 1) { mockRepositoryCaller.getById(birthdayId) }
      coVerify(exactly = 1) { mockRepositoryCaller.updateSyncState(birthdayId, SyncState.FailedToUpload) }
      coVerify(exactly = 0) { mockRepositoryCaller.updateSyncState(birthdayId, SyncState.Uploading) }
      coVerify(exactly = 0) { createCloudFileUseCase(any(), any()) }
    }
  }

  @Test
  fun `invoke with multiple cloud providers should upload to all providers`() {
    runBlocking {
      // Arrange - Upload to both Google Drive and Dropbox
      val dataType = DataType.Notes
      val noteId = "note-key-abc123"
      val note = mockk<Any>()
      val cloudFile = CloudFile(
        id = "note-file",
        name = "$noteId.no2",
        fileExtension = ".no2",
        lastModified = 1698800000000L,
        size = 512,
        version = 1L,
        rev = "r1"
      )
      val gdriveUploadedFile = cloudFile.copy(id = "gdrive-uploaded-id", version = 2L)
      val dropboxUploadedFile = cloudFile.copy(id = "dropbox-uploaded-id", rev = "dropbox-rev-2")
      val gdriveStream = ByteArrayInputStream("note data 1".toByteArray())
      val dropboxStream = ByteArrayInputStream("note data 2".toByteArray())
      val gdriveMetadata = mockk<RemoteFileMetadata>()
      val dropboxMetadata = mockk<RemoteFileMetadata>()

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { mockRepositoryCaller.getById(noteId) } returns note
      coEvery { createCloudFileUseCase(dataType, note) } returns cloudFile
      every { cloudApiProvider.getAllowedCloudApis() } returns listOf(mockGDriveApi, mockDropboxApi)
      every { mockGDriveApi.source } returns Source.GoogleDrive
      every { mockDropboxApi.source } returns Source.Dropbox
      coEvery { syncDataConverter.create(note) } returnsMany listOf(gdriveStream, dropboxStream)
      coEvery { mockGDriveApi.uploadFile(gdriveStream, cloudFile) } returns gdriveUploadedFile
      coEvery { mockDropboxApi.uploadFile(dropboxStream, cloudFile) } returns dropboxUploadedFile
      coEvery { createRemoteFileMetadataUseCase(Source.GoogleDrive.value, gdriveUploadedFile, note) } returns gdriveMetadata
      coEvery { createRemoteFileMetadataUseCase(Source.Dropbox.value, dropboxUploadedFile, note) } returns dropboxMetadata

      // Act
      uploadSingleUseCase(dataType, noteId)

      // Assert - Uploaded to both providers
      coVerify(exactly = 1) { mockGDriveApi.uploadFile(gdriveStream, cloudFile) }
      coVerify(exactly = 1) { mockDropboxApi.uploadFile(dropboxStream, cloudFile) }
      coVerify(exactly = 2) { remoteFileMetadataRepository.save(any()) }
      coVerify(exactly = 1) { remoteFileMetadataRepository.save(gdriveMetadata) }
      coVerify(exactly = 1) { remoteFileMetadataRepository.save(dropboxMetadata) }
      coVerify(exactly = 1) { mockRepositoryCaller.updateSyncState(noteId, SyncState.Synced) }
    }
  }

  @Test
  fun `invoke when upload fails should update state to failed and rethrow exception`() {
    runBlocking {
      // Arrange - Upload fails due to network error
      val dataType = DataType.Groups
      val groupId = "group-uuid-xyz789"
      val group = mockk<Any>()
      val cloudFile = mockk<CloudFile>()
      val inputStream = mockk<InputStream>()

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { mockRepositoryCaller.getById(groupId) } returns group
      coEvery { createCloudFileUseCase(dataType, group) } returns cloudFile
      every { cloudApiProvider.getAllowedCloudApis() } returns listOf(mockGDriveApi)
      coEvery { syncDataConverter.create(group) } returns inputStream
      coEvery { mockGDriveApi.uploadFile(inputStream, cloudFile) } throws RuntimeException("Network error")

      // Act & Assert
      var exceptionThrown = false
      var exceptionMessage = ""
      try {
        uploadSingleUseCase(dataType, groupId)
      } catch (e: RuntimeException) {
        exceptionThrown = true
        exceptionMessage = e.message ?: ""
      }

      // Assert - Exception rethrown and state updated
      assertTrue(exceptionThrown)
      assertEquals("Network error", exceptionMessage)

      coVerify(exactly = 1) { mockRepositoryCaller.updateSyncState(groupId, SyncState.Uploading) }
      coVerify(exactly = 1) { mockRepositoryCaller.updateSyncState(groupId, SyncState.FailedToUpload) }
      coVerify(exactly = 0) { mockRepositoryCaller.updateSyncState(groupId, SyncState.Synced) }
      coVerify(exactly = 0) { remoteFileMetadataRepository.save(any()) }
    }
  }

  @Test
  fun `invoke should set state to uploading before upload and synced after success`() {
    runBlocking {
      // Arrange - Verify state transitions
      val dataType = DataType.Places
      val placeId = "place-id-location-456"
      val place = mockk<Any>()
      val cloudFile = mockk<CloudFile>()
      val uploadedFile = mockk<CloudFile>()
      val inputStream = mockk<InputStream>()
      val metadata = mockk<RemoteFileMetadata>()

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { mockRepositoryCaller.getById(placeId) } returns place
      coEvery { createCloudFileUseCase(dataType, place) } returns cloudFile
      every { cloudApiProvider.getAllowedCloudApis() } returns listOf(mockGDriveApi)
      every { mockGDriveApi.source } returns Source.GoogleDrive
      coEvery { syncDataConverter.create(place) } returns inputStream
      coEvery { mockGDriveApi.uploadFile(inputStream, cloudFile) } returns uploadedFile
      coEvery { createRemoteFileMetadataUseCase(any(), any(), any()) } returns metadata

      // Act
      uploadSingleUseCase(dataType, placeId)

      // Assert - State transitions in correct order
      verifyOrder {
        runBlocking { mockRepositoryCaller.updateSyncState(placeId, SyncState.Uploading) }
        runBlocking { mockGDriveApi.uploadFile(inputStream, cloudFile) }
        runBlocking { mockRepositoryCaller.updateSyncState(placeId, SyncState.Synced) }
      }
    }
  }

  @Test
  fun `invoke should create metadata for each cloud provider`() {
    runBlocking {
      // Arrange - Two providers, verify metadata creation for each
      val dataType = DataType.Reminders
      val reminderId = "reminder-metadata-test"
      val reminder = mockk<Any>()
      val cloudFile = mockk<CloudFile>()
      val gdriveUploadedFile = mockk<CloudFile>()
      val dropboxUploadedFile = mockk<CloudFile>()
      val gdriveStream = mockk<InputStream>()
      val dropboxStream = mockk<InputStream>()

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { mockRepositoryCaller.getById(reminderId) } returns reminder
      coEvery { createCloudFileUseCase(dataType, reminder) } returns cloudFile
      every { cloudApiProvider.getAllowedCloudApis() } returns listOf(mockGDriveApi, mockDropboxApi)
      every { mockGDriveApi.source } returns Source.GoogleDrive
      every { mockDropboxApi.source } returns Source.Dropbox
      coEvery { syncDataConverter.create(reminder) } returnsMany listOf(gdriveStream, dropboxStream)
      coEvery { mockGDriveApi.uploadFile(gdriveStream, cloudFile) } returns gdriveUploadedFile
      coEvery { mockDropboxApi.uploadFile(dropboxStream, cloudFile) } returns dropboxUploadedFile
      coEvery { createRemoteFileMetadataUseCase(any(), any(), any()) } returns mockk()

      // Act
      uploadSingleUseCase(dataType, reminderId)

      // Assert - Metadata created for each provider with correct source
      coVerify(exactly = 1) { createRemoteFileMetadataUseCase(Source.GoogleDrive.value, gdriveUploadedFile, reminder) }
      coVerify(exactly = 1) { createRemoteFileMetadataUseCase(Source.Dropbox.value, dropboxUploadedFile, reminder) }
    }
  }

  @Test
  fun `invoke when cloud file creation fails should update state to failed and propagate exception`() {
    runBlocking {
      // Arrange - Cloud file creation throws exception
      val dataType = DataType.Birthdays
      val birthdayId = "birthday-uuid-error"
      val birthday = mockk<Any>()

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { mockRepositoryCaller.getById(birthdayId) } returns birthday
      coEvery { createCloudFileUseCase(dataType, birthday) } throws RuntimeException("Cloud file creation failed")

      // Act & Assert
      var exceptionThrown = false
      try {
        uploadSingleUseCase(dataType, birthdayId)
      } catch (e: RuntimeException) {
        exceptionThrown = true
      }

      // Assert - Exception propagated and state updated
      assertTrue(exceptionThrown)
      coVerify(exactly = 1) { mockRepositoryCaller.updateSyncState(birthdayId, SyncState.Uploading) }
      coVerify(exactly = 1) { mockRepositoryCaller.updateSyncState(birthdayId, SyncState.FailedToUpload) }
      coVerify(exactly = 0) { mockRepositoryCaller.updateSyncState(birthdayId, SyncState.Synced) }
    }
  }

  @Test
  fun `invoke when metadata creation fails should update state to failed and propagate exception`() {
    runBlocking {
      // Arrange - Metadata creation fails
      val dataType = DataType.Notes
      val noteId = "note-metadata-error"
      val note = mockk<Any>()
      val cloudFile = mockk<CloudFile>()
      val uploadedFile = mockk<CloudFile>()
      val inputStream = mockk<InputStream>()

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { mockRepositoryCaller.getById(noteId) } returns note
      coEvery { createCloudFileUseCase(dataType, note) } returns cloudFile
      every { cloudApiProvider.getAllowedCloudApis() } returns listOf(mockGDriveApi)
      every { mockGDriveApi.source } returns Source.GoogleDrive
      coEvery { syncDataConverter.create(note) } returns inputStream
      coEvery { mockGDriveApi.uploadFile(inputStream, cloudFile) } returns uploadedFile
      coEvery { createRemoteFileMetadataUseCase(any(), any(), any()) } throws RuntimeException("Metadata creation failed")

      // Act & Assert
      var exceptionThrown = false
      try {
        uploadSingleUseCase(dataType, noteId)
      } catch (e: RuntimeException) {
        exceptionThrown = true
      }

      // Assert
      assertTrue(exceptionThrown)
      coVerify(exactly = 1) { mockRepositoryCaller.updateSyncState(noteId, SyncState.Uploading) }
      coVerify(exactly = 1) { mockRepositoryCaller.updateSyncState(noteId, SyncState.FailedToUpload) }
      coVerify(exactly = 0) { remoteFileMetadataRepository.save(any()) }
    }
  }

  @Test
  fun `invoke with no cloud providers should skip upload but still update state to synced`() {
    runBlocking {
      // Arrange - No cloud providers configured
      val dataType = DataType.Places
      val placeId = "place-no-providers"
      val place = mockk<Any>()
      val cloudFile = mockk<CloudFile>()

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { mockRepositoryCaller.getById(placeId) } returns place
      coEvery { createCloudFileUseCase(dataType, place) } returns cloudFile
      every { cloudApiProvider.getAllowedCloudApis() } returns emptyList()

      // Act
      uploadSingleUseCase(dataType, placeId)

      // Assert - No uploads but state updated to Synced
      coVerify(exactly = 1) { mockRepositoryCaller.updateSyncState(placeId, SyncState.Uploading) }
      coVerify(exactly = 0) { syncDataConverter.create(any()) }
      coVerify(exactly = 0) { remoteFileMetadataRepository.save(any()) }
      coVerify(exactly = 1) { mockRepositoryCaller.updateSyncState(placeId, SyncState.Synced) }
    }
  }

  @Test
  fun `invoke should create new stream for each cloud provider`() {
    runBlocking {
      // Arrange - Verify separate streams created for each provider
      val dataType = DataType.Reminders
      val reminderId = "reminder-stream-test"
      val reminder = mockk<Any>()
      val cloudFile = mockk<CloudFile>()
      val stream1 = ByteArrayInputStream("data1".toByteArray())
      val stream2 = ByteArrayInputStream("data2".toByteArray())

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { mockRepositoryCaller.getById(reminderId) } returns reminder
      coEvery { createCloudFileUseCase(dataType, reminder) } returns cloudFile
      every { cloudApiProvider.getAllowedCloudApis() } returns listOf(mockGDriveApi, mockDropboxApi)
      every { mockGDriveApi.source } returns Source.GoogleDrive
      every { mockDropboxApi.source } returns Source.Dropbox
      coEvery { syncDataConverter.create(reminder) } returnsMany listOf(stream1, stream2)
      coEvery { mockGDriveApi.uploadFile(any(), any()) } returns mockk()
      coEvery { mockDropboxApi.uploadFile(any(), any()) } returns mockk()
      coEvery { createRemoteFileMetadataUseCase(any(), any(), any()) } returns mockk()

      // Act
      uploadSingleUseCase(dataType, reminderId)

      // Assert - syncDataConverter.create called twice (once per provider)
      coVerify(exactly = 2) { syncDataConverter.create(reminder) }
      coVerify(exactly = 1) { mockGDriveApi.uploadFile(stream1, cloudFile) }
      coVerify(exactly = 1) { mockDropboxApi.uploadFile(stream2, cloudFile) }
    }
  }
}

