package com.github.naz013.domain.note

import com.github.naz013.domain.sync.SyncState
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Random
import java.util.UUID

data class Note(
  @SerializedName("summary")
  var summary: String = "",
  @SerializedName("key")
  var key: String = UUID.randomUUID().toString(),
  @SerializedName("date")
  var date: String = "",
  @SerializedName("color")
  var color: Int = 0,
  @SerializedName("style")
  var style: Int = 0,
  @SerializedName("palette")
  var palette: Int = 0,
  @SerializedName("uniqueId")
  var uniqueId: Int = Random().nextInt(Integer.MAX_VALUE),
  @SerializedName("updatedAt")
  var updatedAt: String? = null,
  @SerializedName("opacity")
  var opacity: Int = 100,
  @SerializedName("fontSize")
  var fontSize: Int = -1,
  @SerializedName("archived")
  var archived: Boolean = false,
  @SerializedName("versionId")
  var version: Long = 0L,
  @Transient
  val syncState: SyncState,
) : Serializable {

  constructor(oldNote: OldNote) : this(
    color = oldNote.color,
    palette = oldNote.palette,
    key = oldNote.key,
    date = oldNote.date,
    style = oldNote.style,
    uniqueId = oldNote.uniqueId,
    summary = oldNote.summary,
    updatedAt = oldNote.updatedAt,
    fontSize = oldNote.fontSize,
    archived = oldNote.archived,
    syncState = SyncState.Synced,
  )
}
