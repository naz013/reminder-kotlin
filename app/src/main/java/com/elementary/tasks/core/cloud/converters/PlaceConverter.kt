package com.elementary.tasks.core.cloud.converters

import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.storages.FileIndex
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.utils.io.CopyByteArrayStream
import com.elementary.tasks.core.utils.io.MemoryUtil
import timber.log.Timber
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

  override fun convert(t: Place): FileIndex? {
    return try {
      val stream = CopyByteArrayStream()
      memoryUtil.toStream(t, stream)
      FileIndex().apply {
        this.stream = stream
        this.ext = FileConfig.FILE_NAME_PLACE
        this.id = t.id
        this.updatedAt = t.dateTime
        this.type = IndexTypes.TYPE_PLACE
        this.readyToBackup = true
      }
    } catch (e: Exception) {
      Timber.e(e)
      null
    }
  }

  override fun convert(stream: InputStream): Place? {
    return try {
      val place = MemoryUtil.fromStream(stream, Place::class.java)
      stream.close()
      return place
    } catch (e: Exception) {
      Timber.e(e)
      null
    }
  }
}