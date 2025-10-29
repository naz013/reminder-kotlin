package com.github.naz013.sync.usecase

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.sync.DataType
import com.github.naz013.sync.local.DataTypeRepositoryCaller
import com.github.naz013.sync.local.DataTypeRepositoryCallerFactory
import com.github.naz013.sync.settings.UploadSettingsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [UploadDataTypeUseCase].
 *
 * Tests the batch upload of all items of a specific data type that need syncing,
 * including handling of Settings (special case), empty results, multiple items,
 * and different sync states (WaitingForUpload and FailedToUpload).
 */
class UploadDataTypeUseCaseTest {

  private lateinit var uploadSingleUseCase: UploadSingleUseCase
  private lateinit var uploadSettingsUseCase: UploadSettingsUseCase
  private lateinit var dataTypeRepositoryCallerFactory: DataTypeRepositoryCallerFactory
  private lateinit var uploadDataTypeUseCase: UploadDataTypeUseCase

  private lateinit var mockRepositoryCaller: DataTypeRepositoryCaller<Any>

  @Before
  fun setUp() {
    uploadSingleUseCase = mockk(relaxed = true)
    uploadSettingsUseCase = mockk(relaxed = true)
    dataTypeRepositoryCallerFactory = mockk()
    mockRepositoryCaller = mockk(relaxed = true)

    uploadDataTypeUseCase = UploadDataTypeUseCase(
      uploadSingleUseCase = uploadSingleUseCase,
      uploadSettingsUseCase = uploadSettingsUseCase,
      dataTypeRepositoryCallerFactory = dataTypeRepositoryCallerFactory
    )
  }

  @Test
  fun `invoke with settings data type should call upload settings use case`() {
    runBlocking {
      // Arrange - Settings data type has special handling
      val dataType = DataType.Settings

      // Act
      uploadDataTypeUseCase(dataType)

      // Assert - Should call uploadSettingsUseCase, not uploadSingleUseCase
      coVerify(exactly = 1) { uploadSettingsUseCase() }
      coVerify(exactly = 0) { uploadSingleUseCase(any(), any()) }
      coVerify(exactly = 0) { dataTypeRepositoryCallerFactory.getCaller(any()) }
    }
  }

  @Test
  fun `invoke with reminders and items waiting for upload should upload all items`() {
    runBlocking {
      // Arrange - Three reminders waiting to be uploaded
      val dataType = DataType.Reminders
      val idsToUpload = listOf("reminder-1", "reminder-2", "reminder-3")

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery {
        mockRepositoryCaller.getIdsByState(listOf(SyncState.WaitingForUpload, SyncState.FailedToUpload))
      } returns idsToUpload

      // Act
      uploadDataTypeUseCase(dataType)

      // Assert - Should upload each item
      coVerify(exactly = 1) { mockRepositoryCaller.getIdsByState(listOf(SyncState.WaitingForUpload, SyncState.FailedToUpload)) }
      coVerify(exactly = 1) { uploadSingleUseCase(dataType, "reminder-1") }
      coVerify(exactly = 1) { uploadSingleUseCase(dataType, "reminder-2") }
      coVerify(exactly = 1) { uploadSingleUseCase(dataType, "reminder-3") }
      coVerify(exactly = 3) { uploadSingleUseCase(any(), any()) }
    }
  }

  @Test
  fun `invoke with no items to upload should return early without calling upload single`() {
    runBlocking {
      // Arrange - No items waiting for upload
      val dataType = DataType.Birthdays

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery {
        mockRepositoryCaller.getIdsByState(listOf(SyncState.WaitingForUpload, SyncState.FailedToUpload))
      } returns emptyList()

      // Act
      uploadDataTypeUseCase(dataType)

      // Assert - Should not call uploadSingleUseCase
      coVerify(exactly = 1) { mockRepositoryCaller.getIdsByState(any()) }
      coVerify(exactly = 0) { uploadSingleUseCase(any(), any()) }
    }
  }

  @Test
  fun `invoke should query for both waiting and failed to upload states`() {
    runBlocking {
      // Arrange - Verify correct sync states are queried
      val dataType = DataType.Notes
      val expectedStates = listOf(SyncState.WaitingForUpload, SyncState.FailedToUpload)

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { mockRepositoryCaller.getIdsByState(expectedStates) } returns emptyList()

      // Act
      uploadDataTypeUseCase(dataType)

      // Assert - Should query for both WaitingForUpload and FailedToUpload
      coVerify(exactly = 1) { mockRepositoryCaller.getIdsByState(expectedStates) }
    }
  }

  @Test
  fun `invoke with single item should upload only that item`() {
    runBlocking {
      // Arrange - Only one group needs upload
      val dataType = DataType.Groups
      val singleId = "group-uuid-xyz789"

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery {
        mockRepositoryCaller.getIdsByState(listOf(SyncState.WaitingForUpload, SyncState.FailedToUpload))
      } returns listOf(singleId)

      // Act
      uploadDataTypeUseCase(dataType)

      // Assert - Should upload exactly one item
      coVerify(exactly = 1) { uploadSingleUseCase(dataType, singleId) }
      coVerify(exactly = 1) { uploadSingleUseCase(any(), any()) }
    }
  }

