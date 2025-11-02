package com.github.naz013.sync

import com.github.naz013.logging.Logger
import com.github.naz013.sync.cache.SyncApiSessionCache
import com.github.naz013.sync.local.DataTypeRepositoryCallerFactory
import com.github.naz013.sync.performance.measure
import com.github.naz013.sync.usecase.GetAllowedDataTypesUseCase
import com.github.naz013.sync.usecase.HasAnyCloudApiUseCase
import com.github.naz013.sync.usecase.delete.DeleteDataTypeUseCase
import com.github.naz013.sync.usecase.delete.DeleteSingleUseCase
import com.github.naz013.sync.usecase.download.DownloadLegacyFilesUseCase
import com.github.naz013.sync.usecase.download.DownloadSingleUseCase
import com.github.naz013.sync.usecase.download.DownloadUseCase
import com.github.naz013.sync.usecase.upload.UploadDataTypeUseCase
import com.github.naz013.sync.usecase.upload.UploadSingleUseCase

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
  private val syncApiSessionCache: SyncApiSessionCache,
  private val downloadLegacyFilesUseCase: DownloadLegacyFilesUseCase
) : SyncApi {

  override suspend fun sync(forceUpload: Boolean): SyncResult = measure("Total sync") {
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for sync.")
      return@measure SyncResult.Skipped
    }
    val allowedDataTypes = getAllowedDataTypesUseCase()
    if (allowedDataTypes.isEmpty()) {
      Logger.i(TAG, "No allowed data types for sync.")
      return@measure SyncResult.Skipped
    }
    val results = mutableListOf<SyncResult>()
    for (dataType in allowedDataTypes) {
      results.add(syncInternal(dataType, forceUpload))
    }
    // Download legacy files after all other sync is done
    downloadLegacyFilesUseCase()
    syncApiSessionCache.clearCache()
    SyncResult.Success(
      downloaded = results.filterIsInstance<SyncResult.Success>().flatMap { it.downloaded },
      success = results.all { it is SyncResult.Success }
    )
  }

  override suspend fun sync(dataType: DataType, forceUpload: Boolean): SyncResult = measure("Sync for data type: $dataType") {
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for sync.")
      return@measure SyncResult.Skipped
    }
    syncInternal(dataType, forceUpload).also {
      downloadLegacyFilesUseCase()
      syncApiSessionCache.clearCache()
    }
  }

  private suspend fun syncInternal(dataType: DataType, forceUpload: Boolean): SyncResult {
    Logger.i(TAG, "Syncing items for data type: $dataType")
    if (forceUpload) {
      forceUploadInternal(dataType)
    } else {
      uploadInternal(dataType)
    }
    return downloadUseCase(dataType)
  }

  override suspend fun sync(dataType: DataType, id: String, forceUpload: Boolean): SyncResult = measure("Sync single item. dataType: $dataType, id: $id") {
    require(id.isNotBlank()) { "Id cannot be blank" }
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for sync.")
      return@measure SyncResult.Skipped
    }
    if (dataType == DataType.Settings) {
      throw IllegalArgumentException("Cannot sync single settings item.")
    }
    Logger.i(TAG, "Syncing single item. dataType: $dataType, id: $id")
    uploadSingleUseCase(dataType, id)
    downloadSingleUseCase(dataType, id).also {
      syncApiSessionCache.clearCache()
    }
  }

  override suspend fun upload() = measure("Total upload") {
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for upload.")
      return@measure
    }
    val allowedDataTypes = getAllowedDataTypesUseCase()
    for (dataType in allowedDataTypes) {
      Logger.i(TAG, "Uploading items for data type: $dataType")
      uploadInternal(dataType)
    }
    syncApiSessionCache.clearCache()
  }

  override suspend fun upload(dataType: DataType) = measure("Upload for data type: $dataType") {
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for upload.")
      return@measure
    }
    uploadInternal(dataType)
    syncApiSessionCache.clearCache()
  }

  private suspend fun uploadInternal(dataType: DataType) {
    uploadDataTypeUseCase(dataType)
  }

  override suspend fun upload(dataType: DataType, id: String) = measure("Upload single item. dataType: $dataType, id: $id") {
    require(id.isNotBlank()) { "Id cannot be blank" }
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for upload.")
      return@measure
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

  override suspend fun delete() = measure("Total delete") {
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for delete.")
      return@measure
    }
    val allowedDataTypes = getAllowedDataTypesUseCase()
    for (dataType in allowedDataTypes) {
      Logger.i(TAG, "Deleting data type from cloud: $dataType")
      deleteDataTypeUseCase(dataType)
    }
    syncApiSessionCache.clearCache()
  }

  override suspend fun delete(dataType: DataType) = measure("Delete for data type: $dataType") {
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for delete.")
      return@measure
    }
    Logger.i(TAG, "Deleting data type from cloud: $dataType")
    deleteDataTypeUseCase(dataType)
    syncApiSessionCache.clearCache()
  }

  override suspend fun delete(dataType: DataType, id: String) = measure("Delete single item. dataType: $dataType, id: $id") {
    require(id.isNotBlank()) { "Id cannot be blank" }
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for delete.")
      return@measure
    }
    Logger.i(TAG, "Deleting single item from cloud. dataType: $dataType, id: $id")
    deleteSingleUseCase(dataType, id)
    syncApiSessionCache.clearCache()
  }

  override suspend fun delete(dataType: DataType, ids: List<String>) = measure("Delete multiple items. dataType: $dataType, ids count: ${ids.size}") {
    require(ids.isNotEmpty()) { "Ids list cannot be empty" }
    require(ids.all { it.isNotBlank() }) { "All ids must be non-blank" }
    if (!hasAnyCloudApiUseCase()) {
      Logger.i(TAG, "No cloud API configured for delete.")
      return@measure
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
