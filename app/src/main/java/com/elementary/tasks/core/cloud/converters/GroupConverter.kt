package com.elementary.tasks.core.cloud.converters

import com.github.naz013.cloudapi.FileConfig
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.cloudapi.stream.CopyByteArrayStream
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.github.naz013.cloudapi.legacy.Convertible
import com.github.naz013.cloudapi.legacy.Metadata
import com.github.naz013.logging.Logger
import java.io.InputStream

class GroupConverter(
  private val memoryUtil: MemoryUtil
) : Convertible<ReminderGroup> {

  override fun metadata(t: ReminderGroup): Metadata {
    return Metadata(
      t.groupUuId,
      t.groupUuId + FileConfig.FILE_NAME_GROUP,
      FileConfig.FILE_NAME_GROUP,
      t.groupDateTime,
      "Group Backup"
    )
  }

  override fun toOutputStream(t: ReminderGroup): CopyByteArrayStream {
    val stream = CopyByteArrayStream()
    memoryUtil.toStream(t, stream)
    return stream
  }

  override fun convert(stream: InputStream): ReminderGroup? {
    return try {
      val group = MemoryUtil.fromStream(stream, ReminderGroup::class.java)
      stream.close()
      return group
    } catch (e: Exception) {
      Logger.e("GroupConverter: convert error: $e")
      null
    }
  }
}
