package com.elementary.tasks.core.cloud.converters

import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.storages.FileIndex
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.io.CopyByteArrayStream
import com.elementary.tasks.core.utils.io.MemoryUtil
import timber.log.Timber
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

  override fun convert(t: ReminderGroup): FileIndex? {
    return try {
      val stream = CopyByteArrayStream()
      memoryUtil.toStream(t, stream)
      FileIndex().apply {
        this.stream = stream
        this.ext = FileConfig.FILE_NAME_GROUP
        this.id = t.groupUuId
        this.updatedAt = t.groupDateTime
        this.type = IndexTypes.TYPE_GROUP
        this.readyToBackup = true
      }
    } catch (e: Exception) {
      Timber.e(e)
      null
    }
  }

  override fun convert(stream: InputStream): ReminderGroup? {
    return try {
      val group = MemoryUtil.fromStream(stream, ReminderGroup::class.java)
      stream.close()
      return group
    } catch (e: Exception) {
      Timber.e(e)
      null
    }
  }
}