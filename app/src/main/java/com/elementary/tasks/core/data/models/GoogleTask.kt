package com.elementary.tasks.core.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class GoogleTask(
  var title: String = "",
  @PrimaryKey
  var taskId: String = "",
  var completeDate: Long = 0,
  var del: Int = 0,
  var dueDate: Long = 0,
  var eTag: String = "",
  var kind: String = "",
  var notes: String = "",
  var parent: String = "",
  var position: String = "",
  var selfLink: String = "",
  var updateDate: Long = 0,
  var listId: String = "",
  var status: String = "",
  var uuId: String = "",
  var hidden: Int = 0
) : Parcelable {

  @Ignore
  constructor(item: GoogleTask) : this() {
    this.listId = item.listId
    this.selfLink = item.selfLink
    this.kind = item.kind
    this.eTag = item.eTag
    this.title = item.title
    this.taskId = item.taskId
    this.completeDate = item.completeDate
    this.del = item.del
    this.hidden = item.hidden
    this.dueDate = item.dueDate
    this.notes = item.notes
    this.parent = item.parent
    this.position = item.position
    this.updateDate = item.updateDate
    this.status = item.status
    this.uuId = item.uuId
  }
}
