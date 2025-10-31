package com.github.naz013.sync

import com.github.naz013.sync.cache.SyncApiSessionCache
import com.github.naz013.sync.local.DataTypeRepositoryCaller
import com.github.naz013.sync.local.DataTypeRepositoryCallerFactory
import com.github.naz013.sync.usecase.DeleteDataTypeUseCase
import com.github.naz013.sync.usecase.DeleteSingleUseCase
import com.github.naz013.sync.usecase.DownloadSingleUseCase
import com.github.naz013.sync.usecase.DownloadUseCase
import com.github.naz013.sync.usecase.GetAllowedDataTypesUseCase
import com.github.naz013.sync.usecase.HasAnyCloudApiUseCase
import com.github.naz013.sync.usecase.UploadDataTypeUseCase
import com.github.naz013.sync.usecase.UploadSingleUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [SyncApiImpl].
 *
 * Tests the main SyncApi implementation coordinating sync operations across
 * multiple data types, including sync (upload + download), upload, force upload,
 * and delete operations, with proper handling of cloud API availability and
 * Settings validation.
 */
class SyncApiImplTest {

  private lateinit var dataTypeRepositoryCallerFactory: DataTypeRepositoryCallerFactory
  private lateinit var getAllowedDataTypesUseCase: GetAllowedDataTypesUseCase
  private lateinit var uploadSingleUseCase: UploadSingleUseCase
  private lateinit var downloadSingleUseCase: DownloadSingleUseCase
  private lateinit var downloadUseCase: DownloadUseCase
  private lateinit var deleteSingleUseCase: DeleteSingleUseCase
  private lateinit var deleteDataTypeUseCase: DeleteDataTypeUseCase
  private lateinit var uploadDataTypeUseCase: UploadDataTypeUseCase
  private lateinit var hasAnyCloudApiUseCase: HasAnyCloudApiUseCase
  private lateinit var syncApi: SyncApiImpl
  private lateinit var syncApiSessionCache: SyncApiSessionCache

  private lateinit var mockRepositoryCaller: DataTypeRepositoryCaller<Any>

  @Before
  fun setUp() {
    dataTypeRepositoryCallerFactory = mockk()
    getAllowedDataTypesUseCase = mockk()
    uploadSingleUseCase = mockk(relaxed = true)
    downloadSingleUseCase = mockk()
    downloadUseCase = mockk()
    deleteSingleUseCase = mockk(relaxed = true)
    deleteDataTypeUseCase = mockk(relaxed = true)
    uploadDataTypeUseCase = mockk(relaxed = true)
    hasAnyCloudApiUseCase = mockk()
    mockRepositoryCaller = mockk(relaxed = true)
    syncApiSessionCache = mockk(relaxed = true)

    syncApi = SyncApiImpl(
      dataTypeRepositoryCallerFactory = dataTypeRepositoryCallerFactory,
      getAllowedDataTypesUseCase = getAllowedDataTypesUseCase,
      uploadSingleUseCase = uploadSingleUseCase,
      downloadSingleUseCase = downloadSingleUseCase,
      downloadUseCase = downloadUseCase,
      deleteSingleUseCase = deleteSingleUseCase,
      deleteDataTypeUseCase = deleteDataTypeUseCase,
      uploadDataTypeUseCase = uploadDataTypeUseCase,
      hasAnyCloudApiUseCase = hasAnyCloudApiUseCase,
      syncApiSessionCache = syncApiSessionCache
    )
  }

  // ==================== sync() Tests ====================

  @Test
  fun `sync without parameters should sync all allowed data types`() {
    runBlocking {
      // Arrange
      val allowedTypes = listOf(DataType.Reminders, DataType.Birthdays, DataType.Notes)
      val downloadResults = listOf(
        SyncResult.Success(downloaded = listOf(Downloaded(DataType.Reminders, "r1")), success = true),
        SyncResult.Success(downloaded = listOf(Downloaded(DataType.Birthdays, "b1")), success = true),
        SyncResult.Success(downloaded = listOf(Downloaded(DataType.Notes, "n1")), success = true)
      )

      every { hasAnyCloudApiUseCase() } returns true
      every { getAllowedDataTypesUseCase() } returns allowedTypes
      coEvery { uploadDataTypeUseCase(any()) } returns Unit
      coEvery { downloadUseCase(DataType.Reminders) } returns downloadResults[0]
      coEvery { downloadUseCase(DataType.Birthdays) } returns downloadResults[1]
      coEvery { downloadUseCase(DataType.Notes) } returns downloadResults[2]

      // Act
      val result = syncApi.sync()

      // Assert
      assertTrue(result is SyncResult.Success)
      val successResult = result as SyncResult.Success
      assertEquals(3, successResult.downloaded.size)
      assertTrue(successResult.success)

      // Verify each data type was synced
      coVerify(exactly = 1) { uploadDataTypeUseCase(DataType.Reminders) }
      coVerify(exactly = 1) { uploadDataTypeUseCase(DataType.Birthdays) }
      coVerify(exactly = 1) { uploadDataTypeUseCase(DataType.Notes) }
      coVerify(exactly = 1) { downloadUseCase(DataType.Reminders) }
      coVerify(exactly = 1) { downloadUseCase(DataType.Birthdays) }
      coVerify(exactly = 1) { downloadUseCase(DataType.Notes) }
    }
  }

