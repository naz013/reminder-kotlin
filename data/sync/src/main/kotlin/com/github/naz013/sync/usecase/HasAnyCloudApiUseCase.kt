package com.github.naz013.sync.usecase

internal class HasAnyCloudApiUseCase(
  private val getAllowedCloudApisUseCase: GetAllowedCloudApisUseCase
) {

  operator fun invoke(): Boolean {
    return getAllowedCloudApisUseCase().isNotEmpty()
  }
}
