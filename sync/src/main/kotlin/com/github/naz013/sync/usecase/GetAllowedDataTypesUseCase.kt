package com.github.naz013.sync.usecase

import com.github.naz013.sync.DataType

internal class GetAllowedDataTypesUseCase {

  operator fun invoke(): List<DataType> {
    return DataType.entries.toList().filter { dataType -> !dataType.isLegacy }
  }
}
