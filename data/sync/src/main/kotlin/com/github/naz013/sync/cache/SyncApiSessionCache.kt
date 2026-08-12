package com.github.naz013.sync.cache

import com.github.naz013.cloudapi.CloudFileApi

internal class SyncApiSessionCache {

  private var cloudApis: List<CloudFileApi>? = null

  fun cache(cloudFileApis: List<CloudFileApi>) {
    this.cloudApis = cloudFileApis
  }

  fun getCached(): List<CloudFileApi>? {
    return cloudApis
  }

  fun clearCache() {
    this.cloudApis = null
  }
}
