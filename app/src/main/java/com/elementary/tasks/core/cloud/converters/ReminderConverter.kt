package com.elementary.tasks.core.cloud.converters

import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.storages.FileIndex
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.CopyByteArrayStream
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.TimeUtil
import timber.log.Timber
import java.io.InputStream

class ReminderConverter : Convertible<Reminder> {

  override fun metadata(t: Reminder): Metadata {
    return Metadata(
      t.uuId,
      t.uuId + FileConfig.FILE_NAME_REMINDER,
      FileConfig.FILE_NAME_REMINDER,
      t.updatedAt ?: "",
      "Reminder Backup"
    )
  }

  override fun convert(t: Reminder): FileIndex? {
    return try {
      val stream = CopyByteArrayStream()
      MemoryUtil.toStream(t, stream)
      FileIndex().apply {
        this.stream = stream
        this.attachment = t.attachmentFile
        this.ext = FileConfig.FILE_NAME_REMINDER
        this.id = t.uuId
        this.melody = t.melodyPath
        this.updatedAt = t.updatedAt ?: TimeUtil.gmtDateTime
        this.type = IndexTypes.TYPE_REMINDER
        this.readyToBackup = true
      }
    } catch (e: Exception) {
      Timber.e(e)
      null
    }
  }

  override fun convert(stream: InputStream): Reminder? {
    return try {
      val reminder = MemoryUtil.fromStream(stream, Reminder::class.java)
      stream.close()
      return reminder
    } catch (e: Exception) {
      Timber.e(e)
      null
    }
  }
}