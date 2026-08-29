package com.github.naz013.feature.note.create

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteSpanAttribute
import com.github.naz013.ui.note.toAnnotatedString

private const val BULLET_PREFIX_LENGTH = 3 // "•  " - see NoteDocumentRenderer.BULLET_PREFIX

/**
 * Renders [document] with the same styling as the read-only [toAnnotatedString], but as a
 * [VisualTransformation] so it can back the editable field directly: bullet-line glyphs are
 * inserted into the *displayed* text only, with a real [OffsetMapping] translating cursor/
 * selection positions between the editable buffer and the decorated display - the buffer itself
 * never gains characters the user didn't type.
 */
internal fun noteEditVisualTransformation(
  document: NoteDocument,
  baseFontSizeSp: Int,
  fontFamilyResolver: (Int) -> FontFamily?,
): VisualTransformation = VisualTransformation { _ ->
  val annotated = document.toAnnotatedString(baseFontSizeSp, fontFamilyResolver = fontFamilyResolver)

  val lineStarts = listOf(0) + document.text.indices.filter { document.text[it] == '\n' }.map { it + 1 }
  val bulletLineStarts = document.spans
    .filter { it.attribute == NoteSpanAttribute.BulletItem }
    .map { it.start }
    .filter { it in lineStarts }
    .sorted()

  val mapping = object : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
      val insertedBefore = bulletLineStarts.count { it <= offset }
      return offset + insertedBefore * BULLET_PREFIX_LENGTH
    }

    override fun transformedToOriginal(offset: Int): Int {
      var shift = 0
      for (lineStart in bulletLineStarts) {
        val insertPoint = lineStart + shift
        if (offset <= insertPoint) break
        if (offset < insertPoint + BULLET_PREFIX_LENGTH) return lineStart
        shift += BULLET_PREFIX_LENGTH
      }
      return offset - shift
    }
  }

  TransformedText(annotated, mapping)
}
