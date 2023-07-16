package com.elementary.tasks.core.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity
@Parcelize
data class Note(
  var summary: String = "",
  @PrimaryKey
  var key: String = UUID.randomUUID().toString(),
  var date: String = "",
  var color: Int = 0,
  var style: Int = 0,
  var palette: Int = 0,
  var uniqueId: Int = Random().nextInt(Integer.MAX_VALUE),
  @SerializedName("updatedAt")
  var updatedAt: String? = null,
  var opacity: Int = 100,
  var fontSize: Int = -1,
  var archived: Boolean = false
) : Parcelable {

  @Ignore
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
    archived = oldNote.archived
  )
}
