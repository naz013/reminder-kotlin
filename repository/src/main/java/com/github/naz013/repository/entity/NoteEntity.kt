package com.github.naz013.repository.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.sync.SyncState
import com.google.gson.annotations.SerializedName
import java.util.Random
import java.util.UUID

@Entity(tableName = "Note")
internal data class NoteEntity(
  @SerializedName("summary")
  val summary: String = "",
  @SerializedName("key")
  @PrimaryKey
  val key: String = UUID.randomUUID().toString(),
  @SerializedName("date")
  val date: String = "",
  @SerializedName("color")
  val color: Int = 0,
  @SerializedName("style")
  val style: Int = 0,
  @SerializedName("palette")
  val palette: Int = 0,
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
  @SerializedName("version")
  val version: Long = 0L,
  @SerializedName("syncState")
  val syncState: String
) {

  constructor(note: Note) : this(
    summary = note.summary,
    key = note.key,
    date = note.date,
    color = note.color,
    style = note.style,
    palette = note.palette,
    uniqueId = note.uniqueId,
    updatedAt = note.updatedAt,
    opacity = note.opacity,
    fontSize = note.fontSize,
    archived = note.archived,
    version = note.version,
    syncState = note.syncState.name
  )

  fun toDomain(): Note {
    return Note(
      summary = summary,
      key = key,
      date = date,
      color = color,
      style = style,
      palette = palette,
      uniqueId = uniqueId,
      updatedAt = updatedAt,
      opacity = opacity,
      fontSize = fontSize,
      archived = archived,
      version = version,
      syncState = SyncState.valueOf(syncState)
    )
  }
}
