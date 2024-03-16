package com.elementary.tasks.core.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class GoogleTaskList(
  @SerializedName("title")
  var title: String = "",
  @SerializedName("listId")
  @PrimaryKey
  var listId: String = "",
  @SerializedName("def")
  var def: Int = 0,
  @SerializedName("eTag")
  var eTag: String = "",
  @SerializedName("kind")
  var kind: String = "",
  @SerializedName("selfLink")
  var selfLink: String = "",
  @SerializedName("updated")
  var updated: Long = 0,
  @SerializedName("color")
  var color: Int = 0,
  @SerializedName("systemDefault")
  var systemDefault: Int = 0,
  @SerializedName("uploaded")
  val uploaded: Boolean = false
) : Parcelable {

  @Ignore
  fun isDefault() = def == 1
}
