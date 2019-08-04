package com.elementary.tasks.core.cloud.converters

import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.storages.FileIndex
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.google.gson.Gson
import java.io.ByteArrayOutputStream

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
            val stream = ByteArrayOutputStream()
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
            null
        }
    }

    override fun convert(encrypted: String): Reminder? {
        if (encrypted.isEmpty()) return null
        return try {
            val json = MemoryUtil.decryptToJson(encrypted) ?: return null
            return Gson().fromJson(json, Reminder::class.java)
        } catch (e: Exception) {
            null
        }
    }
}