  @Test
  fun `sync without parameters when no cloud api should return skipped`() {
    runBlocking {
      // Arrange
      every { hasAnyCloudApiUseCase() } returns false

      // Act
      val result = syncApi.sync()

      // Assert
      assertEquals(SyncResult.Skipped, result)
      coVerify(exactly = 0) { getAllowedDataTypesUseCase() }
      coVerify(exactly = 0) { uploadDataTypeUseCase(any()) }
      coVerify(exactly = 0) { downloadUseCase(any()) }
    }
  }

  @Test
  fun `sync without parameters when no allowed data types should return skipped`() {
    runBlocking {
      // Arrange
      every { hasAnyCloudApiUseCase() } returns true
      every { getAllowedDataTypesUseCase() } returns emptyList()

      // Act
      val result = syncApi.sync()

      // Assert
      assertEquals(SyncResult.Skipped, result)
      coVerify(exactly = 0) { uploadDataTypeUseCase(any()) }
      coVerify(exactly = 0) { downloadUseCase(any()) }
    }
  }

  @Test
  fun `sync without parameters should aggregate downloaded items from all types`() {
    runBlocking {
      // Arrange
      val allowedTypes = listOf(DataType.Reminders, DataType.Notes)
      val remindersDownloaded = listOf(
        Downloaded(DataType.Reminders, "r1"),
        Downloaded(DataType.Reminders, "r2")
      )
      val notesDownloaded = listOf(
        Downloaded(DataType.Notes, "n1")
      )

      every { hasAnyCloudApiUseCase() } returns true
      every { getAllowedDataTypesUseCase() } returns allowedTypes
      coEvery { uploadDataTypeUseCase(any()) } returns Unit
      coEvery { downloadUseCase(DataType.Reminders) } returns
        SyncResult.Success(downloaded = remindersDownloaded, success = true)
      coEvery { downloadUseCase(DataType.Notes) } returns
        SyncResult.Success(downloaded = notesDownloaded, success = true)

      // Act
      val result = syncApi.sync()

      // Assert
      assertTrue(result is SyncResult.Success)
      val successResult = result as SyncResult.Success
      assertEquals(3, successResult.downloaded.size)
      assertTrue(successResult.downloaded.containsAll(remindersDownloaded))
      assertTrue(successResult.downloaded.containsAll(notesDownloaded))
    }
  }

  // ==================== sync(dataType) Tests ====================

  @Test
  fun `sync with data type should upload then download`() {
    runBlocking {
      // Arrange
      val dataType = DataType.Reminders
      val downloadResult = SyncResult.Success(
        downloaded = listOf(Downloaded(dataType, "r1")),
        success = true
      )

      every { hasAnyCloudApiUseCase() } returns true
      coEvery { uploadDataTypeUseCase(dataType) } returns Unit
      coEvery { downloadUseCase(dataType) } returns downloadResult

      // Act
      val result = syncApi.sync(dataType)

      // Assert
      assertEquals(downloadResult, result)
      coVerify(exactly = 1) { uploadDataTypeUseCase(dataType) }
      coVerify(exactly = 1) { downloadUseCase(dataType) }
    }
  }

  @Test
  fun `sync with data type when no cloud api should return skipped`() {
    runBlocking {
      // Arrange
      every { hasAnyCloudApiUseCase() } returns false

      // Act
      val result = syncApi.sync(DataType.Birthdays)

      // Assert
      assertEquals(SyncResult.Skipped, result)
      coVerify(exactly = 0) { uploadDataTypeUseCase(any()) }
      coVerify(exactly = 0) { downloadUseCase(any()) }
    }
  }

  // ==================== sync(dataType, id) Tests ====================

