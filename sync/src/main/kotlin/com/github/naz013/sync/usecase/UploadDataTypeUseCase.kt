package com.github.naz013.sync.usecase

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.sync.DataType
import com.github.naz013.sync.local.DataTypeRepositoryCallerFactory
import com.github.naz013.sync.settings.UploadSettingsUseCase

internal class UploadDataTypeUseCase(
  private val uploadSingleUseCase: UploadSingleUseCase,
  private val uploadSettingsUseCase: UploadSettingsUseCase,
  private val dataTypeRepositoryCallerFactory: DataTypeRepositoryCallerFactory,
) {

  /**
   * Uploads all items of a specific data type that are waiting for upload or failed previously.
   *
   * For regular data types, uploads items with sync states: WaitingForUpload or FailedToUpload.
   * For Settings type, delegates to UploadSettingsUseCase.
   * Continues uploading remaining items even if some fail.
   *
   * @param dataType The type of data to upload
   * @throws Exception if all uploads fail (at least one success allows completion)
   */
  suspend operator fun invoke(dataType: DataType) {
    if (dataType == DataType.Settings) {
      Logger.i(TAG, "Uploading settings")
      uploadSettingsUseCase()
    } else {
      val repositoryCaller = dataTypeRepositoryCallerFactory.getCaller(dataType)
      val ids = repositoryCaller.getIdsByState(
        listOf(
          SyncState.WaitingForUpload,
          SyncState.FailedToUpload
        )
      )
      if (ids.isEmpty()) {
        Logger.i(TAG, "No items to upload for data type: $dataType")
        return
      }
      Logger.i(TAG, "Uploading ${ids.size} items for data type: $dataType")
      var successCount = 0
      var failureCount = 0
      for (id in ids) {
        try {
          uploadSingleUseCase(dataType, id)
          successCount++
        } catch (e: Exception) {
          failureCount++
          Logger.e(TAG, "Failed to upload item id: $id, continuing with remaining items", e)
        }
      }
      Logger.i(TAG, "Upload completed for $dataType: $successCount succeeded, $failureCount failed")
    }
  }

  companion object {
    private const val TAG = "UploadDataTypeUseCase"
  }
}
