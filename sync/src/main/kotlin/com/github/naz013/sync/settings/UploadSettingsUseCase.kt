package com.github.naz013.sync.settings

import com.github.naz013.logging.Logger
import com.github.naz013.sync.CloudApiProvider
import com.github.naz013.sync.DataType
import com.github.naz013.sync.SyncDataConverter
import com.github.naz013.sync.SyncSettings
import com.github.naz013.sync.usecase.CreateCloudFileUseCase

internal class UploadSettingsUseCase(
  private val createCloudFileUseCase: CreateCloudFileUseCase,
  private val syncSettings: SyncSettings,
  private val cloudApiProvider: CloudApiProvider,
  private val syncDataConverter: SyncDataConverter,
) {

  suspend operator fun invoke() {
    val settingsModel = syncSettings.getSettings()
    val cloudFile = createCloudFileUseCase(DataType.Settings, settingsModel)
    cloudApiProvider.getAllowedCloudApis().forEach { cloudFileApi ->
      val stream = syncDataConverter.create(settingsModel)
      cloudFileApi.uploadFile(stream, cloudFile)
    }
    Logger.i(TAG, "Settings uploaded successfully.")
  }

  companion object {
    private const val TAG = "UploadSettingsUseCase"
  }
}
