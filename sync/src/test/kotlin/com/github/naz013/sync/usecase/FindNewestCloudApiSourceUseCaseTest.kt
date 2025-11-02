package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.cloudapi.CloudFileSearchParams
import com.github.naz013.cloudapi.Source
import com.github.naz013.domain.sync.RemoteFileMetadata
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.CloudApiProvider
import com.github.naz013.sync.DataType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [FindNewestCloudApiSourceUseCase].
 *
 * Tests the discovery of the newest version of a file across multiple cloud providers,
 * including timestamp comparison, metadata filtering, version checking for Google Drive,
 * and revision checking for Dropbox.
 */
class FindNewestCloudApiSourceUseCaseTest {

  private lateinit var getAllowedCloudApisUseCase: GetAllowedCloudApisUseCase
  private lateinit var remoteFileMetadataRepository: RemoteFileMetadataRepository
  private lateinit var findNewestCloudApiSourceUseCase: FindNewestCloudApiSourceUseCase

  private lateinit var mockGDriveApi: CloudFileApi
  private lateinit var mockDropboxApi: CloudFileApi

  @Before
  fun setUp() {
    getAllowedCloudApisUseCase = mockk()
    remoteFileMetadataRepository = mockk()
    mockGDriveApi = mockk(relaxUnitFun = true)
    mockDropboxApi = mockk(relaxUnitFun = true)

    findNewestCloudApiSourceUseCase = FindNewestCloudApiSourceUseCase(
      getAllowedCloudApisUseCase = getAllowedCloudApisUseCase,
      remoteFileMetadataRepository = remoteFileMetadataRepository
    )
  }

  @Test
  fun `invoke with single file in google drive should return that file`() {
    runBlocking {
      // Arrange - Single reminder in Google Drive
      val dataType = DataType.Reminders
      val reminderId = "reminder-uuid-12345"
      val cloudFile = CloudFile(
        id = "gdrive-file-id",
        name = "$reminderId.ta2",
        fileExtension = ".ta2",
        lastModified = 1698765432000L,
        size = 2048,
        version = 3L,
        rev = "rev3"
      )
      val searchParams = CloudFileSearchParams(
        name = reminderId,
        fileExtension = ".ta2"
      )

      every {getAllowedCloudApisUseCase.invoke() } returns listOf(mockGDriveApi)
      every { mockGDriveApi.source } returns Source.GoogleDrive
      coEvery { mockGDriveApi.findFile(searchParams) } returns cloudFile
      coEvery { remoteFileMetadataRepository.getByLocalUuIdAndSource(reminderId, "GoogleDrive") } returns null

      // Act
      val result = findNewestCloudApiSourceUseCase(dataType, reminderId)

      // Assert - Should return the file
      assertNotNull(result)
      assertEquals(mockGDriveApi, result!!.cloudFileApi)
      assertEquals(cloudFile, result.cloudFile)

      coVerify(exactly = 1) { mockGDriveApi.findFile(searchParams) }
      coVerify(exactly = 1) { remoteFileMetadataRepository.getByLocalUuIdAndSource(reminderId, "GoogleDrive") }
    }
  }

  @Test
  fun `invoke when no file found should return null`() {
    runBlocking {
      // Arrange - No file in any cloud provider
      val dataType = DataType.Birthdays
      val birthdayId = "birthday-uuid-67890"
      val searchParams = CloudFileSearchParams(
        name = birthdayId,
        fileExtension = ".bi2"
      )

      every {getAllowedCloudApisUseCase.invoke() } returns listOf(mockGDriveApi)
      every { mockGDriveApi.source } returns Source.GoogleDrive
      coEvery { mockGDriveApi.findFile(searchParams) } returns null
      coEvery { remoteFileMetadataRepository.getByLocalUuIdAndSource(any(), any()) } returns null

      // Act
      val result = findNewestCloudApiSourceUseCase(dataType, birthdayId)

      // Assert - Should return null
      assertNull(result)
    }
  }

