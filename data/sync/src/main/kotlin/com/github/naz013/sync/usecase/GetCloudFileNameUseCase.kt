package com.github.naz013.sync.usecase

import com.github.naz013.files.DataType

internal class GetCloudFileNameUseCase {
  operator fun invoke(dataType: DataType, id: String): String {
    return id + dataType.fileExtension
  }
}
