package com.github.naz013.sync

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.sync.local.DataTypeRepositoryCallerFactory
import com.github.naz013.sync.usecase.DownloadSingleUseCase
import com.github.naz013.sync.usecase.GetAllowedDataTypesUseCase
import com.github.naz013.sync.usecase.UploadSingleUseCase

internal class SyncApiImpl(
  private val dataTypeRepositoryCallerFactory: DataTypeRepositoryCallerFactory,
  private val getAllowedDataTypesUseCase: GetAllowedDataTypesUseCase,
  private val uploadSingleUseCase: UploadSingleUseCase,
  private val syncSettings: SyncSettings,
  private val downloadSingleUseCase: DownloadSingleUseCase
) : SyncApi {

  override suspend fun sync() {
    val allowedDataTypes = getAllowedDataTypesUseCase()
    for (dataType in allowedDataTypes) {
      sync(dataType)
    }
  }

  override suspend fun sync(dataType: DataType) {
    if (syncSettings.isDataTypeEnabled(dataType)) {
      Logger.i(TAG, "Syncing items for data type: $dataType")
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
      upload(dataType, ids)
      ids.forEach { downloadSingleUseCase(dataType, it) }
    }
  }

  override suspend fun sync(dataType: DataType, id: String) {
    if (syncSettings.isDataTypeEnabled(dataType)) {
      Logger.i(TAG, "Syncing single item. dataType: $dataType, id: $id")
      uploadSingleUseCase(dataType, id)
      downloadSingleUseCase(dataType, id)
    } else {
      Logger.i(TAG, "Data type is not enabled for sync: $dataType")
    }
  }

  override suspend fun upload() {
    val allowedDataTypes = getAllowedDataTypesUseCase()
    for (dataType in allowedDataTypes) {
      Logger.i(TAG, "Uploading items for data type: $dataType")
      val repositoryCaller = dataTypeRepositoryCallerFactory.getCaller(dataType)
      val ids = repositoryCaller.getIdsByState(
        listOf(
          SyncState.WaitingForUpload,
          SyncState.FailedToUpload
        )
      )
      if (ids.isEmpty()) {
        Logger.i(TAG, "No items to upload for data type: $dataType")
        continue
      }
      for (id in ids) {
        uploadSingleUseCase(dataType, id)
      }
    }
  }

  override suspend fun upload(dataType: DataType, ids: List<String>) {
    if (syncSettings.isDataTypeEnabled(dataType)) {
      Logger.i(TAG, "Uploading multiple items. dataType: $dataType, ids: $ids")
      for (id in ids) {
        uploadSingleUseCase(dataType, id)
      }
    } else {
      Logger.i(TAG, "Data type is not enabled for upload: $dataType")
    }
  }

  override suspend fun upload(dataType: DataType, id: String) {
    if (syncSettings.isDataTypeEnabled(dataType)) {
      Logger.i(TAG, "Uploading single item. dataType: $dataType, id: $id")
      uploadSingleUseCase(dataType, id)
    } else {
      Logger.i(TAG, "Data type is not enabled for upload: $dataType")
    }
  }

  companion object {
    private const val TAG = "SyncApiImpl"
  }
}