  @Test
  fun `invoke with files in multiple sources should return newest by timestamp`() {
    runBlocking {
      // Arrange - Same file in both Google Drive and Dropbox, Dropbox is newer
      val dataType = DataType.Notes
      val noteId = "note-key-abc123"
      val gdriveFile = CloudFile(
        id = "gdrive-note-1",
        name = "$noteId.no3",
        fileExtension = ".no3",
        lastModified = 1698800000000L,  // Older
        size = 512,
        version = 2L,
        rev = "gdrive-rev-2"
      )
      val dropboxFile = CloudFile(
        id = "dropbox-note-1",
        name = "$noteId.no3",
        fileExtension = ".no3",
        lastModified = 1698900000000L,  // Newer
        size = 768,
        version = 3L,
        rev = "dropbox-rev-3"
      )
      val searchParams = CloudFileSearchParams(
        name = noteId,
        fileExtension = ".no3"
      )

      every {getAllowedCloudApisUseCase.invoke() } returns listOf(mockGDriveApi, mockDropboxApi)
      every { mockGDriveApi.source } returns Source.GoogleDrive
      every { mockDropboxApi.source } returns Source.Dropbox
      coEvery { mockGDriveApi.findFile(searchParams) } returns gdriveFile
      coEvery { mockDropboxApi.findFile(searchParams) } returns dropboxFile
      coEvery { remoteFileMetadataRepository.getByLocalUuIdAndSource(noteId, "GoogleDrive") } returns null
      coEvery { remoteFileMetadataRepository.getByLocalUuIdAndSource(noteId, "Dropbox") } returns null

      // Act
      val result = findNewestCloudApiSourceUseCase(dataType, noteId)

      // Assert - Should return Dropbox file (newer timestamp)
      assertNotNull(result)
      assertEquals(mockDropboxApi, result!!.cloudFileApi)
      assertEquals(dropboxFile, result.cloudFile)
      assertEquals(1698900000000L, result.cloudFile.lastModified)
    }
  }

  @Test
  fun `invoke should filter out file with up to date metadata in google drive by version`() {
    runBlocking {
      // Arrange - Google Drive file has same version as local metadata
      val dataType = DataType.Reminders
      val reminderId = "reminder-filtered"
      val cloudFile = CloudFile(
        id = "gdrive-file",
        name = "$reminderId.ta2",
        fileExtension = ".ta2",
        lastModified = 1698850000000L,
        size = 1024,
        version = 5L,  // Same version
        rev = "rev5"
      )
      val localMetadata = RemoteFileMetadata(
        id = "gdrive-file",
        name = "$reminderId.ta2",
        lastModified = 1698850000000L,  // Same timestamp
        size = 1024,
        source = "GoogleDrive",
        localUuId = reminderId,
        fileExtension = ".ta2",
        version = 5L,  // Same version
        rev = "rev5"
      )
      val searchParams = CloudFileSearchParams(
        name = reminderId,
        fileExtension = ".ta2"
      )

      every {getAllowedCloudApisUseCase.invoke() } returns listOf(mockGDriveApi)
      every { mockGDriveApi.source } returns Source.GoogleDrive
      coEvery { mockGDriveApi.findFile(searchParams) } returns cloudFile
      coEvery { remoteFileMetadataRepository.getByLocalUuIdAndSource(reminderId, "GoogleDrive") } returns localMetadata

      // Act
      val result = findNewestCloudApiSourceUseCase(dataType, reminderId)

      // Assert - Should return null (file is up-to-date)
      assertNull(result)
    }
  }

