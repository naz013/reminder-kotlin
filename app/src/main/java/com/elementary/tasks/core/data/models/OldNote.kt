package com.elementary.tasks.core.data.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class OldNote(
  @SerializedName("summary")
  var summary: String = "",
  @SerializedName("key")
  var key: String = "",
  @SerializedName("date")
  var date: String = "",
  @SerializedName("color")
  var color: Int = 0,
  @SerializedName("palette")
  var palette: Int = 0,
  @SerializedName("style")
  var style: Int = 0,
  @SerializedName("images")
  var images: List<ImageFile> = ArrayList(),
  @SerializedName("updatedAt")
  var updatedAt: String? = null,
  @SerializedName("uniqueId")
  var uniqueId: Int = 0) {

  constructor(noteWithImages: NoteWithImages) : this() {
    this.images = noteWithImages.images
    val note = noteWithImages.note ?: return
    this.uniqueId = note.uniqueId
    this.style = note.style
    this.color = note.color
    this.palette = note.palette
    this.date = note.date
    this.key = note.key
    this.summary = note.summary
    this.updatedAt = note.updatedAt
  }
}