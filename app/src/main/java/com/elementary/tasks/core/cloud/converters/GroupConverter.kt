package com.elementary.tasks.core.cloud.converters

import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.storages.FileIndex
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.MemoryUtil
import com.google.gson.Gson

class GroupConverter : Convertible<ReminderGroup> {

    override fun metadata(t: ReminderGroup): Metadata {
        return Metadata(
                t.groupUuId + FileConfig.FILE_NAME_GROUP,
                FileConfig.FILE_NAME_GROUP,
                t.groupDateTime,
                "Group Backup"
        )
    }

    override fun convert(t: ReminderGroup): FileIndex? {
        return try {
            val json = Gson().toJson(t)
            val encrypted = MemoryUtil.encryptJson(json)
            FileIndex().apply {
                this.json = encrypted
                this.ext = FileConfig.FILE_NAME_GROUP
                this.id = t.groupUuId
                this.updatedAt = t.groupDateTime
                this.type = IndexTypes.TYPE_GROUP
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun convert(encrypted: String): ReminderGroup? {
        if (encrypted.isEmpty()) return null
        return try {
            val json = MemoryUtil.decryptToJson(encrypted) ?: return null
            return Gson().fromJson(json, ReminderGroup::class.java)
        } catch (e: Exception) {
            null
        }
    }
}