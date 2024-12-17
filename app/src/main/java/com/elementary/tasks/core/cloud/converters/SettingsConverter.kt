package com.elementary.tasks.core.cloud.converters

import android.util.Base64
import android.util.Base64InputStream
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.data.models.SettingsModel
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.CopyByteArrayStream
import com.elementary.tasks.core.utils.params.PrefsConstants
import com.github.naz013.logging.Logger
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class SettingsConverter(
  private val dateTimeManager: DateTimeManager
) : Convertible<SettingsModel> {

  override fun metadata(t: SettingsModel): Metadata {
    return Metadata(
      "app",
      FileConfig.FILE_NAME_SETTINGS,
      FileConfig.FILE_NAME_SETTINGS_EXT,
      dateTimeManager.getNowGmtDateTime(),
      "Settings Backup"
    )
  }

  override fun toOutputStream(t: SettingsModel): CopyByteArrayStream? {
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
        outputBytes
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
      Logger.e("SettingsConverter: toOutputStream error: $e")
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
      Logger.e("SettingsConverter: convert error: $e")
      null
    }
  }
}
