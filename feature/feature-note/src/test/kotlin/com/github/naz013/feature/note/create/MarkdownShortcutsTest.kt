package com.github.naz013.feature.note.create

import com.github.naz013.domain.note.NoteSpanAttribute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class MarkdownShortcutsTest {

  @Test
  fun `detects heading markers by length`() {
    assertEquals(MarkdownShortcut(0, 2, NoteSpanAttribute.Heading1), detectMarkdownShortcut("#", "# ", 2))
    assertEquals(MarkdownShortcut(0, 3, NoteSpanAttribute.Heading2), detectMarkdownShortcut("##", "## ", 3))
    assertEquals(MarkdownShortcut(0, 4, NoteSpanAttribute.Heading3), detectMarkdownShortcut("###", "### ", 4))
  }

  @Test
  fun `detects dash and star bullets on a later line`() {
    assertEquals(MarkdownShortcut(2, 4, NoteSpanAttribute.BulletItem), detectMarkdownShortcut("a\n-", "a\n- ", 4))
    assertEquals(MarkdownShortcut(2, 4, NoteSpanAttribute.BulletItem), detectMarkdownShortcut("a\n*", "a\n* ", 4))
  }

  @Test
  fun `ignores four hashes and markers after text`() {
    assertNull(detectMarkdownShortcut("####", "#### ", 5))
    assertNull(detectMarkdownShortcut("a #", "a # ", 4))
  }

  @Test
  fun `ignores multi character insertions`() {
    assertNull(detectMarkdownShortcut("", "# ", 2))
    assertNull(detectMarkdownShortcut("#", "#  ", 3))
  }

  @Test
  fun `ignores a space that is not the typed character`() {
    assertNull(detectMarkdownShortcut("#a", "#b", 2))
  }
}
