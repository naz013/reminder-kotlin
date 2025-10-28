package com.github.naz013.sync

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.sync.local.DataTypeRepositoryCallerFactory
import com.github.naz013.sync.usecase.DeleteDataTypeUseCase
import com.github.naz013.sync.usecase.DeleteSingleUseCase
import com.github.naz013.sync.usecase.DownloadSingleUseCase
import com.github.naz013.sync.usecase.DownloadUseCase
import com.github.naz013.sync.usecase.GetAllowedDataTypesUseCase
import com.github.naz013.sync.usecase.UploadSingleUseCase

internal class SyncApiImpl(
  private val dataTypeRepositoryCallerFactory: DataTypeRepositoryCallerFactory,
  private val getAllowedDataTypesUseCase: GetAllowedDataTypesUseCase,
  private val uploadSingleUseCase: UploadSingleUseCase,
  private val syncSettings: SyncSettings,
  private val downloadSingleUseCase: DownloadSingleUseCase,
  private val downloadUseCase: DownloadUseCase,
  private val deleteSingleUseCase: DeleteSingleUseCase,
  private val deleteDataTypeUseCase: DeleteDataTypeUseCase,
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
      upload(dataType)
      downloadUseCase(dataType)
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

  override suspend fun upload(dataType: DataType) {
    if (syncSettings.isDataTypeEnabled(dataType)) {
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

  override suspend fun forceUpload() {
    val allowedDataTypes = getAllowedDataTypesUseCase()
    for (dataType in allowedDataTypes) {
      Logger.i(TAG, "Force uploading items for data type: $dataType")
      val repositoryCaller = dataTypeRepositoryCallerFactory.getCaller(dataType)
      val ids = repositoryCaller.getAllIds()
      for (id in ids) {
        uploadSingleUseCase(dataType, id)
      }
    }
  }

  override suspend fun forceUpload(dataType: DataType) {
    if (syncSettings.isDataTypeEnabled(dataType)) {
      Logger.i(TAG, "Force uploading items for data type: $dataType")
      val repositoryCaller = dataTypeRepositoryCallerFactory.getCaller(dataType)
      val ids = repositoryCaller.getAllIds()
      for (id in ids) {
        uploadSingleUseCase(dataType, id)
      }
    } else {
      Logger.i(TAG, "Data type is not enabled for upload: $dataType")
    }
  }

  override suspend fun forceUpload(dataType: DataType, id: String) {
    if (syncSettings.isDataTypeEnabled(dataType)) {
      Logger.i(TAG, "Force uploading single item. dataType: $dataType, id: $id")
      uploadSingleUseCase(dataType, id)
    } else {
      Logger.i(TAG, "Data type is not enabled for upload: $dataType")
    }
  }

  override suspend fun delete() {
    val allowedDataTypes = getAllowedDataTypesUseCase()
    for (dataType in allowedDataTypes) {
      Logger.i(TAG, "Deleting data type from cloud: $dataType")
      deleteDataTypeUseCase(dataType)
    }
  }

  override suspend fun delete(dataType: DataType) {
    if (syncSettings.isDataTypeEnabled(dataType)) {
      Logger.i(TAG, "Deleting data type from cloud: $dataType")
      deleteDataTypeUseCase(dataType)
    } else {
      Logger.i(TAG, "Data type is not enabled for delete: $dataType")
    }
  }

  override suspend fun delete(dataType: DataType, id: String) {
    if (syncSettings.isDataTypeEnabled(dataType)) {
      Logger.i(TAG, "Deleting single item from cloud. dataType: $dataType, id: $id")
      deleteSingleUseCase(dataType, id)
    } else {
      Logger.i(TAG, "Data type is not enabled for delete: $dataType")
    }
  }

  companion object {
    private const val TAG = "SyncApiImpl"
  }
}
