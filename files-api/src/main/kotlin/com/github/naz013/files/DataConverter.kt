package com.github.naz013.files

import java.io.InputStream
import java.io.OutputStream

interface DataConverter {
  suspend fun toOutputStream(any: Any, outputStream: OutputStream)
  suspend fun toInputStream(any: Any): InputStream
  suspend fun toData(stream: InputStream): Any
}
