package com.github.naz013.sync

import com.github.naz013.files.DataType
import java.io.InputStream

interface SyncDataConverter {
  @Throws(Exception::class)
  suspend fun create(any: Any): InputStream

  @Throws(Exception::class)
  suspend fun parse(stream: InputStream, dataType: DataType): Any
}
