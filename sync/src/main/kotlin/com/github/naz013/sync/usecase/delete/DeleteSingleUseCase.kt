package com.github.naz013.sync.usecase.delete

import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.DataType
import com.github.naz013.sync.usecase.GetAllowedCloudApisUseCase
import com.github.naz013.sync.usecase.GetCloudFileNameUseCase

internal class DeleteSingleUseCase(
  private val remoteFileMetadataRepository: RemoteFileMetadataRepository,
  private val getCloudFileNameUseCase: GetCloudFileNameUseCase,
  private val getAllowedCloudApisUseCase: GetAllowedCloudApisUseCase
) {
  /**
   * Deletes a single item from all configured cloud sources.
   *
   * @param dataType The type of data to delete
   * @param id The unique identifier of the item
   * @throws IllegalArgumentException if the id is blank
   * @throws Exception if deletion fails
   */
  suspend operator fun invoke(dataType: DataType, id: String) {
    require(id.isNotBlank()) { "Id cannot be blank" }

    if (dataType == DataType.Settings) {
      // Settings are not deleted from cloud.
      return
    }
    val fileName = getCloudFileNameUseCase(dataType, id)
    getAllowedCloudApisUseCase().forEach { cloudFileApi ->
      cloudFileApi.deleteFile(fileName)
    }
    remoteFileMetadataRepository.deleteByLocalUuId(id)
  }
}
