package com.github.naz013.repository.converters

import androidx.room.TypeConverter
import com.github.naz013.domain.note.NoteTextSpan
import com.github.naz013.domain.note.toJson
import com.github.naz013.domain.note.toNoteTextSpans

internal class NoteSpansTypeConverter {

  @TypeConverter
  fun toJson(spans: List<NoteTextSpan>): String {
    return spans.toJson()
  }

  @TypeConverter
  fun toSpans(json: String): List<NoteTextSpan> {
    return json.toNoteTextSpans()
  }
}
