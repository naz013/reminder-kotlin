package com.github.naz013.domain.note

import com.github.naz013.domain.font.FontParams
import com.github.naz013.domain.sync.SyncState
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteWithImagesTest {

  @Test
  fun `getters fall back to defaults when note is null`() {
    val noteWithImages = NoteWithImages(note = null)

    assertEquals("", noteWithImages.getGmtTime())
    assertEquals("", noteWithImages.getSummary())
    assertEquals("", noteWithImages.getTitle())
    assertEquals("", noteWithImages.getKey())
    assertEquals(0, noteWithImages.getColor())
    assertEquals(FontParams.DEFAULT_FONT_STYLE, noteWithImages.getStyle())
    assertEquals(0, noteWithImages.getOpacity())
    assertEquals(FontParams.DEFAULT_FONT_SIZE, noteWithImages.getFontSize())
  }

  @Test
  fun `getters read through to the wrapped note when present`() {
    val note = Note(
      content = NoteDocument.fromLegacy(title = "Title", summary = "Summary"),
      key = "key-1",
      date = "2023-01-01",
      color = 5,
      style = 1,
      opacity = 80,
      fontSize = 18,
      syncState = SyncState.Synced
    )
    val noteWithImages = NoteWithImages(note = note)

    assertEquals("2023-01-01", noteWithImages.getGmtTime())
    assertEquals("Title\nSummary", noteWithImages.getSummary())
    assertEquals("Title", noteWithImages.getTitle())
    assertEquals("key-1", noteWithImages.getKey())
    assertEquals(5, noteWithImages.getColor())
    assertEquals(1, noteWithImages.getStyle())
    assertEquals(80, noteWithImages.getOpacity())
    assertEquals(18, noteWithImages.getFontSize())
  }
}
