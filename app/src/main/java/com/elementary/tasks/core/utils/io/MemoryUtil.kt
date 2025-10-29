package com.elementary.tasks.core.utils.io

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Base64InputStream
import android.util.Base64OutputStream
import com.github.naz013.cloudapi.FileConfig
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.note.OldNote
import com.github.naz013.feature.common.readString
import com.github.naz013.logging.Logger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class MemoryUtil {

  fun toStream(any: Any, outputStream: OutputStream): Boolean {
    try {
      val output64 = Base64OutputStream(outputStream, Base64.DEFAULT)
      val bufferedWriter = BufferedWriter(OutputStreamWriter(output64, StandardCharsets.UTF_8))
      val writer = JsonWriter(bufferedWriter)
      val type = when (any) {
        is Reminder -> object : TypeToken<Reminder>() {}.type
        is Place -> object : TypeToken<Place>() {}.type
        is Birthday -> object : TypeToken<Birthday>() {}.type
        is ReminderGroup -> object : TypeToken<ReminderGroup>() {}.type
        is OldNote -> object : TypeToken<OldNote>() {}.type
        else -> null
      } ?: return false
      Logger.d("toStream: $type, $any")
      try {
        Gson().toJson(any, type, writer)
      } catch (e: Exception) {
        return false
      } catch (e: OutOfMemoryError) {
        System.gc()
        return false
      }
      writer.close()
      return true
    } catch (e: Exception) {
      e.printStackTrace()
      return false
    }
  }

  fun toStream(inputStream: InputStream, outputStream: OutputStream): Boolean {
    try {
      inputStream.copyTo(outputStream)
      return true
    } catch (e: Exception) {
      e.printStackTrace()
      return false
    } finally {
      inputStream.close()
    }
  }

  companion object {
    private const val DIR_PREFS = "preferences"

    private val isSdPresent: Boolean
      get() {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
      }

    val prefsDir = getDir(DIR_PREFS)
    val parent = getDir("")

    private fun getDir(directory: String): File? {
      return if (isSdPresent) {
        val sdPath = Environment.getExternalStorageDirectory()
        val dir = File("$sdPath/JustReminder/$directory")
        if (!dir.exists() && dir.mkdirs()) {
          dir
        } else {
          dir
        }
      } else {
        null
      }
    }

    fun readFromUri(context: Context, uri: Uri, source: String = ""): Any? {
      val cr = context.contentResolver ?: return null
      var inputStream: InputStream? = null
      try {
        inputStream = cr.openInputStream(uri)
      } catch (e: Exception) {
        e.printStackTrace()
      }

      if (inputStream == null) {
        return null
      }
      val cursor: Cursor? = cr.query(
        /* uri = */ uri,
        /* projection = */ null,
        /* selection = */ null,
        /* selectionArgs = */ null,
        /* sortOrder = */ null,
        /* cancellationSignal = */ null
      )

      val name = try {
        cursor?.use {
          if (it.moveToFirst()) {
            it.readString(OpenableColumns.DISPLAY_NAME) ?: source
          } else {
            source
          }
        } ?: source
      } catch (e: Exception) {
        source
      }
      Logger.d("readFromUri: $name, $source")
      return try {
        val output64 = Base64InputStream(inputStream, Base64.DEFAULT)
        val bufferedReader = BufferedReader(InputStreamReader(output64))
        val reader = JsonReader(bufferedReader)
        val obj: Any? = when {
          name.endsWith(FileConfig.FILE_NAME_PLACE) -> {
            Gson().fromJson<Place>(reader, object : TypeToken<Place>() {}.type)
          }
          name.endsWith(FileConfig.FILE_NAME_REMINDER) -> {
            Gson().fromJson<Reminder>(reader, object : TypeToken<Reminder>() {}.type)
          }
          name.endsWith(FileConfig.FILE_NAME_BIRTHDAY) -> {
            Gson().fromJson<Birthday>(reader, object : TypeToken<Birthday>() {}.type)
          }
          name.endsWith(FileConfig.FILE_NAME_GROUP) -> {
            Gson().fromJson<ReminderGroup>(reader, object : TypeToken<ReminderGroup>() {}.type)
          }
          name.endsWith(FileConfig.FILE_NAME_NOTE) -> {
            Gson().fromJson<OldNote>(reader, object : TypeToken<OldNote>() {}.type)
          }
          else -> null
        }
        Logger.d("readFromUri: obj=$obj")
        obj
      } catch (e: Exception) {
        Logger.d("readFromUri: Bad JSON")
        e.printStackTrace()
        null
      }
    }
  }
}
