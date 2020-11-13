package com.elementary.tasks.core.cloud.converters

import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.storages.FileIndex
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.CopyByteArrayStream
import com.elementary.tasks.core.utils.MemoryUtil
import timber.log.Timber
import java.io.InputStream

class TemplateConverter : Convertible<SmsTemplate> {

  override fun metadata(t: SmsTemplate): Metadata {
    return Metadata(
      t.key,
      t.key + FileConfig.FILE_NAME_TEMPLATE,
      FileConfig.FILE_NAME_TEMPLATE,
      t.date,
      "Template Backup"
    )
  }

  override fun convert(t: SmsTemplate): FileIndex? {
    return try {
      val stream = CopyByteArrayStream()
      MemoryUtil.toStream(t, stream)
      FileIndex().apply {
        this.stream = stream
        this.ext = FileConfig.FILE_NAME_TEMPLATE
        this.id = t.key
        this.updatedAt = t.date
        this.type = IndexTypes.TYPE_TEMPLATE
        this.readyToBackup = true
      }
    } catch (e: Exception) {
      Timber.e(e)
      null
    }
  }

  override fun convert(stream: InputStream): SmsTemplate? {
    return try {
      val template = MemoryUtil.fromStream(stream, SmsTemplate::class.java)
      stream.close()
      return template
    } catch (e: Exception) {
      Timber.e(e)
      null
    }
  }
}