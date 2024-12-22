package com.elementary.tasks.core.cloud.converters

import com.github.naz013.cloudapi.FileConfig
import com.github.naz013.domain.Birthday
import com.github.naz013.cloudapi.stream.CopyByteArrayStream
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.github.naz013.cloudapi.legacy.Convertible
import com.github.naz013.cloudapi.legacy.Metadata
import com.github.naz013.logging.Logger
import java.io.InputStream

class BirthdayConverter(
  private val memoryUtil: MemoryUtil
) : Convertible<Birthday> {

  override fun metadata(t: Birthday): Metadata {
    return Metadata(
      t.uuId,
      t.uuId + FileConfig.FILE_NAME_BIRTHDAY,
      FileConfig.FILE_NAME_BIRTHDAY,
      t.updatedAt ?: "",
      "Birthday Backup"
    )
  }

  override fun toOutputStream(t: Birthday): CopyByteArrayStream {
    val stream = CopyByteArrayStream()
    memoryUtil.toStream(t, stream)
    return stream
  }

  override fun convert(stream: InputStream): Birthday? {
    return try {
      val birthday = MemoryUtil.fromStream(stream, Birthday::class.java)
      stream.close()
      return birthday
    } catch (e: Exception) {
      Logger.e("BirthdayConverter: convert error: $e")
      null
    }
  }
}
