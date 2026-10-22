package com.github.naz013.feature.note.share

import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteSpanAttribute
import com.github.naz013.domain.note.NoteTextSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteHtmlExporterTest {

  private val fontName: (Int) -> String? = { listOf("Roboto Black", "Roboto Bold").getOrNull(it) }

  private fun body(text: String, vararg spans: NoteTextSpan) =
    NoteDocument(text, spans.toList()).toHtmlBody(fontName)

  private fun span(start: Int, end: Int, attribute: NoteSpanAttribute) = NoteTextSpan(start, end, attribute)

  @Test
  fun `produces an empty body for an empty note`() {
    assertEquals(emptyList<String>(), body(""))
  }

  @Test
  fun `wraps plain text in a paragraph`() {
    assertEquals(listOf("<p>Hello</p>"), body("Hello"))
  }

  @Test
  fun `joins consecutive plain lines with br and splits paragraphs on a blank line`() {
    assertEquals(listOf("<p>a<br>b</p>", "<p>c</p>"), body("a\nb\n\nc"))
  }

  @Test
  fun `converts headings to h1 through h3`() {
    val result = body(
      "One\nTwo\nThree",
      span(0, 3, NoteSpanAttribute.Heading1),
      span(4, 7, NoteSpanAttribute.Heading2),
      span(8, 13, NoteSpanAttribute.Heading3),
    )

    assertEquals(listOf("<h1>One</h1>", "<h2>Two</h2>", "<h3>Three</h3>"), result)
  }

  @Test
  fun `groups consecutive bullet lines into one list`() {
    val result = body(
      "a\nb\nplain",
      span(0, 1, NoteSpanAttribute.BulletItem),
      span(2, 3, NoteSpanAttribute.BulletItem),
    )

    assertEquals(listOf("<ul>", "<li>a</li>", "<li>b</li>", "</ul>", "<p>plain</p>"), result)
  }

  @Test
  fun `closes an open list at the end of the note`() {
    assertEquals(listOf("<ul>", "<li>a</li>", "</ul>"), body("a", span(0, 1, NoteSpanAttribute.BulletItem)))
  }

  @Test
  fun `nests strong em s and u tags`() {
    val result = body(
      "x",
      span(0, 1, NoteSpanAttribute.Bold),
      span(0, 1, NoteSpanAttribute.Italic),
      span(0, 1, NoteSpanAttribute.Strikethrough),
      span(0, 1, NoteSpanAttribute.Underline),
    )

    assertEquals(listOf("<p><u><s><em><strong>x</strong></em></s></u></p>"), result)
  }

  @Test
  fun `writes color size and family in one span and wraps the tags`() {
    val result = body(
      "x",
      span(0, 1, NoteSpanAttribute.Bold),
      span(0, 1, NoteSpanAttribute.SolidColor(0x80102030.toInt())),
      span(0, 1, NoteSpanAttribute.FontSize(18)),
      span(0, 1, NoteSpanAttribute.FontFamily(0)),
    )

    assertEquals(
      listOf(
        "<p><span style=\"color:rgba(16,32,48,0.5);font-size:18px;font-family:'Roboto Black'\">" +
          "<strong>x</strong></span></p>",
      ),
      result,
    )
  }

  @Test
  fun `writes a gradient span`() {
    val gradient = NoteSpanAttribute.GradientColor(listOf(0xFFFF0000.toInt(), 0xFF00FF00.toInt()), 90f)

    val result = body("x", span(0, 1, gradient))

    assertEquals(
      listOf(
        "<p><span style=\"background:linear-gradient(90deg,#FF0000,#00FF00);" +
          "-webkit-background-clip:text;color:transparent\">x</span></p>",
      ),
      result,
    )
  }

  @Test
  fun `lets the last color span win over an earlier overlapping one`() {
    val result = body(
      "ab",
      span(0, 2, NoteSpanAttribute.SolidColor(0xFFFF0000.toInt())),
      span(1, 2, NoteSpanAttribute.SolidColor(0xFF0000FF.toInt())),
    )

    assertEquals(
      listOf("<p><span style=\"color:#FF0000\">a</span><span style=\"color:#0000FF\">b</span></p>"),
      result,
    )
  }

  @Test
  fun `closes and reopens a span on every line`() {
    val result = body("a\nb", span(0, 3, NoteSpanAttribute.Underline))

    assertEquals(listOf("<p><u>a</u><br><u>b</u></p>"), result)
  }

  @Test
  fun `does not wrap whitespace only ranges and keeps blanks outside tags`() {
    val result = body(" a  b", span(0, 3, NoteSpanAttribute.Bold), span(2, 3, NoteSpanAttribute.Underline))

    assertEquals(listOf("<p> <strong>a</strong>  b</p>"), result)
  }

  @Test
  fun `escapes html special characters in text`() {
    assertEquals(listOf("<p>a &lt; b &amp; &lt;c&gt; &quot;d&quot;</p>"), body("a < b & <c> \"d\""))
  }

  @Test
  fun `builds a complete document with title charset and body colors`() {
    val html = NoteDocument("Hi").toHtmlDocument(
      title = "A & B",
      backgroundArgb = 0xFFFFFFFF.toInt(),
      contentArgb = 0xFF000000.toInt(),
      fontName = fontName,
    )

    assertEquals(
      "<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n<title>A &amp; B</title>\n</head>\n" +
        "<body style=\"background-color:#FFFFFF;color:#000000\">\n<p>Hi</p>\n</body>\n</html>",
      html,
    )
  }

  @Test
  fun `builds a document with a plain body tag when no colors are given`() {
    val html = NoteDocument("").toHtmlDocument("T", null, null, fontName)

    assertTrue(html.contains("<body>\n</body>"))
  }
}
