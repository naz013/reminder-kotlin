package com.github.naz013.repository.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteTextSpan
import com.github.naz013.domain.note.displayTitle
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.converters.NoteSpansTypeConverter
import com.google.gson.annotations.SerializedName
import java.util.Random
import java.util.UUID

@Entity(tableName = "Note")
@TypeConverters(NoteSpansTypeConverter::class)
internal data class NoteEntity(
  @SerializedName("text")
  val text: String = "",
  @SerializedName("spans")
  val spans: List<NoteTextSpan> = emptyList(),
  @SerializedName("displayTitle")
  val displayTitle: String = "",
  @SerializedName("key")
  @PrimaryKey
  val key: String = UUID.randomUUID().toString(),
  @SerializedName("date")
  val date: String = "",
  @SerializedName("color")
  val color: Int = 0,
  @SerializedName("style")
  val style: Int = 0,
  @SerializedName("uniqueId")
  val uniqueId: Int = Random().nextInt(Integer.MAX_VALUE),
  @SerializedName("updatedAt")
  val updatedAt: String? = null,
  @SerializedName("opacity")
  val opacity: Int = 100,
  @SerializedName("fontSize")
  val fontSize: Int = -1,
  @SerializedName("archived")
  val archived: Boolean = false,
  @SerializedName("isPinned")
  val isPinned: Boolean = false,
  @SerializedName("version")
  val version: Long = 0L,
  @SerializedName("syncState")
  val syncState: String
) {

  constructor(note: Note) : this(
    text = note.content.text,
    spans = note.content.spans,
    displayTitle = note.content.displayTitle(),
    key = note.key,
    date = note.date,
    color = note.color,
    style = note.style,
    uniqueId = note.uniqueId,
    updatedAt = note.updatedAt,
    opacity = note.opacity,
    fontSize = note.fontSize,
    archived = note.archived,
    isPinned = note.isPinned,
    version = note.version,
    syncState = note.syncState.name
  )

  fun toDomain(): Note {
    return Note(
      content = NoteDocument(text = text, spans = spans),
      key = key,
      date = date,
      color = color,
      style = style,
      uniqueId = uniqueId,
      updatedAt = updatedAt,
      opacity = opacity,
      fontSize = fontSize,
      archived = archived,
      isPinned = isPinned,
      version = version,
      syncState = SyncState.valueOf(syncState)
    )
  }
}
