package com.github.naz013.domain.note

import java.io.Serializable

sealed interface NoteSpanAttribute : Serializable {
  data object Bold : NoteSpanAttribute
  data object Italic : NoteSpanAttribute
  data object Underline : NoteSpanAttribute
  data object Strikethrough : NoteSpanAttribute

  data class FontFamily(val code: Int) : NoteSpanAttribute
  data class FontSize(val sp: Int) : NoteSpanAttribute

  data class SolidColor(val argb: Int) : NoteSpanAttribute
  data class GradientColor(val colors: List<Int>, val angleDegrees: Float = 0f) : NoteSpanAttribute

  data object Heading1 : NoteSpanAttribute
  data object Heading2 : NoteSpanAttribute
  data object Heading3 : NoteSpanAttribute
  data object BulletItem : NoteSpanAttribute
}
