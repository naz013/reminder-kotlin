package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.logging.Logger
import com.github.naz013.sync.CloudApiProvider
import com.github.naz013.sync.cache.SyncApiSessionCache

internal class GetAllowedCloudApisUseCase(
  private val cloudApiProvider: CloudApiProvider,
  private val syncApiSessionCache: SyncApiSessionCache
) {

  operator fun invoke(): List<CloudFileApi> {
    return syncApiSessionCache.getCached()?.also {
      Logger.d(TAG, "Using cached cloud APIs: ${it.size}")
    } ?: cloudApiProvider.getAllowedCloudApis().also {
      syncApiSessionCache.cache(it)
      Logger.d(TAG, "Fetched and cached cloud APIs: ${it.size}")
    }
  }

  companion object {
    private const val TAG = "GetAllowedCloudApisUseCase"
  }
}
