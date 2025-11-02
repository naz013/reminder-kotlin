package com.github.naz013.sync.usecase.download

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.logging.Logger
import com.github.naz013.sync.DataType
import com.github.naz013.sync.SyncDataConverter
import com.github.naz013.sync.SyncResult

internal class DownloadCloudFileUseCase(
  private val syncDataConverter: SyncDataConverter,
  private val getClassByDataTypeUseCase: GetClassByDataTypeUseCase,
  private val postProcessDownloadedFileUseCase: PostProcessDownloadedFileUseCase
) {

  suspend operator fun invoke(
    cloudFileApi: CloudFileApi,
    cloudFile: CloudFile,
    dataType: DataType
  ): Any {
    val stream = cloudFileApi.downloadFile(cloudFile) ?: run {
      Logger.e(TAG, "Failed to download file from cloud for dataType: $dataType, name: ${cloudFile.name}")
      return SyncResult.Skipped
    }
    val data = syncDataConverter.parse(stream, getClassByDataTypeUseCase(dataType))
    Logger.d(TAG, "Downloaded file: ${cloudFile.name} for dataType: $dataType, starting post-processing.")
    return postProcessDownloadedFileUseCase(
      data = data,
      cloudFileApi = cloudFileApi
    )
  }

  companion object {
    private const val TAG = "DownloadCloudFileUseCase"
  }
}
