package com.github.naz013.sync.usecase.download

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.cloudapi.Source
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.files.DataType
import com.github.naz013.sync.DataPostProcessor
import com.github.naz013.sync.local.DataTypeRepositoryCaller
import com.github.naz013.sync.local.DataTypeRepositoryCallerFactory
import com.github.naz013.sync.usecase.FindAllFilesToDeleteUseCase
import com.github.naz013.sync.usecase.GetLocalUuIdUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [DownloadLegacyFilesUseCase].
 *
 * Legacy migration is one-shot: whether or not an item already exists locally, the legacy
 * cloud file must always be deleted afterwards, otherwise it gets redownloaded and
 * reprocessed on every subsequent sync forever.
 */
class DownloadLegacyFilesUseCaseTest {

  private lateinit var getAllFilesToDeleteUseCase: FindAllFilesToDeleteUseCase
  private lateinit var downloadCloudFileUseCase: DownloadCloudFileUseCase
  private lateinit var dataTypeRepositoryCallerFactory: DataTypeRepositoryCallerFactory
  private lateinit var getLocalUuIdUseCase: GetLocalUuIdUseCase
  private lateinit var dataPostProcessor: DataPostProcessor
  private lateinit var downloadLegacyFilesUseCase: DownloadLegacyFilesUseCase

  private lateinit var mockRepositoryCaller: DataTypeRepositoryCaller<Any>
  private lateinit var mockCloudFileApi: CloudFileApi

  @Before
  fun setUp() {
    getAllFilesToDeleteUseCase = mockk()
    downloadCloudFileUseCase = mockk()
    dataTypeRepositoryCallerFactory = mockk()
    getLocalUuIdUseCase = mockk()
    dataPostProcessor = mockk(relaxed = true)
    mockRepositoryCaller = mockk(relaxed = true)
    mockCloudFileApi = mockk(relaxUnitFun = true)

    downloadLegacyFilesUseCase = DownloadLegacyFilesUseCase(
      getAllFilesToDeleteUseCase = getAllFilesToDeleteUseCase,
      downloadCloudFileUseCase = downloadCloudFileUseCase,
      dataTypeRepositoryCallerFactory = dataTypeRepositoryCallerFactory,
      getLocalUuIdUseCase = getLocalUuIdUseCase,
      dataPostProcessor = dataPostProcessor
    )

    // Only DataType.Groups is legacy-and-relevant for these tests; stub the rest of the
    // legacy set to "nothing found" so they don't interfere with verification.
    DataType.entries.filter { it.isLegacy }.forEach { dataType ->
      every { dataTypeRepositoryCallerFactory.getCaller(dataType) } returns mockRepositoryCaller
      coEvery { getAllFilesToDeleteUseCase(dataType) } returns null
    }
  }

  @Test
  fun `invoke deletes the cloud file even when the item already exists locally`() {
    runBlocking {
      val group = GroupV2(uuId = "group-1", title = "Existing group")
      val cloudFile = CloudFile(
        id = "gdrive-group-1",
        name = "group-1.gr2",
        fileExtension = ".gr2",
        lastModified = 1000L,
        size = 100,
        version = 1L,
        rev = "r1"
      )
      val searchResult = FindAllFilesToDeleteUseCase.SearchResult(
        sources = listOf(
          FindAllFilesToDeleteUseCase.CloudFilesWithSource(
            source = mockCloudFileApi,
            cloudFiles = listOf(cloudFile)
          )
        )
      )

      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { getAllFilesToDeleteUseCase(DataType.Groups) } returns searchResult
      coEvery { downloadCloudFileUseCase(mockCloudFileApi, cloudFile, DataType.Groups) } returns group
      every { getLocalUuIdUseCase(group) } returns "group-1"
      coEvery { mockRepositoryCaller.getById("group-1") } returns group

      downloadLegacyFilesUseCase()

      // Already exists locally, so it must not be overwritten again...
      coVerify(exactly = 0) { mockRepositoryCaller.insertOrUpdate(any()) }
      // ...but the stale legacy cloud copy must still be cleaned up so it isn't redownloaded forever.
      coVerify(exactly = 1) { mockCloudFileApi.deleteFile("group-1.gr2") }
    }
  }

  @Test
  fun `invoke persists and deletes the cloud file for a new legacy item`() {
    runBlocking {
      val group = GroupV2(uuId = "group-2", title = "New group")
      val cloudFile = CloudFile(
        id = "gdrive-group-2",
        name = "group-2.gr2",
        fileExtension = ".gr2",
        lastModified = 1000L,
        size = 100,
        version = 1L,
        rev = "r1"
      )
      val searchResult = FindAllFilesToDeleteUseCase.SearchResult(
        sources = listOf(
          FindAllFilesToDeleteUseCase.CloudFilesWithSource(
            source = mockCloudFileApi,
            cloudFiles = listOf(cloudFile)
          )
        )
      )

      every { mockCloudFileApi.source } returns Source.GoogleDrive
      coEvery { getAllFilesToDeleteUseCase(DataType.Groups) } returns searchResult
      coEvery { downloadCloudFileUseCase(mockCloudFileApi, cloudFile, DataType.Groups) } returns group
      every { getLocalUuIdUseCase(group) } returns "group-2"
      coEvery { mockRepositoryCaller.getById("group-2") } returns null

      downloadLegacyFilesUseCase()

      coVerify(exactly = 1) { mockRepositoryCaller.insertOrUpdate(group) }
      coVerify(exactly = 1) { dataPostProcessor.process(DataType.Groups, group) }
      coVerify(exactly = 1) { mockRepositoryCaller.updateSyncState("group-2", SyncState.Synced) }
      coVerify(exactly = 1) { mockCloudFileApi.deleteFile("group-2.gr2") }
    }
  }
}
