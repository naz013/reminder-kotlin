package com.github.naz013.feature.note.compare

import com.github.naz013.ui.common.compose.foundation.DeviceScreenConfiguration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteLineDiffTest {
  private fun List<DiffLine>.pairs() = map { it.text to it.change }

  @Test
  fun `identical texts have only unchanged lines`() {
    val diff = diffLines("a\nb", "a\nb")

    assertTrue(diff.isIdentical)
    assertEquals(listOf("a" to LineChange.Same, "b" to LineChange.Same), diff.history.pairs())
    assertEquals(diff.history, diff.current)
  }

  @Test
  fun `line only in current is added`() {
    val diff = diffLines("a\nc", "a\nb\nc")

    assertFalse(diff.isIdentical)
    assertEquals(listOf("a" to LineChange.Same, "c" to LineChange.Same), diff.history.pairs())
    assertEquals(
      listOf("a" to LineChange.Same, "b" to LineChange.Added, "c" to LineChange.Same),
      diff.current.pairs(),
    )
  }

  @Test
  fun `line only in history is removed`() {
    val diff = diffLines("a\nb\nc", "a\nc")

    assertEquals(
      listOf("a" to LineChange.Same, "b" to LineChange.Removed, "c" to LineChange.Same),
      diff.history.pairs(),
    )
    assertEquals(listOf("a" to LineChange.Same, "c" to LineChange.Same), diff.current.pairs())
  }

  @Test
  fun `changed line is removed in history and added in current`() {
    val diff = diffLines("a\nold\nc", "a\nnew\nc")

    assertEquals(
      listOf("a" to LineChange.Same, "old" to LineChange.Removed, "c" to LineChange.Same),
      diff.history.pairs(),
    )
    assertEquals(
      listOf("a" to LineChange.Same, "new" to LineChange.Added, "c" to LineChange.Same),
      diff.current.pairs(),
    )
  }

  @Test
  fun `empty history marks every current line as added`() {
    val diff = diffLines("", "a\nb")

    assertTrue(diff.history.isEmpty())
    assertEquals(listOf("a" to LineChange.Added, "b" to LineChange.Added), diff.current.pairs())
  }

  @Test
  fun `empty current marks every history line as removed`() {
    val diff = diffLines("a\nb", "")

    assertEquals(listOf("a" to LineChange.Removed, "b" to LineChange.Removed), diff.history.pairs())
    assertTrue(diff.current.isEmpty())
  }

  @Test
  fun `both empty is identical`() {
    val diff = diffLines("", "")

    assertTrue(diff.isIdentical)
    assertTrue(diff.history.isEmpty() && diff.current.isEmpty())
  }

  @Test
  fun `moved line keeps the longest common run unchanged`() {
    val diff = diffLines("a\nb\nc", "b\nc\na")

    assertEquals(
      listOf("a" to LineChange.Removed, "b" to LineChange.Same, "c" to LineChange.Same),
      diff.history.pairs(),
    )
    assertEquals(
      listOf("b" to LineChange.Same, "c" to LineChange.Same, "a" to LineChange.Added),
      diff.current.pairs(),
    )
  }

  @Test
  fun `compact phone portrait stacks panes`() {
    assertEquals(CompareLayout.Stacked, compareLayoutFor(DeviceScreenConfiguration.MobilePortrait))
  }

  @Test
  fun `wider window classes put panes side by side`() {
    DeviceScreenConfiguration.entries
      .filter { it != DeviceScreenConfiguration.MobilePortrait }
      .forEach { assertEquals(CompareLayout.SideBySide, compareLayoutFor(it)) }
  }
}
