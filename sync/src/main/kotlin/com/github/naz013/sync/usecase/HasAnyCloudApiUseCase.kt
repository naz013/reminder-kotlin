package com.github.naz013.sync.usecase

import com.github.naz013.sync.CloudApiProvider

class HasAnyCloudApiUseCase(
  private val cloudApiProvider: CloudApiProvider
) {

  operator fun invoke(): Boolean {
    return cloudApiProvider.getAllowedCloudApis().isNotEmpty()
  }
}
