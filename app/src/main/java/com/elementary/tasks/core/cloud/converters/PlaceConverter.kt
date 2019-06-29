package com.elementary.tasks.core.cloud.converters

import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.storages.FileIndex
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.utils.MemoryUtil
import com.google.gson.Gson

class PlaceConverter : Convertible<Place> {

    override fun metadata(t: Place): Metadata {
        return Metadata(
                t.id,
                t.id + FileConfig.FILE_NAME_PLACE,
                FileConfig.FILE_NAME_PLACE,
                t.dateTime,
                "Place Backup"
        )
    }

    override fun convert(t: Place): FileIndex? {
        return try {
            val json = Gson().toJson(t)
            val encrypted = MemoryUtil.encryptJson(json)
            FileIndex().apply {
                this.json = encrypted
                this.ext = FileConfig.FILE_NAME_PLACE
                this.id = t.id
                this.updatedAt = t.dateTime
                this.type = IndexTypes.TYPE_PLACE
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun convert(encrypted: String): Place? {
        if (encrypted.isEmpty()) return null
        return try {
            val json = MemoryUtil.decryptToJson(encrypted) ?: return null
            return Gson().fromJson(json, Place::class.java)
        } catch (e: Exception) {
            null
        }
    }
}