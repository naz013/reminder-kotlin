package com.elementary.tasks.core.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Base64InputStream
import android.util.Base64OutputStream
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.OldNote
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.data.models.SmsTemplate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import timber.log.Timber
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.math.ln
import kotlin.math.pow


object MemoryUtil {

  private const val DIR_SD = "backup"
  private const val DIR_PREFS = "preferences"
  private const val DIR_NOTES_SD = "notes"
  private const val DIR_GROUP_SD = "groups"
  private const val DIR_BIRTHDAY_SD = "birthdays"
  private const val DIR_PLACES_SD = "places"
  private const val DIR_TEMPLATES_SD = "templates"

  private val isSdPresent: Boolean
    get() {
      val state = Environment.getExternalStorageState()
      return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
    }

  val remindersDir = getDir(DIR_SD)
  val groupsDir = getDir(DIR_GROUP_SD)
  val birthdaysDir = getDir(DIR_BIRTHDAY_SD)
  val notesDir = getDir(DIR_NOTES_SD)
  val placesDir = getDir(DIR_PLACES_SD)
  val templatesDir = getDir(DIR_TEMPLATES_SD)
  val prefsDir = getDir(DIR_PREFS)
  val parent = getDir("")
  val imagesDir = getDir("image_cache")

  private fun getDir(directory: String): File? {
    return if (isSdPresent) {
      val sdPath = Environment.getExternalStorageDirectory()
      val dir = File("$sdPath/JustReminder/$directory")
      if (!dir.exists() && dir.mkdirs()) {
        dir
      } else dir
    } else {
      null
    }
  }

  fun humanReadableByte(bytes: Long, si: Boolean): String {
    val unit = if (si) 1000 else 1024
    if (bytes < unit) {
      return "$bytes B"
    }
    val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
    val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else ""
    return String.format(Locale.US, "%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
  }

  @Throws(IOException::class)
  fun writeFileNoEncryption(file: File, data: String?): String? {
    if (data == null) return null
    try {
      val inputStream = ByteArrayInputStream(data.toByteArray())
      val buffer = ByteArray(8192)
      var bytesRead: Int
      val output = ByteArrayOutputStream()
      try {
        do {
          bytesRead = inputStream.read(buffer)
          if (bytesRead != -1) {
            output.write(buffer, 0, bytesRead)
          }
        } while (bytesRead != -1)
      } catch (e: IOException) {
        e.printStackTrace()
      }

      if (file.exists()) {
        file.delete()
      }
      val fw = FileWriter(file)
      fw.write(output.toString())
      fw.close()
      output.close()
    } catch (e: SecurityException) {
      return null
    }
    return file.toString()
  }

  fun <T> fromStream(stream: InputStream, clazz: Class<T>): T? {
    try {
      val output64 = Base64InputStream(stream, Base64.DEFAULT)
      val bufferedReader = BufferedReader(InputStreamReader(output64))
      val reader = JsonReader(bufferedReader)
      Timber.d("fromStream: $stream, $clazz")
      val t: T?
      try {
        t = Gson().fromJson<T>(reader, clazz)
      } catch (e: Exception) {
        return null
      } catch (e: OutOfMemoryError) {
        return null
      }
      reader.close()
      return t
    } catch (e: Exception) {
      e.printStackTrace()
      return null
    }
  }

  fun <T> fromStreamNoDecrypt(stream: InputStream, clazz: Class<T>): T? {
    try {
      val bufferedReader = BufferedReader(InputStreamReader(stream))
      val reader = JsonReader(bufferedReader)
      Timber.d("fromStream: $stream, $clazz")
      val t: T?
      try {
        t = Gson().fromJson<T>(reader, clazz)
      } catch (e: Exception) {
        return null
      } catch (e: OutOfMemoryError) {
        return null
      }
      reader.close()
      return t
    } catch (e: Exception) {
      e.printStackTrace()
      return null
    }
  }

  fun toStream(any: Any, outputStream: OutputStream): Boolean {
    try {
      System.gc()
      val output64 = Base64OutputStream(outputStream, Base64.DEFAULT)
      val bufferedWriter = BufferedWriter(OutputStreamWriter(output64, StandardCharsets.UTF_8))
      val writer = JsonWriter(bufferedWriter)
      val type = when (any) {
        is Reminder -> object : TypeToken<Reminder>() {}.type
        is Place -> object : TypeToken<Place>() {}.type
        is Birthday -> object : TypeToken<Birthday>() {}.type
        is ReminderGroup -> object : TypeToken<ReminderGroup>() {}.type
        is SmsTemplate -> object : TypeToken<SmsTemplate>() {}.type
        is NoteWithImages -> object : TypeToken<OldNote>() {}.type
        else -> null
      } ?: return false
      Timber.d("toStream: $type, $any")
      try {
        Gson().toJson(if (any is NoteWithImages) OldNote(any) else any, type, writer)
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
    val cursor: Cursor? = cr.query(uri, null, null,
      null, null, null)

    val name = try {
      cursor?.use {
        if (it.moveToFirst()) {
          val displayName: String =
            it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
          displayName
        } else {
          source
        }
      } ?: source
    } catch (e: Exception) {
      source
    }
    Timber.d("readFromUri: $name, $source")
    return try {
      val output64 = Base64InputStream(inputStream, Base64.DEFAULT)
      val r = JsonReader(BufferedReader(InputStreamReader(output64)))
      val type = when {
        name.endsWith(FileConfig.FILE_NAME_PLACE) -> {
          object : TypeToken<Place>() {}.type
        }
        name.endsWith(FileConfig.FILE_NAME_REMINDER) -> {
          object : TypeToken<Reminder>() {}.type
        }
        name.endsWith(FileConfig.FILE_NAME_BIRTHDAY) -> {
          object : TypeToken<Birthday>() {}.type
        }
        name.endsWith(FileConfig.FILE_NAME_GROUP) -> {
          object : TypeToken<ReminderGroup>() {}.type
        }
        name.endsWith(FileConfig.FILE_NAME_NOTE) -> {
          object : TypeToken<OldNote>() {}.type
        }
        name.endsWith(FileConfig.FILE_NAME_TEMPLATE) -> {
          object : TypeToken<SmsTemplate>() {}.type
        }
        else -> null
      }
      Gson().fromJson(r, type)
    } catch (e: Exception) {
      Timber.d("readFromUri: Bad JSON")
      e.printStackTrace()
      null
    }
  }
}
