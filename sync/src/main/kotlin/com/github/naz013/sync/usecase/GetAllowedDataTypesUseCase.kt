package com.github.naz013.sync.usecase

import com.github.naz013.sync.DataType
import com.github.naz013.sync.SyncSettings

internal class GetAllowedDataTypesUseCase(
  private val syncSettings: SyncSettings
) {

  suspend operator fun invoke(): List<DataType> {
    return DataType.entries.toList().filter {
      syncSettings.isDataTypeEnabled(it)
    }
  }
}
