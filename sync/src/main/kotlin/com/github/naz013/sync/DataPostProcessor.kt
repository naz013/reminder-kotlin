package com.github.naz013.sync

import com.github.naz013.files.DataType

interface DataPostProcessor {
  suspend fun process(dataType: DataType, any: Any)
}
