package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileApi
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [FindAllFilesToDownloadUseCase].
 *
 * Tests the discovery and filtering of cloud files to download,
 * including metadata comparison, version checking, source filtering,
 * and result aggregation across multiple cloud providers.
 */
class FindAllFilesToDownloadUseCaseTest {

  private lateinit var cloudApiProvider: CloudApiProvider
  private lateinit var remoteFileMetadataRepository: RemoteFileMetadataRepository
  private lateinit var findAllFilesToDownloadUseCase: FindAllFilesToDownloadUseCase

  private lateinit var mockCloudFileApi: CloudFileApi

  @Before
  fun setUp() {
    cloudApiProvider = mockk()
    remoteFileMetadataRepository = mockk()
    mockCloudFileApi = mockk(relaxUnitFun = true)

    findAllFilesToDownloadUseCase = FindAllFilesToDownloadUseCase(
      cloudApiProvider = cloudApiProvider,
      remoteFileMetadataRepository = remoteFileMetadataRepository
    )
  }

  @Test
  fun `invoke with new files should return all files without local metadata`() {
    runBlocking {
      // Arrange - Cloud has 3 reminders, none exist locally
      val dataType = DataType.Reminders
      val cloudFile1 = CloudFile(
        id = "gdrive-reminder-1",
        name = "reminder-uuid-1.ta2",
        fileExtension = ".ta2",
        lastModified = 1698765432000L,
        size = 2048,
        version = 3L,
        rev = "rev3"
      )
      val cloudFile2 = CloudFile(
        id = "gdrive-reminder-2",
        name = "reminder-uuid-2.ta2",
        fileExtension = ".ta2",
        lastModified = 1698765433000L,
        size = 1024,
        version = 2L,
        rev = "rev2"
      )
      val cloudFile3 = CloudFile(
        id = "gdrive-reminder-3",
        name = "reminder-uuid-3.ta2",
        fileExtension = ".ta2",
        lastModified = 1698765434000L,
        size = 4096,
        version = 1L,
        rev = "rev1"
      )

      every { cloudApiProvider.getAllowedCloudApis() } returns listOf(mockCloudFileApi)
      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.findFiles(".ta2") } returns listOf(cloudFile1, cloudFile2, cloudFile3)
      coEvery { remoteFileMetadataRepository.getBySource("GoogleDrive") } returns emptyList()

      // Act
      val result = findAllFilesToDownloadUseCase(dataType)

      // Assert - All 3 files should be returned (no local metadata = new files)
      assertNotNull(result)
      assertEquals(1, result!!.sources.size)
      assertEquals(3, result.sources[0].cloudFiles.size)
      assertTrue(result.sources[0].cloudFiles.contains(cloudFile1))
      assertTrue(result.sources[0].cloudFiles.contains(cloudFile2))
      assertTrue(result.sources[0].cloudFiles.contains(cloudFile3))

      coVerify(exactly = 1) { mockCloudFileApi.findFiles(".ta2") }
      coVerify(exactly = 1) { remoteFileMetadataRepository.getBySource("GoogleDrive") }
    }
  }

  @Test
  fun `invoke when no files found should return null`() {
    runBlocking {
      // Arrange - Cloud has no files
      val dataType = DataType.Birthdays

      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.findFiles(".gr2") } returns emptyList()
      coEvery { remoteFileMetadataRepository.getBySource("GoogleDrive") } returns emptyList()
      every { cloudApiProvider.getAllowedCloudApis() } returns listOf(mockCloudFileApi)

      // Act
      val result = findAllFilesToDownloadUseCase(dataType)

      // Assert - Should return null
      assertNull(result)
    }
  }

  @Test
  fun `invoke with updated file in google drive should detect version change`() {
    runBlocking {
      // Arrange - Cloud file has newer version
      val dataType = DataType.Notes
      val cloudFile = CloudFile(
        id = "gdrive-note-1",
        name = "note-key-1.no2",
        fileExtension = ".no2",
        lastModified = 1698800000000L,
        size = 512,
        version = 5L,  // Newer version
        rev = "rev5"
      )
      val localMetadata = RemoteFileMetadata(
        id = "gdrive-note-1",
        name = "note-key-1.no2",
        lastModified = 1698799000000L,
        size = 512,
        source = "GoogleDrive",
        localUuId = "note-1",
        fileExtension = ".no2",
        version = 3L,  // Older version
        rev = "rev3"
      )

      every { cloudApiProvider.getAllowedCloudApis() } returns listOf(mockCloudFileApi)
      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.findFiles(".no2") } returns listOf(cloudFile)
      coEvery { remoteFileMetadataRepository.getBySource("GoogleDrive") } returns listOf(localMetadata)

      // Act
      val result = findAllFilesToDownloadUseCase(dataType)

      // Assert - Should detect version change and return file
      assertNotNull(result)
      assertEquals(1, result!!.sources.size)
      assertEquals(1, result.sources[0].cloudFiles.size)
      assertEquals(cloudFile, result.sources[0].cloudFiles[0])
    }
  }

  @Test
  fun `invoke with updated file in google drive should detect timestamp change`() {
    runBlocking {
      // Arrange - Cloud file has newer timestamp (same version)
      val dataType = DataType.Reminders
      val cloudFile = CloudFile(
        id = "gdrive-reminder-1",
        name = "reminder-uuid-1.ta2",
        fileExtension = ".ta2",
        lastModified = 1698900000000L,  // Newer timestamp
        size = 2048,
        version = 3L,  // Same version
        rev = "rev3"
      )
      val localMetadata = RemoteFileMetadata(
        id = "gdrive-reminder-1",
        name = "reminder-uuid-1.ta2",
        lastModified = 1698800000000L,  // Older timestamp
        size = 2048,
        source = "GoogleDrive",
        localUuId = "reminder-1",
        fileExtension = ".ta2",
        version = 3L,  // Same version
        rev = "rev3"
      )

      every { cloudApiProvider.getAllowedCloudApis() } returns listOf(mockCloudFileApi)
      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.findFiles(".ta2") } returns listOf(cloudFile)
      coEvery { remoteFileMetadataRepository.getBySource("GoogleDrive") } returns listOf(localMetadata)

      // Act
      val result = findAllFilesToDownloadUseCase(dataType)

      // Assert - Should detect timestamp change
      assertNotNull(result)
      assertEquals(1, result!!.sources[0].cloudFiles.size)
      assertEquals(cloudFile, result.sources[0].cloudFiles[0])
    }
  }

  @Test
  fun `invoke with updated file in dropbox should detect revision change`() {
    runBlocking {
      // Arrange - Dropbox file has different revision
      val dataType = DataType.Birthdays
      val cloudFile = CloudFile(
        id = "dropbox-birthday-1",
        name = "birthday-uuid-1.gr2",
        fileExtension = ".gr2",
        lastModified = 1698850000000L,
        size = 1024,
        version = 1L,
        rev = "new-revision-xyz"  // Different revision
      )
      val localMetadata = RemoteFileMetadata(
        id = "dropbox-birthday-1",
        name = "birthday-uuid-1.gr2",
        lastModified = 1698850000000L,
        size = 1024,
        source = "Dropbox",
        localUuId = "birthday-1",
        fileExtension = ".gr2",
        version = 1L,
        rev = "old-revision-abc"  // Old revision
      )

      every { cloudApiProvider.getAllowedCloudApis() } returns listOf(mockCloudFileApi)
      every { mockCloudFileApi.source } returns Source.Dropbox
      coEvery { mockCloudFileApi.findFiles(".gr2") } returns listOf(cloudFile)
      coEvery { remoteFileMetadataRepository.getBySource("Dropbox") } returns listOf(localMetadata)

      // Act
      val result = findAllFilesToDownloadUseCase(dataType)

      // Assert - Should detect revision change (Dropbox uses rev)
      assertNotNull(result)
      assertEquals(1, result!!.sources[0].cloudFiles.size)
      assertEquals(cloudFile, result.sources[0].cloudFiles[0])
    }
  }

  @Test
  fun `invoke should filter out up to date files`() {
    runBlocking {
      // Arrange - 3 files: 1 new, 1 updated, 1 up-to-date
      val dataType = DataType.Groups
      val newFile = CloudFile(
        id = "new-group",
        name = "new-group.bi2",
        fileExtension = ".bi2",
        lastModified = 1000L,
        size = 100,
        version = 1L,
        rev = "r1"
      )
      val updatedFile = CloudFile(
        id = "updated-group",
        name = "updated-group.bi2",
        fileExtension = ".bi2",
        lastModified = 2000L,  // Newer
        size = 200,
        version = 2L,
        rev = "r2"
      )
      val upToDateFile = CloudFile(
        id = "current-group",
        name = "current-group.bi2",
        fileExtension = ".bi2",
        lastModified = 3000L,  // Same
        size = 300,
        version = 3L,  // Same
        rev = "r3"
      )
      val upToDateMetadata = RemoteFileMetadata(
        id = "current-group",
        name = "current-group.bi2",
        lastModified = 3000L,  // Same
        size = 300,
        source = "GoogleDrive",
        localUuId = "current",
        fileExtension = ".bi2",
        version = 3L,  // Same
        rev = "r3"
      )
      val outdatedMetadata = RemoteFileMetadata(
        id = "updated-group",
        name = "updated-group.bi2",
        lastModified = 1500L,  // Older
        size = 200,
        source = "GoogleDrive",
        localUuId = "updated",
        fileExtension = ".bi2",
        version = 1L,
        rev = "r1"
      )

      every { cloudApiProvider.getAllowedCloudApis() } returns listOf(mockCloudFileApi)
      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.findFiles(".bi2") } returns listOf(newFile, updatedFile, upToDateFile)
      coEvery { remoteFileMetadataRepository.getBySource("GoogleDrive") } returns
        listOf(upToDateMetadata, outdatedMetadata)

      // Act
      val result = findAllFilesToDownloadUseCase(dataType)

      // Assert - Should return only new and updated files (2 files)
      assertNotNull(result)
      assertEquals(1, result!!.sources.size)
      assertEquals(2, result.sources[0].cloudFiles.size)
      assertTrue(result.sources[0].cloudFiles.contains(newFile))
      assertTrue(result.sources[0].cloudFiles.contains(updatedFile))
      assertTrue(!result.sources[0].cloudFiles.contains(upToDateFile))
    }
  }

  @Test
  fun `invoke with multiple cloud sources should aggregate filtered files from all sources`() {
    runBlocking {
      // Arrange - Files in both Google Drive and Dropbox
      val dataType = DataType.Places
      val mockGDriveApi = mockk<CloudFileApi>()
      val mockDropboxApi = mockk<CloudFileApi>()

      val gdriveFile = CloudFile(
        id = "gdrive-place-1",
        name = "place-1.pl2",
        fileExtension = ".pl2",
        lastModified = 1000L,
        size = 100,
        version = 2L,  // Updated
        rev = "gr1"
      )
      val gdriveMetadata = RemoteFileMetadata(
        id = "gdrive-place-1",
        name = "place-1.pl2",
        lastModified = 900L,
        size = 100,
        source = "GoogleDrive",
        localUuId = "place-1",
        fileExtension = ".pl2",
        version = 1L,  // Old version
        rev = "gr0"
      )

      val dropboxFile = CloudFile(
        id = "dropbox-place-2",
        name = "place-2.pl2",
        fileExtension = ".pl2",
        lastModified = 2000L,
        size = 200,
        version = 1L,
        rev = "new-rev"  // New revision
      )
      val dropboxMetadata = RemoteFileMetadata(
        id = "dropbox-place-2",
        name = "place-2.pl2",
        lastModified = 2000L,
        size = 200,
        source = "Dropbox",
        localUuId = "place-2",
        fileExtension = ".pl2",
        version = 1L,
        rev = "old-rev"  // Old revision
      )

      every { cloudApiProvider.getAllowedCloudApis() } returns listOf(mockGDriveApi, mockDropboxApi)
      every { mockGDriveApi.source } returns Source.GoogleDrive
      every { mockDropboxApi.source } returns Source.Dropbox
      coEvery { mockGDriveApi.findFiles(".pl2") } returns listOf(gdriveFile)
      coEvery { mockDropboxApi.findFiles(".pl2") } returns listOf(dropboxFile)
      coEvery { remoteFileMetadataRepository.getBySource("GoogleDrive") } returns listOf(gdriveMetadata)
      coEvery { remoteFileMetadataRepository.getBySource("Dropbox") } returns listOf(dropboxMetadata)

      // Act
      val result = findAllFilesToDownloadUseCase(dataType)

      // Assert - Should aggregate files from both sources
      assertNotNull(result)
      assertEquals(2, result!!.sources.size)

      val gdriveSource = result.sources.find { it.source == mockGDriveApi }
      assertNotNull(gdriveSource)
      assertEquals(1, gdriveSource!!.cloudFiles.size)
      assertEquals(gdriveFile, gdriveSource.cloudFiles[0])

      val dropboxSource = result.sources.find { it.source == mockDropboxApi }
      assertNotNull(dropboxSource)
      assertEquals(1, dropboxSource!!.cloudFiles.size)
      assertEquals(dropboxFile, dropboxSource.cloudFiles[0])
    }
  }

  @Test
  fun `invoke should only include sources with downloadable files in result`() {
    runBlocking {
      // Arrange - Google Drive has new files, Dropbox has only up-to-date files
      val dataType = DataType.Notes
      val mockGDriveApi = mockk<CloudFileApi>()
      val mockDropboxApi = mockk<CloudFileApi>()

      val gdriveFile = CloudFile(
        id = "gdrive-note-1",
        name = "note-1.no2",
        fileExtension = ".no2",
        lastModified = 1000L,
        size = 100,
        version = 1L,
        rev = "r1"
      )

      val dropboxFile = CloudFile(
        id = "dropbox-note-2",
        name = "note-2.no2",
        fileExtension = ".no2",
        lastModified = 2000L,
        size = 200,
        version = 1L,
        rev = "same-rev"
      )
      val dropboxMetadata = RemoteFileMetadata(
        id = "dropbox-note-2",
        name = "note-2.no2",
        lastModified = 2000L,
        size = 200,
        source = "Dropbox",
        localUuId = "note-2",
        fileExtension = ".no2",
        version = 1L,
        rev = "same-rev"  // Same revision = up-to-date
      )

      every { cloudApiProvider.getAllowedCloudApis() } returns listOf(mockGDriveApi, mockDropboxApi)
      every { mockGDriveApi.source } returns Source.GoogleDrive
      every { mockDropboxApi.source } returns Source.Dropbox
      coEvery { mockGDriveApi.findFiles(".no2") } returns listOf(gdriveFile)
      coEvery { mockDropboxApi.findFiles(".no2") } returns listOf(dropboxFile)
      coEvery { remoteFileMetadataRepository.getBySource("GoogleDrive") } returns emptyList()
      coEvery { remoteFileMetadataRepository.getBySource("Dropbox") } returns listOf(dropboxMetadata)

      // Act
      val result = findAllFilesToDownloadUseCase(dataType)

      // Assert - Should only include Google Drive (has new file)
      assertNotNull(result)
      assertEquals(1, result!!.sources.size)
      assertEquals(mockGDriveApi, result.sources[0].source)
    }
  }

  @Test
  fun `invoke should return null when all files are up to date`() {
    runBlocking {
      // Arrange - All cloud files match local metadata
      val dataType = DataType.Reminders
      val cloudFile = CloudFile(
        id = "reminder-1",
        name = "reminder-1.ta2",
        fileExtension = ".ta2",
        lastModified = 1000L,
        size = 100,
        version = 5L,
        rev = "r5"
      )
      val localMetadata = RemoteFileMetadata(
        id = "reminder-1",
        name = "reminder-1.ta2",
        lastModified = 1000L,  // Same
        size = 100,
        source = "GoogleDrive",
        localUuId = "r1",
        fileExtension = ".ta2",
        version = 5L,  // Same
        rev = "r5"
      )

      every { cloudApiProvider.getAllowedCloudApis() } returns listOf(mockCloudFileApi)
      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.findFiles(".ta2") } returns listOf(cloudFile)
      coEvery { remoteFileMetadataRepository.getBySource("GoogleDrive") } returns listOf(localMetadata)

      // Act
      val result = findAllFilesToDownloadUseCase(dataType)

      // Assert - Should return null (all files up-to-date)
      assertNull(result)
    }
  }

  @Test
  fun `invoke with no cloud apis should return null`() {
    runBlocking {
      // Arrange - No cloud APIs configured
      val dataType = DataType.Settings

      every { cloudApiProvider.getAllowedCloudApis() } returns emptyList()

      // Act
      val result = findAllFilesToDownloadUseCase(dataType)

      // Assert - Should return null
      assertNull(result)
      coVerify(exactly = 0) { remoteFileMetadataRepository.getBySource(any()) }
    }
  }

  @Test
  fun `invoke should use metadata name as key for matching cloud files`() {
    runBlocking {
      // Arrange - Multiple files with some matching metadata by name
      val dataType = DataType.Birthdays
      val file1 = CloudFile(id = "f1", name = "b1.gr2", fileExtension = ".gr2",
        lastModified = 1000L, size = 100, version = 2L, rev = "r2")
      val file2 = CloudFile(id = "f2", name = "b2.gr2", fileExtension = ".gr2",
        lastModified = 2000L, size = 200, version = 1L, rev = "r1")
      val file3 = CloudFile(id = "f3", name = "b3.gr2", fileExtension = ".gr2",
        lastModified = 3000L, size = 300, version = 1L, rev = "r1")

      val metadata1 = RemoteFileMetadata(
        id = "f1", name = "b1.gr2", lastModified = 1000L, size = 100,
        source = "GoogleDrive", localUuId = "b1", fileExtension = ".gr2",
        version = 2L, rev = "r2"  // Same version - up-to-date
      )
      val metadata2 = RemoteFileMetadata(
        id = "f2", name = "b2.gr2", lastModified = 1500L, size = 200,
        source = "GoogleDrive", localUuId = "b2", fileExtension = ".gr2",
        version = 1L, rev = "r0"  // Different version - needs download
      )
      // No metadata for file3 - it's new

      every { cloudApiProvider.getAllowedCloudApis() } returns listOf(mockCloudFileApi)
      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.findFiles(".gr2") } returns listOf(file1, file2, file3)
      coEvery { remoteFileMetadataRepository.getBySource("GoogleDrive") } returns
        listOf(metadata1, metadata2)

      // Act
      val result = findAllFilesToDownloadUseCase(dataType)

      // Assert - Should return file2 (updated) and file3 (new), but not file1 (up-to-date)
      assertNotNull(result)
      assertEquals(1, result!!.sources.size)
      assertEquals(2, result.sources[0].cloudFiles.size)
      assertTrue(result.sources[0].cloudFiles.contains(file2))
      assertTrue(result.sources[0].cloudFiles.contains(file3))
      assertTrue(!result.sources[0].cloudFiles.contains(file1))
    }
  }
}