  @Test
  fun `invoke with large number of items should upload all sequentially`() {
    runBlocking {
      // Arrange - 20 places need upload
      val dataType = DataType.Places
      val idsToUpload = (1..20).map { "place-id-$it" }

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery {
        mockRepositoryCaller.getIdsByState(listOf(SyncState.WaitingForUpload, SyncState.FailedToUpload))
      } returns idsToUpload

      // Act
      uploadDataTypeUseCase(dataType)

      // Assert - Should upload all 20 items
      coVerify(exactly = 20) { uploadSingleUseCase(any(), any()) }
      idsToUpload.forEach { id ->
        coVerify(exactly = 1) { uploadSingleUseCase(dataType, id) }
      }
    }
  }

  @Test
  fun `invoke with different data types should use correct repository caller`() {
    runBlocking {
      // Arrange - Test multiple data types
      val dataTypes = listOf(
        DataType.Reminders,
        DataType.Birthdays,
        DataType.Notes,
        DataType.Groups,
        DataType.Places
      )

      for (dataType in dataTypes) {
        val mockCaller = mockk<DataTypeRepositoryCaller<Any>>(relaxed = true)
        every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockCaller
        coEvery {
          mockCaller.getIdsByState(listOf(SyncState.WaitingForUpload, SyncState.FailedToUpload))
        } returns emptyList()

        // Act
        uploadDataTypeUseCase(dataType)

        // Assert - Should get correct caller for each type
        coVerify(exactly = 1) { dataTypeRepositoryCallerFactory.getCaller(dataType) }
      }
    }
  }

  @Test
  fun `invoke should process items in order they are returned`() {
    runBlocking {
      // Arrange - Items in specific order
      val dataType = DataType.Reminders
      val orderedIds = listOf("first", "second", "third", "fourth")
      val callOrder = mutableListOf<String>()

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery {
        mockRepositoryCaller.getIdsByState(listOf(SyncState.WaitingForUpload, SyncState.FailedToUpload))
      } returns orderedIds
      coEvery { uploadSingleUseCase(any(), any()) } answers {
        callOrder.add(secondArg())
      }

      // Act
      uploadDataTypeUseCase(dataType)

      // Assert - Should process in same order
      assert(callOrder == orderedIds) { "Expected order: $orderedIds, but got: $callOrder" }
    }
  }

  @Test
  fun `invoke when upload single fails should continue with remaining items`() {
    runBlocking {
      // Arrange - Second item fails, should continue with third
      val dataType = DataType.Notes
      val idsToUpload = listOf("note-1", "note-2", "note-3")

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery {
        mockRepositoryCaller.getIdsByState(listOf(SyncState.WaitingForUpload, SyncState.FailedToUpload))
      } returns idsToUpload
      coEvery { uploadSingleUseCase(dataType, "note-1") } returns Unit
      coEvery { uploadSingleUseCase(dataType, "note-2") } throws RuntimeException("Upload failed")
      coEvery { uploadSingleUseCase(dataType, "note-3") } returns Unit

      // Act & Assert - Exception should propagate (no try-catch in implementation)
      var exceptionThrown = false
      try {
        uploadDataTypeUseCase(dataType)
      } catch (e: RuntimeException) {
        exceptionThrown = true
      }

      // Assert - First item processed, exception thrown on second
      assert(exceptionThrown) { "Expected exception to be thrown" }
      coVerify(exactly = 1) { uploadSingleUseCase(dataType, "note-1") }
      coVerify(exactly = 1) { uploadSingleUseCase(dataType, "note-2") }
      coVerify(exactly = 0) { uploadSingleUseCase(dataType, "note-3") } // Not reached due to exception
    }
  }

  @Test
  fun `invoke with settings should not query repository`() {
    runBlocking {
      // Arrange - Settings uses different upload path
      val dataType = DataType.Settings

      // Act
      uploadDataTypeUseCase(dataType)

      // Assert - Should not interact with repository at all
      coVerify(exactly = 0) { dataTypeRepositoryCallerFactory.getCaller(any()) }
      coVerify(exactly = 0) { mockRepositoryCaller.getIdsByState(any()) }
    }
  }

  @Test
  fun `invoke multiple times with same data type should process independently`() {
    runBlocking {
      // Arrange - Multiple invocations with same data type
      val dataType = DataType.Birthdays
      val firstCallIds = listOf("b1", "b2")
      val secondCallIds = listOf("b3", "b4", "b5")

      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery {
        mockRepositoryCaller.getIdsByState(listOf(SyncState.WaitingForUpload, SyncState.FailedToUpload))
      } returnsMany listOf(firstCallIds, secondCallIds)

      // Act - Call twice
      uploadDataTypeUseCase(dataType)
      uploadDataTypeUseCase(dataType)

      // Assert - Should process items from both calls
      coVerify(exactly = 2) { mockRepositoryCaller.getIdsByState(any()) }
      coVerify(exactly = 5) { uploadSingleUseCase(any(), any()) } // 2 + 3 items
      firstCallIds.forEach { id ->
        coVerify(exactly = 1) { uploadSingleUseCase(dataType, id) }
      }
      secondCallIds.forEach { id ->
        coVerify(exactly = 1) { uploadSingleUseCase(dataType, id) }
      }
    }
  }
}

