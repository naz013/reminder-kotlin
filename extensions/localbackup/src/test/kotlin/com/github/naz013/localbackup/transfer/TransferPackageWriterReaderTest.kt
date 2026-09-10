package com.github.naz013.localbackup.transfer

import com.github.naz013.domain.note.ImageFile
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.files.DataConverter
import com.github.naz013.files.model.NoteV4Json
import com.github.naz013.localbackup.archive.BackupArchiveReader
import com.github.naz013.localbackup.archive.BackupArchiveWriter
import com.github.naz013.localbackup.archive.BackupEnvelope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

/** See BackupArchiveReaderWriterTest for why a hand-rolled fake stands in for the real
 * DataConverter - extended here to also cover NoteV4Json, the one type this writer/reader pair
 * adds on top of the envelope types BackupArchiveWriter/Reader already handle. */
private class FakeDataConverter : DataConverter {
  override suspend fun toOutputStream(any: Any, outputStream: OutputStream) {
    val encoded = when (any) {
      is ReminderV2 -> "R|${any.uuId}|${any.summary}"
      is NoteV4Json -> "N|${any.key}|${any.text}"
      else -> error("FakeDataConverter does not support ${any::class.java}")
    }
    outputStream.use { it.write(encoded.toByteArray()) }
  }

  override suspend fun toInputStream(any: Any): InputStream {
    val buffer = ByteArrayOutputStream()
    toOutputStream(any, buffer)
    return ByteArrayInputStream(buffer.toByteArray())
  }

  override suspend fun toData(stream: InputStream): Any {
    val (tag, id, label) = stream.readBytes().decodeToString().split("|", limit = 3)
    return when (tag) {
      "R" -> ReminderV2(uuId = id, summary = label, schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))
      "N" -> NoteV4Json(key = id, text = label)
      else -> error("FakeDataConverter does not support tag $tag")
    }
  }
}

class TransferPackageWriterReaderTest {

  private val dataConverter = FakeDataConverter()
  private val writer = TransferPackageWriter(dataConverter, BackupArchiveWriter(dataConverter))
  private val reader = TransferPackageReader(dataConverter, BackupArchiveReader(dataConverter))

  private fun reminder(id: String) = ReminderV2(
    uuId = id,
    summary = "Reminder $id",
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 1, 1, 9, 0))
  )

  private fun note(key: String, text: String) = Note(
    content = NoteDocument(text = text),
    key = key,
    syncState = SyncState.Synced
  )

  @Test
  fun `round trips an envelope with no notes`() = runTest {
    val envelope = BackupEnvelope(reminders = listOf(reminder("r1")))
    val output = ByteArrayOutputStream()

    writer.write(output, envelope, notes = emptyList())
    val result = reader.read(ByteArrayInputStream(output.toByteArray()))

    assertEquals(1, result.envelope.reminders.size)
    assertTrue(result.notes.isEmpty())
  }

  @Test
  fun `round trips a note with no images`() = runTest {
    val output = ByteArrayOutputStream()
    val notes = listOf(NoteWithImages(note = note("n1", "Groceries"), images = emptyList()))

    writer.write(output, BackupEnvelope(), notes)
    val result = reader.read(ByteArrayInputStream(output.toByteArray()))

    assertEquals(1, result.notes.size)
    val item = result.notes.single()
    assertEquals("n1", item.note.key)
    assertEquals("Groceries", item.note.content.text)
    assertTrue(item.images.isEmpty())
  }

  @Test
  fun `round trips a note's image bytes keyed by its own note`() = runTest {
    val output = ByteArrayOutputStream()
    val imageBytes = byteArrayOf(1, 2, 3, 4)
    val imageFile = java.io.File.createTempFile("transfer_test", ".jpg").apply { writeBytes(imageBytes) }
    val notes = listOf(
      NoteWithImages(
        note = note("n1", "Trip photo"),
        images = listOf(ImageFile(noteId = "n1", fileName = "photo.jpg", filePath = imageFile.absolutePath))
      )
    )

    writer.write(output, BackupEnvelope(), notes)
    val result = reader.read(ByteArrayInputStream(output.toByteArray()))

    val item = result.notes.single()
    assertEquals(1, item.images.size)
    val (fileName, bytes) = item.images.single()
    assertEquals("photo.jpg", fileName)
    assertTrue(imageBytes.contentEquals(bytes))
    imageFile.delete()
  }

  @Test
  fun `keeps two notes' images separate when both use the same file name`() = runTest {
    val output = ByteArrayOutputStream()
    val fileA = java.io.File.createTempFile("transfer_test_a", ".jpg").apply { writeBytes(byteArrayOf(1)) }
    val fileB = java.io.File.createTempFile("transfer_test_b", ".jpg").apply { writeBytes(byteArrayOf(2)) }
    val notes = listOf(
      NoteWithImages(
        note = note("n1", "First"),
        images = listOf(ImageFile(noteId = "n1", fileName = "photo.jpg", filePath = fileA.absolutePath))
      ),
      NoteWithImages(
        note = note("n2", "Second"),
        images = listOf(ImageFile(noteId = "n2", fileName = "photo.jpg", filePath = fileB.absolutePath))
      ),
    )

    writer.write(output, BackupEnvelope(), notes)
    val result = reader.read(ByteArrayInputStream(output.toByteArray()))

    val byKey = result.notes.associateBy { it.note.key }
    assertEquals(1, byKey.getValue("n1").images.single().second[0].toInt())
    assertEquals(2, byKey.getValue("n2").images.single().second[0].toInt())
    fileA.delete()
    fileB.delete()
  }

  @Test
  fun `skips an image whose file no longer exists on disk`() = runTest {
    val output = ByteArrayOutputStream()
    val notes = listOf(
      NoteWithImages(
        note = note("n1", "Missing image"),
        images = listOf(ImageFile(noteId = "n1", fileName = "gone.jpg", filePath = "/no/such/file.jpg"))
      )
    )

    writer.write(output, BackupEnvelope(), notes)
    val result = reader.read(ByteArrayInputStream(output.toByteArray()))

    assertTrue(result.notes.single().images.isEmpty())
  }
}
