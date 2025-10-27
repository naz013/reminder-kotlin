package com.github.naz013.sync

import java.io.InputStream

interface SyncDataConverter {
  @Throws(Exception::class)
  suspend fun create(any: Any): InputStream

  @Throws(Exception::class)
  suspend fun <T> parse(stream: InputStream, clazz: Class<T>): T
}
