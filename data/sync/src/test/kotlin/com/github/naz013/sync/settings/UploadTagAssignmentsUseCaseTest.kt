package com.github.naz013.sync.settings

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.domain.TagAssignment
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.files.DataType
import com.github.naz013.files.model.TagAssignmentRowJson
import com.github.naz013.files.model.TagAssignmentsSnapshotJson
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.sync.SyncDataConverter
import com.github.naz013.sync.usecase.CreateCloudFileUseCase
import com.github.naz013.sync.usecase.GetAllowedCloudApisUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class UploadTagAssignmentsUseCaseTest {

  private lateinit var createCloudFileUseCase: CreateCloudFileUseCase
  private lateinit var tagAssignmentRepository: TagAssignmentRepository
  private lateinit var getAllowedCloudApisUseCase: GetAllowedCloudApisUseCase
  private lateinit var syncDataConverter: SyncDataConverter
  private lateinit var uploadTagAssignmentsUseCase: UploadTagAssignmentsUseCase

  private lateinit var mockCloudFileApi: CloudFileApi

  private val expectedSnapshot = TagAssignmentsSnapshotJson(
    assignments = listOf(TagAssignmentRowJson(tagId = "tag-1", itemId = "note-1", itemType = "NOTE"))
  )

  @Before
  fun setUp() {
    createCloudFileUseCase = mockk()
    tagAssignmentRepository = mockk()
    getAllowedCloudApisUseCase = mockk()
    syncDataConverter = mockk()
    mockCloudFileApi = mockk()

    uploadTagAssignmentsUseCase = UploadTagAssignmentsUseCase(
      createCloudFileUseCase = createCloudFileUseCase,
      tagAssignmentRepository = tagAssignmentRepository,
      getAllowedCloudApisUseCase = getAllowedCloudApisUseCase,
      syncDataConverter = syncDataConverter
    )

    coEvery { tagAssignmentRepository.getAll() } returns listOf(
      TagAssignment(tagId = "tag-1", itemId = "note-1", itemType = TaggedItemType.NOTE)
    )
  }

  @Test
  fun `invoke builds a fresh snapshot from the repository and uploads it`() = runBlocking {
    val cloudFile = CloudFile(id = "id", name = "app.tga1", fileExtension = ".tga1")
    val inputStream = ByteArrayInputStream("data".toByteArray())

    coEvery { createCloudFileUseCase(DataType.TagAssignments, expectedSnapshot) } returns cloudFile
    every { getAllowedCloudApisUseCase() } returns listOf(mockCloudFileApi)
    coEvery { syncDataConverter.create(expectedSnapshot) } returns inputStream
    coEvery { mockCloudFileApi.uploadFile(any(), any()) } returns cloudFile

    uploadTagAssignmentsUseCase()

    coVerify(exactly = 1) { tagAssignmentRepository.getAll() }
    coVerify(exactly = 1) { mockCloudFileApi.uploadFile(inputStream, cloudFile) }
  }

  @Test
  fun `invoke uploads to every allowed cloud api with its own stream`() = runBlocking {
    val cloudFile = CloudFile(id = "id", name = "app.tga1", fileExtension = ".tga1")
    val stream1 = ByteArrayInputStream("data1".toByteArray())
    val stream2 = ByteArrayInputStream("data2".toByteArray())
    val api2 = mockk<CloudFileApi>()

    coEvery { createCloudFileUseCase(DataType.TagAssignments, expectedSnapshot) } returns cloudFile
    every { getAllowedCloudApisUseCase() } returns listOf(mockCloudFileApi, api2)
    coEvery { syncDataConverter.create(expectedSnapshot) } returnsMany listOf(stream1, stream2)
    coEvery { mockCloudFileApi.uploadFile(any(), any()) } returns cloudFile
    coEvery { api2.uploadFile(any(), any()) } returns cloudFile

    uploadTagAssignmentsUseCase()

    coVerify(exactly = 2) { syncDataConverter.create(expectedSnapshot) }
    coVerify(exactly = 1) { mockCloudFileApi.uploadFile(stream1, cloudFile) }
    coVerify(exactly = 1) { api2.uploadFile(stream2, cloudFile) }
  }

  @Test
  fun `invoke uploads an empty snapshot when there are no assignments`() = runBlocking {
    coEvery { tagAssignmentRepository.getAll() } returns emptyList()
    val emptySnapshot = TagAssignmentsSnapshotJson(assignments = emptyList())
    val cloudFile = CloudFile(id = "id", name = "app.tga1", fileExtension = ".tga1")
    val inputStream = ByteArrayInputStream(byteArrayOf())

    coEvery { createCloudFileUseCase(DataType.TagAssignments, emptySnapshot) } returns cloudFile
    every { getAllowedCloudApisUseCase() } returns listOf(mockCloudFileApi)
    coEvery { syncDataConverter.create(emptySnapshot) } returns inputStream
    coEvery { mockCloudFileApi.uploadFile(any(), any()) } returns cloudFile

    uploadTagAssignmentsUseCase()

    coVerify(exactly = 1) { mockCloudFileApi.uploadFile(inputStream, cloudFile) }
  }
}
