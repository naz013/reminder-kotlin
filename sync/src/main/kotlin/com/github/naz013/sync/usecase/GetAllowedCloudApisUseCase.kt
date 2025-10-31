package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.sync.CloudApiProvider

class GetAllowedCloudApisUseCase(
  private val cloudApiProvider: CloudApiProvider
) {

  operator fun invoke(): List<CloudFileApi> {
    return cloudApiProvider.getAllowedCloudApis()
  }
}
