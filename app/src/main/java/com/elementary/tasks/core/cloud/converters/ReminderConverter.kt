package com.elementary.tasks.core.cloud.converters

import com.github.naz013.cloudapi.FileConfig
import com.github.naz013.domain.Reminder
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.github.naz013.cloudapi.stream.CopyByteArrayStream
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.github.naz013.cloudapi.legacy.Convertible
import com.github.naz013.cloudapi.legacy.Metadata
import com.github.naz013.logging.Logger
import java.io.InputStream

class ReminderConverter(
  private val memoryUtil: MemoryUtil
) : Convertible<Reminder> {

  override fun metadata(t: Reminder): Metadata {
    return Metadata(
      t.uuId,
      t.uuId + FileConfig.FILE_NAME_REMINDER,
      FileConfig.FILE_NAME_REMINDER,
      t.updatedAt ?: "",
      "Reminder Backup"
    )
  }

  override fun toOutputStream(t: Reminder): CopyByteArrayStream {
    val stream = CopyByteArrayStream()
    memoryUtil.toStream(t, stream)
    return stream
  }

  override fun convert(stream: InputStream): Reminder? {
    return try {
      val reminder = MemoryUtil.fromStream(stream, Reminder::class.java)
      stream.close()
      return reminder?.takeIf { !isDeprecatedType(it.type) }
    } catch (e: Throwable) {
      Logger.e("ReminderConverter: convert error: $e")
      null
    }
  }

  private fun isDeprecatedType(type: Int): Boolean {
    val uiType = UiReminderType(type)
    return uiType.isBase(UiReminderType.Base.SKYPE) || uiType.isBase(UiReminderType.Base.PLACE)
  }
}
