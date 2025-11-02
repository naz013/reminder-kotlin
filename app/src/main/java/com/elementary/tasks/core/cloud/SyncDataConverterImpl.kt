package com.elementary.tasks.core.cloud

import android.util.Base64
import android.util.Base64InputStream
import android.util.Base64OutputStream
import com.github.naz013.cloudapi.stream.CopyByteArrayStream
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.RecurPreset
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.sync.NoteV3Json
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
        is RecurPreset -> object : TypeToken<RecurPreset>() {}.type
        is NoteV3Json -> object : TypeToken<NoteV3Json>() {}.type
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

  /**
   * Converts SettingsModel to InputStream with Base64 encoding.
   *
   * Encodes the settings data map using ObjectOutputStream wrapped in Base64OutputStream
   * to ensure consistent encoding with the decoding process.
   *
   * @param t The SettingsModel to encode
   * @return Base64-encoded InputStream containing the serialized settings
   * @throws IOException if encoding fails
   */
  private fun toInputStream(t: SettingsModel): InputStream {
    val outputBytes = CopyByteArrayStream()
    try {
      // Wrap in Base64 encoding to match decoding expectations
      val base64Output = Base64OutputStream(outputBytes, Base64.DEFAULT)
      val objectOutput = ObjectOutputStream(base64Output)

      objectOutput.use { output ->
        output.writeObject(t.data)
      }

      base64Output.close()

      // Convert to InputStream after all streams are closed
      return outputBytes.toInputStream()
    } catch (e: IOException) {
      Logger.e(TAG, "SettingsConverter: toInputStream error: $e")
      throw e
    } catch (e: Exception) {
      Logger.e(TAG, "SettingsConverter: toInputStream unexpected error: $e")
      throw e
    } finally {
      outputBytes.close()
    }
  }

  override suspend fun <T> parse(stream: InputStream, clazz: Class<T>): T {
    if (clazz == SettingsModel::class.java) {
      @Suppress("UNCHECKED_CAST")
      return convert(stream) as T
    }
    val output64 = Base64InputStream(stream, Base64.DEFAULT)
    val bufferedReader = BufferedReader(InputStreamReader(output64))
    val reader = JsonReader(bufferedReader)
    return reader.use { Gson().fromJson<T>(reader, clazz) }
  }

  /**
   * Converts Base64-encoded InputStream to SettingsModel.
   *
   * Decodes the Base64-encoded stream and deserializes the settings data map.
   * Validates that the deserialized object is actually a Map.
   *
   * @param stream The Base64-encoded InputStream containing serialized settings
   * @return SettingsModel with the deserialized settings data
   * @throws IOException if decoding fails
   * @throws ClassNotFoundException if the serialized class is not found
   * @throws IllegalStateException if the deserialized object is not a Map
   */
  private fun convert(stream: InputStream): SettingsModel {
    return try {
      val base64Input = Base64InputStream(stream, Base64.DEFAULT)
      val objectInput = ObjectInputStream(base64Input)

      objectInput.use { input ->
        val obj = input.readObject()

        // Validate the deserialized object is a Map
        if (obj !is Map<*, *>) {
          throw IllegalStateException(
            "Expected Map but got ${obj?.javaClass?.name ?: "null"}"
          )
        }

        @Suppress("UNCHECKED_CAST")
        val entries = obj as Map<String, *>
        SettingsModel(entries)
      }
    } catch (e: IOException) {
      Logger.e(TAG, "SettingsConverter: convert IO error: $e")
      throw e
    } catch (e: ClassNotFoundException) {
      Logger.e(TAG, "SettingsConverter: convert class not found: $e")
      throw e
    } catch (e: ClassCastException) {
      Logger.e(TAG, "SettingsConverter: convert cast error: $e")
      throw IllegalStateException("Failed to cast deserialized object to Map", e)
    } catch (e: Exception) {
      Logger.e(TAG, "SettingsConverter: convert unexpected error: $e")
      throw e
    }
  }

  companion object {
    private const val TAG = "SyncDataConverter"
  }
}