  @Test
  fun `invoke should include file with newer version in google drive`() {
    runBlocking {
      // Arrange - Google Drive file has newer version than local metadata
      val dataType = DataType.Groups
      val groupId = "group-uuid-xyz789"
      val cloudFile = CloudFile(
        id = "gdrive-group",
        name = "$groupId.gr2",
        fileExtension = ".gr2",
        lastModified = 1698900000000L,
        size = 256,
        version = 7L,  // Newer version
        rev = "rev7"
      )
      val localMetadata = RemoteFileMetadata(
        id = "gdrive-group",
        name = "$groupId.gr2",
        lastModified = 1698900000000L,  // Same timestamp
        size = 256,
        source = "GoogleDrive",
        localUuId = groupId,
        fileExtension = ".gr2",
        version = 5L,  // Older version
        rev = "rev5"
      )
      val searchParams = CloudFileSearchParams(
        name = groupId,
        fileExtension = ".gr2"
      )

      every {getAllowedCloudApisUseCase.invoke() } returns listOf(mockGDriveApi)
      every { mockGDriveApi.source } returns Source.GoogleDrive
      coEvery { mockGDriveApi.findFile(searchParams) } returns cloudFile
      coEvery { remoteFileMetadataRepository.getByLocalUuIdAndSource(groupId, "GoogleDrive") } returns localMetadata

      // Act
      val result = findNewestCloudApiSourceUseCase(dataType, groupId)

      // Assert - Should return file (newer version)
      assertNotNull(result)
      assertEquals(cloudFile, result!!.cloudFile)
    }
  }

  @Test
  fun `invoke should include file with newer timestamp in google drive`() {
    runBlocking {
      // Arrange - Google Drive file has newer timestamp (same version)
      val dataType = DataType.Places
      val placeId = "place-id-location-456"
      val cloudFile = CloudFile(
        id = "gdrive-place",
        name = "$placeId.pl2",
        fileExtension = ".pl2",
        lastModified = 1699000000000L,  // Newer timestamp
        size = 512,
        version = 3L,  // Same version
        rev = "rev3"
      )
      val localMetadata = RemoteFileMetadata(
        id = "gdrive-place",
        name = "$placeId.pl2",
        lastModified = 1698900000000L,  // Older timestamp
        size = 512,
        source = "GoogleDrive",
        localUuId = placeId,
        fileExtension = ".pl2",
        version = 3L,  // Same version
        rev = "rev3"
      )
      val searchParams = CloudFileSearchParams(
        name = placeId,
        fileExtension = ".pl2"
      )

      every {getAllowedCloudApisUseCase.invoke() } returns listOf(mockGDriveApi)
      every { mockGDriveApi.source } returns Source.GoogleDrive
      coEvery { mockGDriveApi.findFile(searchParams) } returns cloudFile
      coEvery { remoteFileMetadataRepository.getByLocalUuIdAndSource(placeId, "GoogleDrive") } returns localMetadata

      // Act
      val result = findNewestCloudApiSourceUseCase(dataType, placeId)

      // Assert - Should return file (newer timestamp)
      assertNotNull(result)
      assertEquals(cloudFile, result!!.cloudFile)
    }
  }

  @Test
  fun `invoke should filter out file with same revision in dropbox`() {
    runBlocking {
      // Arrange - Dropbox file has same revision as local metadata
      val dataType = DataType.Birthdays
      val birthdayId = "birthday-dropbox-filtered"
      val cloudFile = CloudFile(
        id = "dropbox-birthday",
        name = "$birthdayId.bi2",
        fileExtension = ".bi2",
        lastModified = 1698950000000L,
        size = 1024,
        version = 2L,
        rev = "same-revision-abc123"  // Same revision
      )
      val localMetadata = RemoteFileMetadata(
        id = "dropbox-birthday",
        name = "$birthdayId.bi2",
        lastModified = 1698950000000L,
        size = 1024,
        source = "Dropbox",
        localUuId = birthdayId,
        fileExtension = ".bi2",
        version = 2L,
        rev = "same-revision-abc123"  // Same revision
      )
      val searchParams = CloudFileSearchParams(
        name = birthdayId,
        fileExtension = ".bi2"
      )

      every {getAllowedCloudApisUseCase.invoke() } returns listOf(mockDropboxApi)
      every { mockDropboxApi.source } returns Source.Dropbox
      coEvery { mockDropboxApi.findFile(searchParams) } returns cloudFile
      coEvery { remoteFileMetadataRepository.getByLocalUuIdAndSource(birthdayId, "Dropbox") } returns localMetadata

      // Act
      val result = findNewestCloudApiSourceUseCase(dataType, birthdayId)

      // Assert - Should return null (same revision = up-to-date)
      assertNull(result)
    }
  }

