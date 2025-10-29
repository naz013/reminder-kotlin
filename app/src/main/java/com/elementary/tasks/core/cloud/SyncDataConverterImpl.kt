package com.elementary.tasks.core.cloud

import android.util.Base64
import android.util.Base64InputStream
import android.util.Base64OutputStream
import com.github.naz013.cloudapi.stream.CopyByteArrayStream
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.note.OldNote
import com.github.naz013.logging.Logger
import com.github.naz013.sync.SyncDataConverter
import com.github.naz013.sync.settings.SettingsModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class SyncDataConverterImpl : SyncDataConverter {

  override suspend fun create(any: Any): InputStream {
    if (any is SettingsModel) {
      return toInputStream(any)
    }
    val outputStream = CopyByteArrayStream()
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
      } ?: run {
        throw IllegalArgumentException("Unsupported type: ${any::class.java}")
      }
      Gson().toJson(any, type, writer)
      writer.close()
      output64.close()
      return outputStream.toInputStream()
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to create InputStream: $e")
      throw e
    } finally {
      outputStream.close()
    }
  }

  private fun toInputStream(t: SettingsModel): InputStream {
    return try {
      var output: ObjectOutputStream? = null
      val outputBytes = CopyByteArrayStream()
      try {
        output = ObjectOutputStream(outputBytes)
        output.writeObject(t.data)
        outputBytes.toInputStream()
      } catch (e: IOException) {
        throw e
      } finally {
        try {
          if (output != null) {
            output.flush()
            output.close()
          }
        } catch (ex: IOException) {
          Logger.e(TAG, "SettingsConverter: toInputStream error: $ex")
        }
      }
    } catch (e: Exception) {
      Logger.e(TAG, "SettingsConverter: toInputStream error: $e")
      throw e
    }
  }

  override suspend fun <T> parse(stream: InputStream, clazz: Class<T>): T {
    if (clazz == SettingsModel::class.java) {
      return convert(stream) as T
    }
    val output64 = Base64InputStream(stream, Base64.DEFAULT)
    val bufferedReader = BufferedReader(InputStreamReader(output64))
    val reader = JsonReader(bufferedReader)
    return reader.use { Gson().fromJson<T>(reader, clazz) }
  }

  private fun convert(stream: InputStream): SettingsModel {
    return try {
      var input: ObjectInputStream? = null
      try {
        input = ObjectInputStream(Base64InputStream(stream, Base64.DEFAULT))
        val entries = input.readObject() as Map<String, *>
        SettingsModel(entries)
      } catch (e: Exception) {
        throw e
      } finally {
        try {
          input?.close()
        } catch (ex: IOException) {
          Logger.e(TAG, "SettingsConverter: convert error: $ex")
        }
      }
    } catch (e: Exception) {
      Logger.e(TAG, "SettingsConverter: convert error: $e")
      throw e
    }
  }

  companion object {
    private const val TAG = "SyncDataConverter"
  }
}
