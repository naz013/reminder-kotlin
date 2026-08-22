package com.github.naz013.localbackup.archive

import com.github.naz013.files.DataConverter
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.OutputStream

/**
 * Frames every item as a length-prefixed blob produced by the existing [DataConverter] (already
 * used by cloud sync), so this module never has to duplicate Reminder/Group JSON mapping logic -
 * it just reuses whatever [DataConverter] already knows how to (de)serialize.
 */
internal class BackupArchiveWriter(
  private val dataConverter: DataConverter
) {
  suspend fun write(output: OutputStream, envelope: BackupEnvelope) {
    val items: List<Any> =
      envelope.reminders + envelope.groups + envelope.birthdays + envelope.places + envelope.presets +
        envelope.tags + envelope.tagAssignments + envelope.routines + envelope.routineExecutions
    val dataOutput = DataOutputStream(output)
    dataOutput.writeInt(FORMAT_VERSION)
    dataOutput.writeInt(items.size)
    for (item in items) {
      val buffer = ByteArrayOutputStream()
      dataConverter.toOutputStream(item, buffer)
      val bytes = buffer.toByteArray()
      dataOutput.writeInt(bytes.size)
      dataOutput.write(bytes)
    }
    dataOutput.flush()
  }

  companion object {
    const val FORMAT_VERSION = 1
  }
}
