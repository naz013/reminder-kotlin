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
  var hidden: Int = 0,
  val uploaded: Boolean = false
) : Parcelable {

  @Ignore
  constructor(item: GoogleTask) : this(
    listId = item.listId,
    selfLink = item.selfLink,
    kind = item.kind,
    eTag = item.eTag,
    title = item.title,
    taskId = item.taskId,
    completeDate = item.completeDate,
    del = item.del,
    hidden = item.hidden,
    dueDate = item.dueDate,
    notes = item.notes,
    parent = item.parent,
    position = item.position,
    updateDate = item.updateDate,
    status = item.status,
    uuId = item.uuId,
    uploaded = true
  )
}
