package com.elementary.tasks.core.cloud.converters

import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.storages.FileIndex
import com.elementary.tasks.core.data.models.SettingsModel
import com.elementary.tasks.core.utils.PrefsConstants
import com.elementary.tasks.core.utils.TimeUtil
import java.io.*

class SettingsConverter : Convertible<SettingsModel> {

    override fun metadata(t: SettingsModel): Metadata {
        return Metadata(
                FileConfig.FILE_NAME_SETTINGS,
                FileConfig.FILE_NAME_SETTINGS_EXT,
                TimeUtil.gmtDateTime,
                "Settings Backup"
        )
    }

    override fun convert(t: SettingsModel): FileIndex? {
        return try {
            var output: ObjectOutputStream? = null
            val outputBytes = ByteArrayOutputStream()
            try {
                output = ObjectOutputStream(outputBytes)
                val list = t.data
                if (list.containsKey(PrefsConstants.DRIVE_USER)) {
                    list.remove(PrefsConstants.DRIVE_USER)
                }
                if (list.containsKey(PrefsConstants.TASKS_USER)) {
                    list.remove(PrefsConstants.TASKS_USER)
                }
                output.writeObject(list)
                val data = outputBytes.toString()
                output.close()
                FileIndex().apply {
                    this.json = data
                    this.ext = FileConfig.FILE_NAME_SETTINGS_EXT
                    this.id = "app"
                    this.updatedAt = TimeUtil.gmtDateTime
                    this.type = IndexTypes.TYPE_SETTINGS
                }
            } catch (e: IOException) {
                null
            } finally {
                try {
                    if (output != null) {
                        output.flush()
                        output.close()
                    }
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun convert(encrypted: String): SettingsModel? {
        if (encrypted.isEmpty()) return null
        return try {
            var input: ObjectInputStream? = null
            try {
                input = ObjectInputStream(ByteArrayInputStream(encrypted.toByteArray()))
                val entries = input.readObject() as MutableMap<String, *>
                SettingsModel(entries)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                try {
                    input?.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}