package com.github.naz013.sync.settings

import com.github.naz013.files.DataType
import com.github.naz013.files.model.TagAssignmentRowJson
import com.github.naz013.files.model.TagAssignmentsSnapshotJson
import com.github.naz013.logging.Logger
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.sync.SyncDataConverter
import com.github.naz013.sync.usecase.CreateCloudFileUseCase
import com.github.naz013.sync.usecase.GetAllowedCloudApisUseCase

internal class UploadTagAssignmentsUseCase(
  private val createCloudFileUseCase: CreateCloudFileUseCase,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val getAllowedCloudApisUseCase: GetAllowedCloudApisUseCase,
  private val syncDataConverter: SyncDataConverter,
) {

  suspend operator fun invoke() {
    val snapshot = TagAssignmentsSnapshotJson(
      assignments = tagAssignmentRepository.getAll().map {
        TagAssignmentRowJson(tagId = it.tagId, itemId = it.itemId, itemType = it.itemType.name)
      }
    )
    val cloudFile = createCloudFileUseCase(DataType.TagAssignments, snapshot)
    getAllowedCloudApisUseCase().forEach { cloudFileApi ->
      val stream = syncDataConverter.create(snapshot)
      cloudFileApi.uploadFile(stream, cloudFile)
    }
    Logger.i(TAG, "Tag assignments uploaded successfully.")
  }

  companion object {
    private const val TAG = "UploadTagAssignmentsUseCase"
  }
}
