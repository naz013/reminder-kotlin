package com.github.naz013.sync

import com.github.naz013.cloudapi.CloudFileApi

interface CloudApiProvider {
  fun getAllowedCloudApis(): List<CloudFileApi>
}
