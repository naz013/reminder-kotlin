package com.github.naz013.localbackup.transfer

import com.github.naz013.domain.note.ImageFile
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteSpanAttribute
import com.github.naz013.domain.note.NoteTextSpan
import com.github.naz013.domain.sync.SyncState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteTransferMapperTest {

  @Test
  fun `round trips a note's fields and spans through the transfer json`() {
    val note = Note(
      content = NoteDocument(
        text = "Buy milk",
        spans = listOf(NoteTextSpan(start = 0, end = 3, attribute = NoteSpanAttribute.Bold))
      ),
      key = "n1",
      date = "2026-01-01",
      color = 5,
      style = 1,
      uniqueId = 42,
      fontSize = 16,
      archived = true,
      isPinned = true,
      version = 3,
      syncState = SyncState.Synced
    )

    val json = note.toTransferJson(images = emptyList())
    val result = json.toTransferNote()

    assertEquals(note.key, result.key)
    assertEquals(note.content.text, result.content.text)
    assertEquals(1, result.content.spans.size)
    assertEquals(NoteSpanAttribute.Bold, result.content.spans.single().attribute)
    assertEquals(note.color, result.color)
    assertEquals(note.style, result.style)
    assertEquals(note.uniqueId, result.uniqueId)
    assertEquals(note.fontSize, result.fontSize)
    assertEquals(note.archived, result.archived)
    assertEquals(note.isPinned, result.isPinned)
    assertEquals(note.version, result.version)
  }

  @Test
  fun `carries each image's file name and size into the json manifest`() {
    val file = java.io.File.createTempFile("note_transfer_mapper_test", ".jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
    val note = Note(content = NoteDocument(text = "Photo note"), key = "n1", syncState = SyncState.Synced)
    val image = ImageFile(noteId = "n1", fileName = "photo.jpg", filePath = file.absolutePath)

    val json = note.toTransferJson(images = listOf(image))

    assertEquals(1, json.images.size)
    assertEquals("photo.jpg", json.images.single().fileName)
    assertEquals(3, json.images.single().size)
    assertTrue(file.delete())
  }
}
