package com.elementary.tasks.core.cloud.converters

import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.storages.FileIndex
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.MemoryUtil
import com.google.gson.Gson

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
            val json = Gson().toJson(t)
            val encrypted = MemoryUtil.encryptJson(json)
            FileIndex().apply {
                this.json = encrypted
                this.ext = FileConfig.FILE_NAME_TEMPLATE
                this.id = t.key
                this.updatedAt = t.date
                this.type = IndexTypes.TYPE_TEMPLATE
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun convert(encrypted: String): SmsTemplate? {
        if (encrypted.isEmpty()) return null
        return try {
            val json = MemoryUtil.decryptToJson(encrypted) ?: return null
            return Gson().fromJson(json, SmsTemplate::class.java)
        } catch (e: Exception) {
            null
        }
    }
}