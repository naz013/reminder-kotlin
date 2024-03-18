package com.elementary.tasks.core.cloud.converters

import com.elementary.tasks.core.utils.io.CopyByteArrayStream
import java.io.InputStream

interface Convertible<T> {
  fun toOutputStream(t: T): CopyByteArrayStream?
  fun convert(stream: InputStream): T?
  fun metadata(t: T): Metadata
}
