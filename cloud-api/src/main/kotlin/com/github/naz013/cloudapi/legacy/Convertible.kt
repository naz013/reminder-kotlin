package com.github.naz013.cloudapi.legacy

import com.github.naz013.cloudapi.stream.CopyByteArrayStream
import java.io.InputStream

interface Convertible<T> {
  fun toOutputStream(t: T): CopyByteArrayStream?
  fun convert(stream: InputStream): T?
  fun metadata(t: T): Metadata
}