  @Test
  fun `invoke should include file with different revision in dropbox`() {
    runBlocking {
      // Arrange - Dropbox file has different revision than local metadata
      val dataType = DataType.Notes
      val noteId = "note-dropbox-updated"
      val cloudFile = CloudFile(
        id = "dropbox-note",
        name = "$noteId.no3",
        fileExtension = ".no3",
        lastModified = 1699000000000L,
        size = 768,
        version = 1L,
        rev = "new-revision-xyz789"  // Different revision
      )
      val localMetadata = RemoteFileMetadata(
        id = "dropbox-note",
        name = "$noteId.no3",
        lastModified = 1699000000000L,  // Same timestamp doesn't matter for Dropbox
        size = 768,
        source = "Dropbox",
        localUuId = noteId,
        fileExtension = ".no3",
        version = 1L,
        rev = "old-revision-abc123"  // Different revision
      )
      val searchParams = CloudFileSearchParams(
        name = noteId,
        fileExtension = ".no3"
      )

      every {getAllowedCloudApisUseCase.invoke() } returns listOf(mockDropboxApi)
      every { mockDropboxApi.source } returns Source.Dropbox
      coEvery { mockDropboxApi.findFile(searchParams) } returns cloudFile
      coEvery { remoteFileMetadataRepository.getByLocalUuIdAndSource(noteId, "Dropbox") } returns localMetadata

      // Act
      val result = findNewestCloudApiSourceUseCase(dataType, noteId)

      // Assert - Should return file (different revision)
      assertNotNull(result)
      assertEquals(cloudFile, result!!.cloudFile)
      assertEquals("new-revision-xyz789", result.cloudFile.rev)
    }
  }

  @Test
  fun `invoke with no cloud apis should return null`() {
    runBlocking {
      // Arrange - No cloud APIs configured
      val dataType = DataType.Settings
      val settingsId = "settings-uuid"

      every {getAllowedCloudApisUseCase.invoke() } returns emptyList()

      // Act
      val result = findNewestCloudApiSourceUseCase(dataType, settingsId)

      // Assert - Should return null
      assertNull(result)
      coVerify(exactly = 0) { remoteFileMetadataRepository.getByLocalUuIdAndSource(any(), any()) }
    }
  }

  @Test
  fun `invoke should return file when no local metadata exists`() {
    runBlocking {
      // Arrange - File in cloud but no local metadata (new file)
      val dataType = DataType.Reminders
      val reminderId = "reminder-new-file"
      val cloudFile = CloudFile(
        id = "gdrive-new",
        name = "$reminderId.ta2",
        fileExtension = ".ta2",
        lastModified = 1699050000000L,
        size = 2048,
        version = 1L,
        rev = "rev1"
      )
      val searchParams = CloudFileSearchParams(
        name = reminderId,
        fileExtension = ".ta2"
      )

      every {getAllowedCloudApisUseCase.invoke() } returns listOf(mockGDriveApi)
      every { mockGDriveApi.source } returns Source.GoogleDrive
      coEvery { mockGDriveApi.findFile(searchParams) } returns cloudFile
      coEvery { remoteFileMetadataRepository.getByLocalUuIdAndSource(reminderId, "GoogleDrive") } returns null

      // Act
      val result = findNewestCloudApiSourceUseCase(dataType, reminderId)

      // Assert - Should return file (no metadata = new file)
      assertNotNull(result)
      assertEquals(cloudFile, result!!.cloudFile)
    }
  }

