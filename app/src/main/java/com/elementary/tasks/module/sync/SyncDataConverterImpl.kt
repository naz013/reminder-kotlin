package com.elementary.tasks.module.sync

import android.util.Base64
import android.util.Base64InputStream
import android.util.Base64OutputStream
import com.github.naz013.files.CopyByteArrayStream
import com.github.naz013.files.DataConverter
import com.github.naz013.files.DataType
import com.github.naz013.files.model.SettingsModel
import com.github.naz013.files.model.TagAssignmentsSnapshotJson
import com.github.naz013.logging.Logger
import com.github.naz013.sync.SyncDataConverter
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class SyncDataConverterImpl(
  private val dataConverter: DataConverter,
) : SyncDataConverter {
  override suspend fun create(any: Any): InputStream {
    if (any is SettingsModel) {
      return toInputStream(any)
    }
    if (any is TagAssignmentsSnapshotJson) {
      return toInputStream(any)
    }
    return dataConverter.toInputStream(any)
  }

  /**
   * Converts a [TagAssignmentsSnapshotJson] to a Base64-wrapped, Gson-serialized InputStream -
   * unlike [SettingsModel]'s legacy ObjectOutputStream codec, this is a normal typed data class,
   * so plain Gson (matching DataConverterImpl's own approach for every other entity) is enough.
   */
  private fun toInputStream(snapshot: TagAssignmentsSnapshotJson): InputStream {
    val outputBytes = CopyByteArrayStream()
    try {
      val base64Output = Base64OutputStream(outputBytes, Base64.DEFAULT)
      val writer = OutputStreamWriter(base64Output, StandardCharsets.UTF_8)
      writer.use { Gson().toJson(snapshot, TagAssignmentsSnapshotJson::class.java, it) }
      base64Output.close()
      return outputBytes.toInputStream()
    } catch (e: Exception) {
      Logger.e(TAG, "TagAssignmentsConverter: toInputStream error: $e")
      throw e
    } finally {
      outputBytes.close()
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
   * @throws java.io.IOException if encoding fails
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

  override suspend fun parse(
    stream: InputStream,
    dataType: DataType,
  ): Any {
    if (dataType == DataType.Settings) {
      @Suppress("UNCHECKED_CAST")
      return convert(stream)
    }
    if (dataType == DataType.TagAssignments) {
      return convertTagAssignments(stream)
    }
    return dataConverter.toData(stream)
  }

  private fun convertTagAssignments(stream: InputStream): TagAssignmentsSnapshotJson =
    try {
      val base64Input = Base64InputStream(stream, Base64.DEFAULT)
      val reader = BufferedReader(InputStreamReader(base64Input, StandardCharsets.UTF_8))
      reader.use { Gson().fromJson(it, TagAssignmentsSnapshotJson::class.java) }
    } catch (e: Exception) {
      Logger.e(TAG, "TagAssignmentsConverter: convert error: $e")
      throw e
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
  private fun convert(stream: InputStream): SettingsModel =
    try {
      val base64Input = Base64InputStream(stream, Base64.DEFAULT)
      val objectInput = ObjectInputStream(base64Input)

      objectInput.use { input ->
        val obj = input.readObject()

        // Validate the deserialized object is a Map
        if (obj !is Map<*, *>) {
          throw IllegalStateException(
            "Expected Map but got ${obj?.javaClass?.name ?: "null"}",
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

  companion object {
    private const val TAG = "SyncDataConverter"
  }
}
