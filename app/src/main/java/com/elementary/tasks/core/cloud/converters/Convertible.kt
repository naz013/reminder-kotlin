package com.elementary.tasks.core.cloud.converters

import com.elementary.tasks.core.cloud.storages.FileIndex
import java.io.InputStream

interface Convertible<T> {
  fun convert(t: T): FileIndex?
  fun convert(stream: InputStream): T?
  fun metadata(t: T): Metadata
}