package com.github.naz013.repository.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteTextSpan
import com.github.naz013.domain.note.NoteVersion
import com.github.naz013.repository.converters.NoteSpansTypeConverter

@Entity(tableName = "NoteVersion", indices = [Index(value = ["noteKey"])])
@TypeConverters(NoteSpansTypeConverter::class)
@Keep
internal data class NoteVersionEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0L,
  val noteKey: String,
  val text: String,
  val spans: List<NoteTextSpan>,
  val savedAt: Long,
)

internal fun NoteVersion.toEntity(): NoteVersionEntity = NoteVersionEntity(
  id = id,
  noteKey = noteKey,
  text = content.text,
  spans = content.spans,
  savedAt = savedAt,
)

internal fun NoteVersionEntity.toDomain(): NoteVersion = NoteVersion(
  id = id,
  noteKey = noteKey,
  content = NoteDocument(text = text, spans = spans),
  savedAt = savedAt,
)
