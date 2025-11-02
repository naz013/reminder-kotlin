package com.github.naz013.sync.usecase.download

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.sync.DataPostProcessor
import com.github.naz013.sync.DataType
import com.github.naz013.sync.local.DataTypeRepositoryCallerFactory
import com.github.naz013.sync.usecase.FindAllFilesToDeleteUseCase
import com.github.naz013.sync.usecase.GetLocalUuIdUseCase

internal class DownloadLegacyFilesUseCase(
  private val getAllFilesToDeleteUseCase: FindAllFilesToDeleteUseCase,
  private val downloadCloudFileUseCase: DownloadCloudFileUseCase,
  private val dataTypeRepositoryCallerFactory: DataTypeRepositoryCallerFactory,
  private val getLocalUuIdUseCase: GetLocalUuIdUseCase,
  private val dataPostProcessor: DataPostProcessor,
) {

  suspend operator fun invoke() {
    Logger.i(TAG, "Starting download of legacy files.")
    DataType.entries.filter { it.isLegacy }.forEach { dataType ->
      val caller = dataTypeRepositoryCallerFactory.getCaller(dataType)
      val searchResult = getAllFilesToDeleteUseCase(dataType)
      if (searchResult != null) {
        for (source in searchResult.sources) {
          val cloudFileApi = source.source
          val filesToDelete = source.cloudFiles

          Logger.d(TAG, "Found ${filesToDelete.size} legacy files to download for dataType: $dataType from source: ${cloudFileApi.source}")
          for (cloudFile in filesToDelete) {
            val data = downloadCloudFileUseCase(
              cloudFileApi = cloudFileApi,
              cloudFile = cloudFile,
              dataType = dataType
            )

            val id = getLocalUuIdUseCase(data)

            // Check for conflicts before updating
            val existingData = caller.getById(id)
            if (existingData != null) {
              Logger.d(TAG, "Existing data found for id: $id, skipping update.")
              continue
            }

            caller.insertOrUpdate(data)
            dataPostProcessor.process(dataType, data)
            caller.updateSyncState(id, SyncState.Synced)
            Logger.i(TAG, "Downloaded and saved file: ${cloudFile.name} from source: ${cloudFileApi.source}")

            cloudFileApi.deleteFile(cloudFile.name)
          }
        }
      }
    }
  }

  companion object {
    private const val TAG = "DownloadLegacyFilesUseCase"
  }
}
