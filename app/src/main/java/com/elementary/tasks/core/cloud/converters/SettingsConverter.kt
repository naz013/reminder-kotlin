package com.elementary.tasks.core.cloud.converters

import android.util.Base64
import android.util.Base64InputStream
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.storages.FileIndex
import com.elementary.tasks.core.data.models.SettingsModel
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.CopyByteArrayStream
import com.elementary.tasks.core.utils.params.PrefsConstants
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class SettingsConverter : Convertible<SettingsModel> {

  override fun metadata(t: SettingsModel): Metadata {
    return Metadata(
      "app",
      FileConfig.FILE_NAME_SETTINGS,
      FileConfig.FILE_NAME_SETTINGS_EXT,
      DateTimeManager.gmtDateTime,
      "Settings Backup"
    )
  }

  override fun convert(t: SettingsModel): FileIndex? {
    return try {
      var output: ObjectOutputStream? = null
      val outputBytes = CopyByteArrayStream()
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
        FileIndex().apply {
          this.stream = outputBytes
          this.ext = FileConfig.FILE_NAME_SETTINGS_EXT
          this.id = "app"
          this.updatedAt = DateTimeManager.gmtDateTime
          this.type = IndexTypes.TYPE_SETTINGS
          this.readyToBackup = true
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
      Timber.e(e)
      null
    }
  }

  override fun convert(stream: InputStream): SettingsModel? {
    return try {
      var input: ObjectInputStream? = null
      try {
        input = ObjectInputStream(Base64InputStream(stream, Base64.DEFAULT))
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
      Timber.e(e)
      null
    }
  }
}