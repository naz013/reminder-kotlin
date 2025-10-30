package com.github.naz013.sync

interface DataPostProcessor {
  suspend fun process(dataType: DataType, any: Any)
}
