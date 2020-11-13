package com.elementary.tasks.core.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
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
  var opacity: Int = 100) : Parcelable {

  @Ignore
  constructor(oldNote: OldNote) : this() {
    this.color = oldNote.color
    this.palette = oldNote.palette
    this.key = oldNote.key
    this.date = oldNote.date
    this.style = oldNote.style
    this.uniqueId = oldNote.uniqueId
    this.summary = oldNote.summary
    this.updatedAt = oldNote.updatedAt
  }
}