  @Test
  fun `sync single item should upload then download`() {
    runBlocking {
      // Arrange
      val dataType = DataType.Notes
      val itemId = "note-key-abc123"
      val downloadResult = SyncResult.Success(
        downloaded = listOf(Downloaded(dataType, itemId)),
        success = true
      )

      every { hasAnyCloudApiUseCase() } returns true
      coEvery { uploadSingleUseCase(dataType, itemId) } returns Unit
      coEvery { downloadSingleUseCase(dataType, itemId) } returns downloadResult

      // Act
      val result = syncApi.sync(dataType, itemId)

      // Assert
      assertEquals(downloadResult, result)
      coVerify(exactly = 1) { uploadSingleUseCase(dataType, itemId) }
      coVerify(exactly = 1) { downloadSingleUseCase(dataType, itemId) }
    }
  }

  @Test
  fun `sync single item when no cloud api should return skipped`() {
    runBlocking {
      // Arrange
      every { hasAnyCloudApiUseCase() } returns false

      // Act
      val result = syncApi.sync(DataType.Places, "place-id-123")

      // Assert
      assertEquals(SyncResult.Skipped, result)
      coVerify(exactly = 0) { uploadSingleUseCase(any(), any()) }
      coVerify(exactly = 0) { downloadSingleUseCase(any(), any()) }
    }
  }

  @Test
  fun `sync single item with Settings should throw IllegalArgumentException`() {
    runBlocking {
      // Arrange
      every { hasAnyCloudApiUseCase() } returns true

      // Act & Assert
      var exceptionThrown = false
      var exceptionMessage = ""
      try {
        syncApi.sync(DataType.Settings, "settings-id")
      } catch (e: IllegalArgumentException) {
        exceptionThrown = true
        exceptionMessage = e.message ?: ""
      }

      assertTrue(exceptionThrown)
      assertEquals("Cannot sync single settings item.", exceptionMessage)
      coVerify(exactly = 0) { uploadSingleUseCase(any(), any()) }
      coVerify(exactly = 0) { downloadSingleUseCase(any(), any()) }
    }
  }

  // ==================== upload() Tests ====================

  @Test
  fun `upload without parameters should upload all allowed data types`() {
    runBlocking {
      // Arrange
      val allowedTypes = listOf(DataType.Reminders, DataType.Birthdays, DataType.Groups)

      every { hasAnyCloudApiUseCase() } returns true
      every { getAllowedDataTypesUseCase() } returns allowedTypes
      coEvery { uploadDataTypeUseCase(any()) } returns Unit

      // Act
      syncApi.upload()

      // Assert
      coVerify(exactly = 1) { uploadDataTypeUseCase(DataType.Reminders) }
      coVerify(exactly = 1) { uploadDataTypeUseCase(DataType.Birthdays) }
      coVerify(exactly = 1) { uploadDataTypeUseCase(DataType.Groups) }
    }
  }

  @Test
  fun `upload without parameters when no cloud api should return early`() {
    runBlocking {
      // Arrange
      every { hasAnyCloudApiUseCase() } returns false

      // Act
      syncApi.upload()

      // Assert
      coVerify(exactly = 0) { getAllowedDataTypesUseCase() }
      coVerify(exactly = 0) { uploadDataTypeUseCase(any()) }
    }
  }

  @Test
  fun `upload with data type should call upload data type use case`() {
    runBlocking {
      // Arrange
      val dataType = DataType.Places

      every { hasAnyCloudApiUseCase() } returns true
      coEvery { uploadDataTypeUseCase(dataType) } returns Unit

      // Act
      syncApi.upload(dataType)

      // Assert
      coVerify(exactly = 1) { uploadDataTypeUseCase(dataType) }
    }
  }

  @Test
  fun `upload with data type when no cloud api should return early`() {
    runBlocking {
      // Arrange
      every { hasAnyCloudApiUseCase() } returns false

      // Act
      syncApi.upload(DataType.Notes)

      // Assert
      coVerify(exactly = 0) { uploadDataTypeUseCase(any()) }
    }
  }

  // ==================== upload(dataType, id) Tests ====================

  @Test
  fun `upload single item should call upload single use case`() {
    runBlocking {
      // Arrange
      val dataType = DataType.Reminders
      val itemId = "reminder-uuid-12345"

      every { hasAnyCloudApiUseCase() } returns true
      coEvery { uploadSingleUseCase(dataType, itemId) } returns Unit

      // Act
      syncApi.upload(dataType, itemId)

      // Assert
      coVerify(exactly = 1) { uploadSingleUseCase(dataType, itemId) }
    }
  }

