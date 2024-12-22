package com.elementary.tasks.core.cloud.converters

import com.github.naz013.cloudapi.FileConfig
import com.github.naz013.domain.Place
import com.github.naz013.cloudapi.stream.CopyByteArrayStream
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.github.naz013.cloudapi.legacy.Convertible
import com.github.naz013.cloudapi.legacy.Metadata
import com.github.naz013.logging.Logger
import java.io.InputStream

class PlaceConverter(
  private val memoryUtil: MemoryUtil
) : Convertible<Place> {

  override fun metadata(t: Place): Metadata {
    return Metadata(
      t.id,
      t.id + FileConfig.FILE_NAME_PLACE,
      FileConfig.FILE_NAME_PLACE,
      t.dateTime,
      "Place Backup"
    )
  }

  override fun toOutputStream(t: Place): CopyByteArrayStream {
    val stream = CopyByteArrayStream()
    memoryUtil.toStream(t, stream)
    return stream
  }

  override fun convert(stream: InputStream): Place? {
    return try {
      val place = MemoryUtil.fromStream(stream, Place::class.java)
      stream.close()
      return place
    } catch (e: Exception) {
      Logger.e("PlaceConverter: convert error: $e")
      null
    }
  }
}
