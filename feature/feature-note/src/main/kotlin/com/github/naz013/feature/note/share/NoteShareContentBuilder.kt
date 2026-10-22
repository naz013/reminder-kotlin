package com.github.naz013.feature.note.share

import com.github.naz013.domain.note.NoteDocument

internal enum class NoteShareFormat { ORIGINAL, PLAIN_TEXT, MARKDOWN, HTML }

/** Text payload of a "Share as" action. [mimeType] is `text/plain` or `text/html`. */
internal data class NoteShareText(
  val subject: String,
  val text: String,
  val mimeType: String,
) {
  val isHtml: Boolean get() = mimeType == MIME_HTML

  companion object {
    const val MIME_PLAIN = "text/plain"
    const val MIME_HTML = "text/html"
  }
}

/** Turns a note into the text of a [NoteShareFormat]. [fontNames] lists the app fonts by family code. */
internal class NoteShareContentBuilder(
  private val fontNames: () -> List<String>,
) {

  /** Returns null for [NoteShareFormat.ORIGINAL], which shares the `.note` file instead of text. */
  fun build(
    format: NoteShareFormat,
    title: String,
    document: NoteDocument,
    backgroundArgb: Int? = null,
    contentArgb: Int? = null,
  ): NoteShareText? {
    val names = fontNames()
    val fontName: (Int) -> String? = { code -> names.getOrNull(code) }
    return when (format) {
      NoteShareFormat.ORIGINAL -> null
      NoteShareFormat.PLAIN_TEXT -> NoteShareText(title, document.text, NoteShareText.MIME_PLAIN)
      NoteShareFormat.MARKDOWN ->
        NoteShareText(title, document.toHybridMarkdown(fontName), NoteShareText.MIME_PLAIN)

      NoteShareFormat.HTML -> NoteShareText(
        subject = title,
        text = document.toHtmlDocument(title, backgroundArgb, contentArgb, fontName),
        mimeType = NoteShareText.MIME_HTML,
      )
    }
  }
}