  @Test
  fun `upload single item when no cloud api should return early`() {
    runBlocking {
      // Arrange
      every { hasAnyCloudApiUseCase() } returns false

      // Act
      syncApi.upload(DataType.Birthdays, "birthday-id")

      // Assert
      coVerify(exactly = 0) { uploadSingleUseCase(any(), any()) }
    }
  }

  @Test
  fun `upload single item with Settings should throw IllegalArgumentException`() {
    runBlocking {
      // Arrange
      every { hasAnyCloudApiUseCase() } returns true

      // Act & Assert
      var exceptionThrown = false
      var exceptionMessage = ""
      try {
        syncApi.upload(DataType.Settings, "settings-id")
      } catch (e: IllegalArgumentException) {
        exceptionThrown = true
        exceptionMessage = e.message ?: ""
      }

      assertTrue(exceptionThrown)
      assertEquals("Cannot upload single settings item.", exceptionMessage)
      coVerify(exactly = 0) { uploadSingleUseCase(any(), any()) }
    }
  }

  // ==================== forceUpload() Tests ====================

  @Test
  fun `force upload without parameters should upload all items from all data types`() {
    runBlocking {
      // Arrange
      val allowedTypes = listOf(DataType.Reminders, DataType.Notes)
      val reminderIds = listOf("r1", "r2", "r3")
      val noteIds = listOf("n1", "n2")

      val mockReminderCaller = mockk<DataTypeRepositoryCaller<Any>>(relaxed = true)
      val mockNoteCaller = mockk<DataTypeRepositoryCaller<Any>>(relaxed = true)

      every { hasAnyCloudApiUseCase() } returns true
      every { getAllowedDataTypesUseCase() } returns allowedTypes
      every { dataTypeRepositoryCallerFactory.getCaller(DataType.Reminders) } returns mockReminderCaller
      every { dataTypeRepositoryCallerFactory.getCaller(DataType.Notes) } returns mockNoteCaller
      coEvery { mockReminderCaller.getAllIds() } returns reminderIds
      coEvery { mockNoteCaller.getAllIds() } returns noteIds
      coEvery { uploadSingleUseCase(any(), any()) } returns Unit

      // Act
      syncApi.forceUpload()

      // Assert - All items from all types uploaded
      reminderIds.forEach { id ->
        coVerify(exactly = 1) { uploadSingleUseCase(DataType.Reminders, id) }
      }
      noteIds.forEach { id ->
        coVerify(exactly = 1) { uploadSingleUseCase(DataType.Notes, id) }
      }
      coVerify(exactly = 5) { uploadSingleUseCase(any(), any()) } // 3 + 2 items
    }
  }

  @Test
  fun `force upload without parameters when no cloud api should return early`() {
    runBlocking {
      // Arrange
      every { hasAnyCloudApiUseCase() } returns false

      // Act
      syncApi.forceUpload()

      // Assert
      coVerify(exactly = 0) { getAllowedDataTypesUseCase() }
      coVerify(exactly = 0) { uploadSingleUseCase(any(), any()) }
    }
  }

  @Test
  fun `force upload with data type should upload all items of that type`() {
    runBlocking {
      // Arrange
      val dataType = DataType.Groups
      val groupIds = listOf("g1", "g2", "g3", "g4")

      every { hasAnyCloudApiUseCase() } returns true
      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { mockRepositoryCaller.getAllIds() } returns groupIds
      coEvery { uploadSingleUseCase(any(), any()) } returns Unit

      // Act
      syncApi.forceUpload(dataType)

      // Assert
      groupIds.forEach { id ->
        coVerify(exactly = 1) { uploadSingleUseCase(dataType, id) }
      }
      coVerify(exactly = 4) { uploadSingleUseCase(any(), any()) }
    }
  }

  @Test
  fun `force upload with data type when no cloud api should return early`() {
    runBlocking {
      // Arrange
      every { hasAnyCloudApiUseCase() } returns false

      // Act
      syncApi.forceUpload(DataType.Places)

      // Assert
      coVerify(exactly = 0) { dataTypeRepositoryCallerFactory.getCaller(any()) }
      coVerify(exactly = 0) { uploadSingleUseCase(any(), any()) }
    }
  }

  @Test
  fun `force upload with data type and id should upload single item`() {
    runBlocking {
      // Arrange
      val dataType = DataType.Birthdays
      val itemId = "birthday-uuid-xyz"

      every { hasAnyCloudApiUseCase() } returns true
      coEvery { uploadSingleUseCase(dataType, itemId) } returns Unit

      // Act
      syncApi.forceUpload(dataType, itemId)

      // Assert
      coVerify(exactly = 1) { uploadSingleUseCase(dataType, itemId) }
    }
  }

