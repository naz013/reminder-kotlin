package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.cloudapi.Source
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
 * Unit tests for [FindAllFilesToDeleteUseCase].
 *
 * Tests the discovery of cloud files to be deleted,
 * including file search across multiple cloud providers,
 * filtering, and result aggregation.
 */
class FindAllFilesToDeleteUseCaseTest {

  private lateinit var getAllowedCloudApisUseCase: GetAllowedCloudApisUseCase
  private lateinit var findAllFilesToDeleteUseCase: FindAllFilesToDeleteUseCase

  private lateinit var mockCloudFileApi: CloudFileApi

  @Before
  fun setUp() {
    getAllowedCloudApisUseCase = mockk()
    mockCloudFileApi = mockk()

    findAllFilesToDeleteUseCase = FindAllFilesToDeleteUseCase(
      getAllowedCloudApisUseCase = getAllowedCloudApisUseCase
    )
  }

  @Test
  fun `invoke with reminders should find all reminder files in single source`() {
    runBlocking {
      // Arrange - Three reminder files in Google Drive
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

      every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockCloudFileApi)
      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { mockCloudFileApi.findFiles(".ta2") } returns listOf(cloudFile1, cloudFile2, cloudFile3)

      // Act
      val result = findAllFilesToDeleteUseCase(dataType)

      // Assert - Should find all three files
      assertNotNull(result)
      assertEquals(1, result!!.sources.size)
      assertEquals(3, result.sources[0].cloudFiles.size)
      assertEquals(mockCloudFileApi, result.sources[0].source)
      assertTrue(result.sources[0].cloudFiles.contains(cloudFile1))
      assertTrue(result.sources[0].cloudFiles.contains(cloudFile2))
      assertTrue(result.sources[0].cloudFiles.contains(cloudFile3))

      coVerify(exactly = 1) { mockCloudFileApi.findFiles(".ta2") }
    }
  }

  @Test
  fun `invoke when no files found should return null`() {
    runBlocking {
      // Arrange - No files found in cloud
      val dataType = DataType.Birthdays

      every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockCloudFileApi)
      coEvery { mockCloudFileApi.findFiles(".bi2") } returns emptyList()

      // Act
      val result = findAllFilesToDeleteUseCase(dataType)

      // Assert - Should return null when no files found
      assertNull(result)
      coVerify(exactly = 1) { mockCloudFileApi.findFiles(".bi2") }
    }
  }

  @Test
  fun `invoke with no cloud apis should return null`() {
    runBlocking {
      // Arrange - No cloud APIs configured
      val dataType = DataType.Notes

      every { getAllowedCloudApisUseCase.invoke() } returns emptyList()

      // Act
      val result = findAllFilesToDeleteUseCase(dataType)

      // Assert - Should return null when no APIs available
      assertNull(result)
      coVerify(exactly = 0) { mockCloudFileApi.findFiles(any()) }
    }
  }

  @Test
  fun `invoke with multiple cloud sources should aggregate files from all sources`() {
    runBlocking {
      // Arrange - Files in both Google Drive and Dropbox
      val dataType = DataType.Notes
      val mockGDriveApi = mockk<CloudFileApi>()
      val mockDropboxApi = mockk<CloudFileApi>()

      val gdriveFile = CloudFile(
        id = "gdrive-note-1",
        name = "note-key-1.no3",
        fileExtension = ".no3",
        lastModified = 1698800000000L,
        size = 512,
        version = 1L,
        rev = "gdrive-rev-1"
      )
      val dropboxFile1 = CloudFile(
        id = "dropbox-note-1",
        name = "note-key-2.no3",
        fileExtension = ".no3",
        lastModified = 1698800001000L,
        size = 768,
        version = 2L,
        rev = "dropbox-rev-1"
      )
      val dropboxFile2 = CloudFile(
        id = "dropbox-note-2",
        name = "note-key-3.no3",
        fileExtension = ".no3",
        lastModified = 1698800002000L,
        size = 1024,
        version = 1L,
        rev = "dropbox-rev-2"
      )

      every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockGDriveApi, mockDropboxApi)
      every { mockGDriveApi.source } returns Source.GoogleDrive
      every { mockDropboxApi.source } returns Source.Dropbox
      coEvery { mockGDriveApi.findFiles(".no3") } returns listOf(gdriveFile)
      coEvery { mockDropboxApi.findFiles(".no3") } returns listOf(dropboxFile1, dropboxFile2)

      // Act
      val result = findAllFilesToDeleteUseCase(dataType)

      // Assert - Should aggregate files from both sources
      assertNotNull(result)
      assertEquals(2, result!!.sources.size)

      // Verify Google Drive source
      val gdriveSource = result.sources.find { it.source == mockGDriveApi }
      assertNotNull(gdriveSource)
      assertEquals(1, gdriveSource!!.cloudFiles.size)
      assertEquals(gdriveFile, gdriveSource.cloudFiles[0])

      // Verify Dropbox source
      val dropboxSource = result.sources.find { it.source == mockDropboxApi }
      assertNotNull(dropboxSource)
      assertEquals(2, dropboxSource!!.cloudFiles.size)
      assertTrue(dropboxSource.cloudFiles.contains(dropboxFile1))
      assertTrue(dropboxSource.cloudFiles.contains(dropboxFile2))

      coVerify(exactly = 1) { mockGDriveApi.findFiles(".no3") }
      coVerify(exactly = 1) { mockDropboxApi.findFiles(".no3") }
    }
  }

  @Test
  fun `invoke should only include sources with files in result`() {
    runBlocking {
      // Arrange - Google Drive has files, Dropbox is empty
      val dataType = DataType.Groups
      val mockGDriveApi = mockk<CloudFileApi>()
      val mockDropboxApi = mockk<CloudFileApi>()

      val gdriveFile = CloudFile(
        id = "gdrive-group-1",
        name = "group-uuid-1.gr2",
        fileExtension = ".gr2",
        lastModified = 1698850000000L,
        size = 256,
        version = 1L,
        rev = "rev1"
      )

      every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockGDriveApi, mockDropboxApi)
      every { mockGDriveApi.source } returns Source.GoogleDrive
      every { mockDropboxApi.source } returns Source.Dropbox
      coEvery { mockGDriveApi.findFiles(".gr2") } returns listOf(gdriveFile)
      coEvery { mockDropboxApi.findFiles(".gr2") } returns emptyList()

      // Act
      val result = findAllFilesToDeleteUseCase(dataType)

      // Assert - Should only include Google Drive source (has files)
      assertNotNull(result)
      assertEquals(1, result!!.sources.size)
      assertEquals(mockGDriveApi, result.sources[0].source)
      assertEquals(1, result.sources[0].cloudFiles.size)

      coVerify(exactly = 1) { mockGDriveApi.findFiles(".gr2") }
      coVerify(exactly = 1) { mockDropboxApi.findFiles(".gr2") }
    }
  }

  @Test
  fun `invoke with different data types should use correct file extensions`() {
    runBlocking {
      // Arrange - Test multiple data types with their extensions
      val testCases = listOf(
        DataType.Reminders to ".ta2",
        DataType.Birthdays to ".bi2",
        DataType.Notes to ".no3",
        DataType.Groups to ".gr2",
        DataType.Places to ".pl2",
        DataType.Settings to ".settings"
      )

      for ((dataType, expectedExtension) in testCases) {
        val cloudFile = CloudFile(
          id = "test-file-${dataType.name}",
          name = "test-file$expectedExtension",
          fileExtension = expectedExtension,
          lastModified = 1698900000000L,
          size = 128,
          version = 1L,
          rev = "rev1"
        )

        every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockCloudFileApi)
        coEvery { mockCloudFileApi.findFiles(expectedExtension) } returns listOf(cloudFile)

        // Act
        val result = findAllFilesToDeleteUseCase(dataType)

        // Assert - Should use correct file extension
        assertNotNull("Failed for $dataType", result)
        coVerify(exactly = 1) { mockCloudFileApi.findFiles(expectedExtension) }
      }
    }
  }

  @Test
  fun `invoke with large number of files should return all files`() {
    runBlocking {
      // Arrange - 50 place files
      val dataType = DataType.Places
      val cloudFiles = (1..50).map { index ->
        CloudFile(
          id = "place-file-$index",
          name = "place-id-$index.pl2",
          fileExtension = ".pl2",
          lastModified = 1699000000000L + index,
          size = 100 * index,
          version = 1L,
          rev = "rev-$index"
        )
      }

      every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockCloudFileApi)
      coEvery { mockCloudFileApi.findFiles(".pl2") } returns cloudFiles

      // Act
      val result = findAllFilesToDeleteUseCase(dataType)

      // Assert - Should return all 50 files
      assertNotNull(result)
      assertEquals(1, result!!.sources.size)
      assertEquals(50, result.sources[0].cloudFiles.size)
      assertEquals(cloudFiles, result.sources[0].cloudFiles)
    }
  }

  @Test
  fun `invoke should return null when all sources have empty results`() {
    runBlocking {
      // Arrange - Three cloud APIs, all return empty lists
      val dataType = DataType.Reminders
      val mockApi1 = mockk<CloudFileApi>()
      val mockApi2 = mockk<CloudFileApi>()
      val mockApi3 = mockk<CloudFileApi>()

      every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockApi1, mockApi2, mockApi3)
      coEvery { mockApi1.findFiles(".ta2") } returns emptyList()
      coEvery { mockApi2.findFiles(".ta2") } returns emptyList()
      coEvery { mockApi3.findFiles(".ta2") } returns emptyList()

      // Act
      val result = findAllFilesToDeleteUseCase(dataType)

      // Assert - Should return null when all sources are empty
      assertNull(result)
      coVerify(exactly = 1) { mockApi1.findFiles(".ta2") }
      coVerify(exactly = 1) { mockApi2.findFiles(".ta2") }
      coVerify(exactly = 1) { mockApi3.findFiles(".ta2") }
    }
  }

  @Test
  fun `invoke with settings type should find settings files`() {
    runBlocking {
      // Arrange - Settings files with special naming
      val dataType = DataType.Settings
      val settingsFile1 = CloudFile(
        id = "settings-file-1",
        name = "app.settings",
        fileExtension = ".settings",
        lastModified = 1699050000000L,
        size = 512,
        version = 10L,
        rev = "settings-rev-10"
      )
      val settingsFile2 = CloudFile(
        id = "settings-file-2",
        name = "backup.settings",
        fileExtension = ".settings",
        lastModified = 1699050001000L,
        size = 256,
        version = 5L,
        rev = "settings-rev-5"
      )

      every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockCloudFileApi)
      coEvery { mockCloudFileApi.findFiles(".settings") } returns listOf(settingsFile1, settingsFile2)

      // Act
      val result = findAllFilesToDeleteUseCase(dataType)

      // Assert - Should find settings files
      assertNotNull(result)
      assertEquals(1, result!!.sources.size)
      assertEquals(2, result.sources[0].cloudFiles.size)
      assertTrue(result.sources[0].cloudFiles.contains(settingsFile1))
      assertTrue(result.sources[0].cloudFiles.contains(settingsFile2))
    }
  }

  @Test
  fun `invoke should preserve file metadata in result`() {
    runBlocking {
      // Arrange - File with specific metadata
      val dataType = DataType.Birthdays
      val specificFile = CloudFile(
        id = "specific-birthday-id",
        name = "birthday-uuid-12345.bi2",
        fileExtension = ".bi2",
        lastModified = 1699100000000L,
        size = 2048,
        version = 7L,
        rev = "unique-revision-abc123"
      )

      every { getAllowedCloudApisUseCase.invoke() } returns listOf(mockCloudFileApi)
      coEvery { mockCloudFileApi.findFiles(".bi2") } returns listOf(specificFile)

      // Act
      val result = findAllFilesToDeleteUseCase(dataType)

      // Assert - Should preserve all file metadata
      assertNotNull(result)
      val returnedFile = result!!.sources[0].cloudFiles[0]
      assertEquals("specific-birthday-id", returnedFile.id)
      assertEquals("birthday-uuid-12345.bi2", returnedFile.name)
      assertEquals(".bi2", returnedFile.fileExtension)
      assertEquals(1699100000000L, returnedFile.lastModified)
      assertEquals(2048, returnedFile.size)
      assertEquals(7L, returnedFile.version)
      assertEquals("unique-revision-abc123", returnedFile.rev)
    }
  }
}