  @Test
  fun `invoke with one source having newer file and other filtered should return the newer`() {
    runBlocking {
      // Arrange - Google Drive has new file, Dropbox has up-to-date file
      val dataType = DataType.Groups
      val groupId = "group-mixed-scenario"
      val gdriveFile = CloudFile(
        id = "gdrive-group-new",
        name = "$groupId.gr2",
        fileExtension = ".gr2",
        lastModified = 1699100000000L,  // Newer
        size = 256,
        version = 5L,
        rev = "grev5"
      )
      val dropboxFile = CloudFile(
        id = "dropbox-group-old",
        name = "$groupId.gr2",
        fileExtension = ".gr2",
        lastModified = 1699000000000L,  // Older
        size = 256,
        version = 3L,
        rev = "same-rev"
      )
      val gdriveMetadata = RemoteFileMetadata(
        id = "gdrive-group-new",
        name = "$groupId.gr2",
        lastModified = 1699090000000L,  // Older than cloud
        size = 256,
        source = "GoogleDrive",
        localUuId = groupId,
        fileExtension = ".gr2",
        version = 4L,  // Older version
        rev = "grev4"
      )
      val dropboxMetadata = RemoteFileMetadata(
        id = "dropbox-group-old",
        name = "$groupId.gr2",
        lastModified = 1699000000000L,
        size = 256,
        source = "Dropbox",
        localUuId = groupId,
        fileExtension = ".gr2",
        version = 3L,
        rev = "same-rev"  // Same revision = filtered
      )
      val searchParams = CloudFileSearchParams(
        name = groupId,
        fileExtension = ".gr2"
      )

      every {getAllowedCloudApisUseCase.invoke() } returns listOf(mockGDriveApi, mockDropboxApi)
      every { mockGDriveApi.source } returns Source.GoogleDrive
      every { mockDropboxApi.source } returns Source.Dropbox
      coEvery { mockGDriveApi.findFile(searchParams) } returns gdriveFile
      coEvery { mockDropboxApi.findFile(searchParams) } returns dropboxFile
      coEvery { remoteFileMetadataRepository.getByLocalUuIdAndSource(groupId, "GoogleDrive") } returns gdriveMetadata
      coEvery { remoteFileMetadataRepository.getByLocalUuIdAndSource(groupId, "Dropbox") } returns dropboxMetadata

      // Act
      val result = findNewestCloudApiSourceUseCase(dataType, groupId)

      // Assert - Should return Google Drive file (Dropbox filtered, GDrive is newer)
      assertNotNull(result)
      assertEquals(mockGDriveApi, result!!.cloudFileApi)
      assertEquals(gdriveFile, result.cloudFile)
    }
  }

  @Test
  fun `invoke should compare timestamps correctly when both sources have valid files`() {
    runBlocking {
      // Arrange - Both sources have files, different timestamps
      val dataType = DataType.Places
      val placeId = "place-comparison"
      val gdriveFile = CloudFile(
        id = "gdrive-place-1",
        name = "$placeId.pl2",
        fileExtension = ".pl2",
        lastModified = 1699200000000L,  // Later timestamp
        size = 512,
        version = 10L,
        rev = "grev10"
      )
      val dropboxFile = CloudFile(
        id = "dropbox-place-1",
        name = "$placeId.pl2",
        fileExtension = ".pl2",
        lastModified = 1699100000000L,  // Earlier timestamp
        size = 512,
        version = 8L,
        rev = "drev8"
      )
      val searchParams = CloudFileSearchParams(
        name = placeId,
        fileExtension = ".pl2"
      )

      every {getAllowedCloudApisUseCase.invoke() } returns listOf(mockGDriveApi, mockDropboxApi)
      every { mockGDriveApi.source } returns Source.GoogleDrive
      every { mockDropboxApi.source } returns Source.Dropbox
      coEvery { mockGDriveApi.findFile(searchParams) } returns gdriveFile
      coEvery { mockDropboxApi.findFile(searchParams) } returns dropboxFile
      coEvery { remoteFileMetadataRepository.getByLocalUuIdAndSource(placeId, "GoogleDrive") } returns null
      coEvery { remoteFileMetadataRepository.getByLocalUuIdAndSource(placeId, "Dropbox") } returns null

      // Act
      val result = findNewestCloudApiSourceUseCase(dataType, placeId)

      // Assert - Should return Google Drive file (later timestamp)
      assertNotNull(result)
      assertEquals(mockGDriveApi, result!!.cloudFileApi)
      assertEquals(1699200000000L, result.cloudFile.lastModified)
    }
  }
}