  // ==================== delete() Tests ====================

  @Test
  fun `delete without parameters should delete all allowed data types`() {
    runBlocking {
      // Arrange
      val allowedTypes = listOf(DataType.Reminders, DataType.Birthdays)

      every { hasAnyCloudApiUseCase() } returns true
      every { getAllowedDataTypesUseCase() } returns allowedTypes
      coEvery { deleteDataTypeUseCase(any()) } returns Unit

      // Act
      syncApi.delete()

      // Assert
      coVerify(exactly = 1) { deleteDataTypeUseCase(DataType.Reminders) }
      coVerify(exactly = 1) { deleteDataTypeUseCase(DataType.Birthdays) }
    }
  }

  @Test
  fun `delete without parameters when no cloud api should return early`() {
    runBlocking {
      // Arrange
      every { hasAnyCloudApiUseCase() } returns false

      // Act
      syncApi.delete()

      // Assert
      coVerify(exactly = 0) { getAllowedDataTypesUseCase() }
      coVerify(exactly = 0) { deleteDataTypeUseCase(any()) }
    }
  }

  @Test
  fun `delete with data type should call delete data type use case`() {
    runBlocking {
      // Arrange
      val dataType = DataType.Notes

      every { hasAnyCloudApiUseCase() } returns true
      coEvery { deleteDataTypeUseCase(dataType) } returns Unit

      // Act
      syncApi.delete(dataType)

      // Assert
      coVerify(exactly = 1) { deleteDataTypeUseCase(dataType) }
    }
  }

  @Test
  fun `delete with data type when no cloud api should return early`() {
    runBlocking {
      // Arrange
      every { hasAnyCloudApiUseCase() } returns false

      // Act
      syncApi.delete(DataType.Groups)

      // Assert
      coVerify(exactly = 0) { deleteDataTypeUseCase(any()) }
    }
  }

  @Test
  fun `delete single item should call delete single use case`() {
    runBlocking {
      // Arrange
      val dataType = DataType.Places
      val itemId = "place-id-location-456"

      every { hasAnyCloudApiUseCase() } returns true
      coEvery { deleteSingleUseCase(dataType, itemId) } returns Unit

      // Act
      syncApi.delete(dataType, itemId)

      // Assert
      coVerify(exactly = 1) { deleteSingleUseCase(dataType, itemId) }
    }
  }

  @Test
  fun `delete single item when no cloud api should return early`() {
    runBlocking {
      // Arrange
      every { hasAnyCloudApiUseCase() } returns false

      // Act
      syncApi.delete(DataType.Reminders, "reminder-id")

      // Assert
      coVerify(exactly = 0) { deleteSingleUseCase(any(), any()) }
    }
  }

  @Test
  fun `delete multiple items should call delete single for each id`() {
    runBlocking {
      // Arrange
      val dataType = DataType.Notes
      val itemIds = listOf("n1", "n2", "n3", "n4", "n5")

      every { hasAnyCloudApiUseCase() } returns true
      coEvery { deleteSingleUseCase(any(), any()) } returns Unit

      // Act
      syncApi.delete(dataType, itemIds)

      // Assert
      itemIds.forEach { id ->
        coVerify(exactly = 1) { deleteSingleUseCase(dataType, id) }
      }
      coVerify(exactly = 5) { deleteSingleUseCase(any(), any()) }
    }
  }

  @Test
  fun `delete multiple items when no cloud api should return early`() {
    runBlocking {
      // Arrange
      every { hasAnyCloudApiUseCase() } returns false

      // Act
      syncApi.delete(DataType.Birthdays, listOf("b1", "b2", "b3"))

      // Assert
      coVerify(exactly = 0) { deleteSingleUseCase(any(), any()) }
    }
  }

  @Test
  fun `delete multiple items with empty list should throw exception`() {
    runBlocking {
      // Arrange
      every { hasAnyCloudApiUseCase() } returns true

      // Act & Assert
      var exceptionThrown = false
      try {
        syncApi.delete(DataType.Groups, emptyList())
      } catch (e: IllegalArgumentException) {
        exceptionThrown = true
      }

      assert(exceptionThrown) { "Expected IllegalArgumentException to be thrown" }
      coVerify(exactly = 0) { deleteSingleUseCase(any(), any()) }
    }
  }
}

