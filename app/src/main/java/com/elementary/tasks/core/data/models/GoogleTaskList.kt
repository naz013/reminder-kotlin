package com.elementary.tasks.core.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class GoogleTaskList(
  var title: String = "",
  @PrimaryKey
  var listId: String = "",
  var def: Int = 0,
  var eTag: String = "",
  var kind: String = "",
  var selfLink: String = "",
  var updated: Long = 0,
  var color: Int = 0,
  var systemDefault: Int = 0
) : Parcelable {

  @Ignore
  fun isDefault() = def == 1
}
