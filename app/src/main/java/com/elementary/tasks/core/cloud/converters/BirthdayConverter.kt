package com.elementary.tasks.core.cloud.converters

import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.storages.FileIndex
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.CopyByteArrayStream
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.google.gson.Gson

class BirthdayConverter : Convertible<Birthday> {

    override fun metadata(t: Birthday): Metadata {
        return Metadata(
                t.uuId,
                t.uuId + FileConfig.FILE_NAME_BIRTHDAY,
                FileConfig.FILE_NAME_BIRTHDAY,
                t.updatedAt ?: "",
                "Birthday Backup"
        )
    }

    override fun convert(t: Birthday): FileIndex? {
        return try {
            val stream = CopyByteArrayStream()
            MemoryUtil.toStream(t, stream)
            FileIndex().apply {
                this.stream = stream
                this.ext = FileConfig.FILE_NAME_BIRTHDAY
                this.id = t.uuId
                this.updatedAt = t.updatedAt ?: TimeUtil.gmtDateTime
                this.type = IndexTypes.TYPE_BIRTHDAY
                this.readyToBackup = true
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun convert(encrypted: String): Birthday? {
        if (encrypted.isEmpty()) return null
        return try {
            val json = MemoryUtil.decryptToJson(encrypted) ?: return null
            return Gson().fromJson(json, Birthday::class.java)
        } catch (e: Exception) {
            null
        }
    }
}