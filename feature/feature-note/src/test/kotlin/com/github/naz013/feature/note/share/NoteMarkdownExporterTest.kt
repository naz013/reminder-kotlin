package com.github.naz013.feature.note.share

import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteSpanAttribute
import com.github.naz013.domain.note.NoteTextSpan
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteMarkdownExporterTest {

  private val fonts = listOf("Roboto Black", "Roboto Bold")
  private val fontName: (Int) -> String? = { fonts.getOrNull(it) }

  private fun markdown(text: String, vararg spans: NoteTextSpan) =
    NoteDocument(text, spans.toList()).toHybridMarkdown(fontName)

  private fun span(start: Int, end: Int, attribute: NoteSpanAttribute) = NoteTextSpan(start, end, attribute)

  @Test
  fun `returns empty string for an empty note`() {
    assertEquals("", markdown(""))
  }

  @Test
  fun `returns text unchanged when there are no spans`() {
    assertEquals("Hello world", markdown("Hello world"))
  }

  @Test
  fun `converts headings one to three to hash prefixes`() {
    val text = "One\nTwo\nThree"

    val result = markdown(
      text,
      span(0, 3, NoteSpanAttribute.Heading1),
      span(4, 7, NoteSpanAttribute.Heading2),
      span(8, 13, NoteSpanAttribute.Heading3),
    )

    assertEquals("# One\n## Two\n### Three", result)
  }

  @Test
  fun `converts bullet lines to dash prefixes and separates a following plain line`() {
    val result = markdown("a\nb\nplain", span(0, 1, NoteSpanAttribute.BulletItem), span(2, 3, NoteSpanAttribute.BulletItem))

    assertEquals("- a\n- b\n\nplain", result)
  }

  @Test
  fun `wraps bold italic and both with the right number of asterisks`() {
    val text = "bold ital both"

    val result = markdown(
      text,
      span(0, 4, NoteSpanAttribute.Bold),
      span(5, 9, NoteSpanAttribute.Italic),
      span(10, 14, NoteSpanAttribute.Bold),
      span(10, 14, NoteSpanAttribute.Italic),
    )

    assertEquals("**bold** *ital* ***both***", result)
  }

  @Test
  fun `wraps strikethrough in double tildes`() {
    assertEquals("~~gone~~", markdown("gone", span(0, 4, NoteSpanAttribute.Strikethrough)))
  }

  @Test
  fun `wraps underline in a u tag`() {
    assertEquals("<u>under</u>", markdown("under", span(0, 5, NoteSpanAttribute.Underline)))
  }

  @Test
  fun `writes an opaque solid color as a hex span`() {
    val result = markdown("red", span(0, 3, NoteSpanAttribute.SolidColor(0xFFFF0000.toInt())))

    assertEquals("<span style=\"color:#FF0000\">red</span>", result)
  }

  @Test
  fun `writes a translucent solid color as rgba`() {
    val result = markdown("red", span(0, 3, NoteSpanAttribute.SolidColor(0x80FF0000.toInt())))

    assertEquals("<span style=\"color:rgba(255,0,0,0.5)\">red</span>", result)
  }

  @Test
  fun `writes a gradient as a clipped background with angle and stops`() {
    val gradient = NoteSpanAttribute.GradientColor(listOf(0xFFFF0000.toInt(), 0xFF0000FF.toInt()), 45f)

    val result = markdown("text", span(0, 4, gradient))

    assertEquals(
      "<span style=\"background:linear-gradient(45deg,#FF0000,#0000FF);" +
        "-webkit-background-clip:text;color:transparent\">text</span>",
      result,
    )
  }

  @Test
  fun `merges color size and family covering the same range into one span`() {
    val result = markdown(
      "text",
      span(0, 4, NoteSpanAttribute.SolidColor(0xFF00FF00.toInt())),
      span(0, 4, NoteSpanAttribute.FontSize(20)),
      span(0, 4, NoteSpanAttribute.FontFamily(1)),
    )

    assertEquals("<span style=\"color:#00FF00;font-size:20px;font-family:'Roboto Bold'\">text</span>", result)
  }

  @Test
  fun `omits font family when the code is unknown`() {
    val result = markdown("text", span(0, 4, NoteSpanAttribute.FontFamily(99)))

    assertEquals("text", result)
  }

  @Test
  fun `puts html outside and markdown markers inside`() {
    val result = markdown(
      "text",
      span(0, 4, NoteSpanAttribute.Bold),
      span(0, 4, NoteSpanAttribute.Underline),
      span(0, 4, NoteSpanAttribute.SolidColor(0xFF000000.toInt())),
    )

    assertEquals("<span style=\"color:#000000\"><u>**text**</u></span>", result)
  }

  @Test
  fun `splits overlapping spans into runs with their own combination of styles`() {
    val result = markdown("abcdef", span(0, 4, NoteSpanAttribute.Bold), span(2, 6, NoteSpanAttribute.Italic))

    assertEquals("**ab**<!---->***cd***<!---->*ef*", result)
  }

  @Test
  fun `closes and reopens a span that crosses lines`() {
    val result = markdown("ab\ncd", span(0, 5, NoteSpanAttribute.Bold))

    assertEquals("**ab**  \n**cd**", result)
  }

  @Test
  fun `keeps blanks outside the markers`() {
    val result = markdown(" hi  ", span(0, 5, NoteSpanAttribute.Bold))

    assertEquals(" **hi**  ", result)
  }

  @Test
  fun `never wraps a whitespace only range`() {
    val result = markdown("a   b", span(1, 4, NoteSpanAttribute.Bold), span(1, 4, NoteSpanAttribute.Underline))

    assertEquals("a   b", result)
  }

  @Test
  fun `adds a hard break between consecutive plain lines only`() {
    assertEquals("a  \nb\n\nc", markdown("a\nb\n\nc"))
  }

  @Test
  fun `escapes a plain line that looks like a heading or bullet`() {
    assertEquals("\\# not heading  \n\\- not bullet", markdown("# not heading\n- not bullet"))
  }

  @Test
  fun `does not escape literal html characters`() {
    assertEquals("a < b & <c>", markdown("a < b & <c>"))
  }

  @Test
  fun `combines a heading line with inline formatting`() {
    val result = markdown("Title", span(0, 5, NoteSpanAttribute.Heading2), span(0, 5, NoteSpanAttribute.Bold))

    assertEquals("## **Title**", result)
  }
}
