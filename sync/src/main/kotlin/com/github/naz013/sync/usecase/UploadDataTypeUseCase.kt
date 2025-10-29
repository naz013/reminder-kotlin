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
      Logger.i(TAG, "Uploading items for data type: $dataType")
      for (id in ids) {
        uploadSingleUseCase(dataType, id)
      }
    }
  }

  companion object {
    private const val TAG = "UploadDataTypeUseCase"
  }
}
