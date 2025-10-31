package com.github.naz013.sync

import com.github.naz013.logging.Logger
import com.github.naz013.sync.cache.SyncApiSessionCache
import com.github.naz013.sync.local.DataTypeRepositoryCallerFactory
import com.github.naz013.sync.usecase.DeleteDataTypeUseCase
import com.github.naz013.sync.usecase.DeleteSingleUseCase
import com.github.naz013.sync.usecase.DownloadSingleUseCase
import com.github.naz013.sync.usecase.DownloadUseCase
import com.github.naz013.sync.usecase.GetAllowedDataTypesUseCase
import com.github.naz013.sync.usecase.HasAnyCloudApiUseCase
import com.github.naz013.sync.usecase.UploadDataTypeUseCase
import com.github.naz013.sync.usecase.UploadSingleUseCase

internal class SyncApiImpl(
  private val dataTypeRepositoryCallerFactory: DataTypeRepositoryCallerFactory,
  private val getAllowedDataTypesUseCase: GetAllowedDataTypesUseCase,
  private val uploadSingleUseCase: UploadSingleUseCase,
  private val downloadSingleUseCase: DownloadSingleUseCase,
  private val downloadUseCase: DownloadUseCase,
  private val deleteSingleUseCase: DeleteSingleUseCase,
  private val deleteDataTypeUseCase: DeleteDataTypeUseCase,
  private val uploadDataTypeUseCase: UploadDataTypeUseCase,
  private val hasAnyCloudApiUseCase: HasAnyCloudApiUseCase,
  private val syncApiSessionCache: SyncApiSessionCache
) : SyncApi {

  override suspend fun sync(forceUpload: Boolean): SyncResult {
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for sync.")
      return SyncResult.Skipped
    }
    val allowedDataTypes = getAllowedDataTypesUseCase()
    if (allowedDataTypes.isEmpty()) {
      Logger.i(TAG, "No allowed data types for sync.")
      return SyncResult.Skipped
    }
    val results = mutableListOf<SyncResult>()
    for (dataType in allowedDataTypes) {
      results.add(syncInternal(dataType, forceUpload))
    }
    syncApiSessionCache.clearCache()
    return SyncResult.Success(
      downloaded = results.filterIsInstance<SyncResult.Success>().flatMap { it.downloaded },
      success = results.all { it is SyncResult.Success }
    )
  }

  override suspend fun sync(dataType: DataType, forceUpload: Boolean): SyncResult {
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for sync.")
      return SyncResult.Skipped
    }
    return syncInternal(dataType, forceUpload).also {
      syncApiSessionCache.clearCache()
    }
  }

  private suspend fun syncInternal(dataType: DataType, forceUpload: Boolean): SyncResult {
    Logger.i(TAG, "Syncing items for data type: $dataType")
    if (forceUpload) {
      forceUpload(dataType)
    } else {
      upload(dataType)
    }
    return downloadUseCase(dataType)
  }

  override suspend fun sync(dataType: DataType, id: String, forceUpload: Boolean): SyncResult {
    require(id.isNotBlank()) { "Id cannot be blank" }
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for sync.")
      return SyncResult.Skipped
    }
    if (dataType == DataType.Settings) {
      throw IllegalArgumentException("Cannot sync single settings item.")
    }
    Logger.i(TAG, "Syncing single item. dataType: $dataType, id: $id")
    uploadSingleUseCase(dataType, id)
    return downloadSingleUseCase(dataType, id).also {
      syncApiSessionCache.clearCache()
    }
  }

  override suspend fun upload() {
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for upload.")
      return
    }
    val allowedDataTypes = getAllowedDataTypesUseCase()
    for (dataType in allowedDataTypes) {
      Logger.i(TAG, "Uploading items for data type: $dataType")
      uploadDataTypeUseCase(dataType)
    }
    syncApiSessionCache.clearCache()
  }

  override suspend fun upload(dataType: DataType) {
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for upload.")
      return
    }
    uploadDataTypeUseCase(dataType)
    syncApiSessionCache.clearCache()
  }

  override suspend fun upload(dataType: DataType, id: String) {
    require(id.isNotBlank()) { "Id cannot be blank" }
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for upload.")
      return
    }
    if (dataType == DataType.Settings) {
      throw IllegalArgumentException("Cannot upload single settings item.")
    }
    Logger.i(TAG, "Uploading single item. dataType: $dataType, id: $id")
    uploadSingleUseCase(dataType, id)
    syncApiSessionCache.clearCache()
  }

  override suspend fun forceUpload() {
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for upload.")
      return
    }
    val allowedDataTypes = getAllowedDataTypesUseCase()
    for (dataType in allowedDataTypes) {
      forceUploadInternal(dataType)
    }
    syncApiSessionCache.clearCache()
  }

  override suspend fun forceUpload(dataType: DataType) {
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for upload.")
      return
    }
    forceUploadInternal(dataType)
    syncApiSessionCache.clearCache()
  }

  private suspend fun forceUploadInternal(dataType: DataType) {
    Logger.i(TAG, "Force uploading items for data type: $dataType")
    val repositoryCaller = dataTypeRepositoryCallerFactory.getCaller(dataType)
    val ids = repositoryCaller.getAllIds()
    Logger.i(TAG, "Found ${ids.size} items to force upload for data type: $dataType")
    for (id in ids) {
      uploadSingleUseCase(dataType, id)
    }
  }

  override suspend fun forceUpload(dataType: DataType, id: String) {
    require(id.isNotBlank()) { "Id cannot be blank" }
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for upload.")
      return
    }
    Logger.i(TAG, "Force uploading single item. dataType: $dataType, id: $id")
    uploadSingleUseCase(dataType, id)
    syncApiSessionCache.clearCache()
  }

  override suspend fun delete() {
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for delete.")
      return
    }
    val allowedDataTypes = getAllowedDataTypesUseCase()
    for (dataType in allowedDataTypes) {
      Logger.i(TAG, "Deleting data type from cloud: $dataType")
      deleteDataTypeUseCase(dataType)
    }
    syncApiSessionCache.clearCache()
  }

  override suspend fun delete(dataType: DataType) {
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for delete.")
      return
    }
    Logger.i(TAG, "Deleting data type from cloud: $dataType")
    deleteDataTypeUseCase(dataType)
    syncApiSessionCache.clearCache()
  }

  override suspend fun delete(dataType: DataType, id: String) {
    require(id.isNotBlank()) { "Id cannot be blank" }
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for delete.")
      return
    }
    Logger.i(TAG, "Deleting single item from cloud. dataType: $dataType, id: $id")
    deleteSingleUseCase(dataType, id)
    syncApiSessionCache.clearCache()
  }

  override suspend fun delete(dataType: DataType, ids: List<String>) {
    require(ids.isNotEmpty()) { "Ids list cannot be empty" }
    require(ids.all { it.isNotBlank() }) { "All ids must be non-blank" }
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for delete.")
      return
    }
    Logger.i(TAG, "Deleting multiple items from cloud. dataType: $dataType, ids count: ${ids.size}")
    for (id in ids) {
      deleteSingleUseCase(dataType, id)
    }
    syncApiSessionCache.clearCache()
  }

  companion object {
    private const val TAG = "SyncApiImpl"
  }
}
