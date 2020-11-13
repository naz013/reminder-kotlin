package com.elementary.tasks.core.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.api.services.tasks.model.Task
import kotlinx.android.parcel.Parcelize

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

  @Ignore
  constructor(task: Task, listId: String) : this() {
    this.listId = listId
    val dueDate = task.due
    val due = dueDate?.value ?: 0
    val completeDate = task.completed
    val complete = completeDate?.value ?: 0
    val updateDate = task.updated
    val update = updateDate?.value ?: 0
    val taskId = task.id ?: ""
    var isDeleted = false
    try {
      isDeleted = task.deleted ?: false
    } catch (ignored: NullPointerException) {
    }

    var isHidden = false
    try {
      isHidden = task.hidden ?: false
    } catch (ignored: NullPointerException) {
    }

    this.selfLink = task.selfLink ?: ""
    this.kind = task.kind ?: ""
    this.eTag = task.etag ?: ""
    this.title = task.title ?: ""
    this.taskId = taskId
    this.completeDate = complete
    this.del = if (isDeleted) 1 else 0
    this.hidden = if (isHidden) 1 else 0
    this.dueDate = due
    this.notes = task.notes ?: ""
    this.parent = task.parent ?: ""
    this.position = task.position ?: ""
    this.updateDate = update
    this.status = task.status ?: ""
  }

  fun update(task: Task) {
    val dueDate = task.due
    val due = dueDate?.value ?: 0
    val completeDate = task.completed
    val complete = completeDate?.value ?: 0
    val updateDate = task.updated
    val update = updateDate?.value ?: 0
    var isDeleted = false
    try {
      isDeleted = task.deleted ?: false
    } catch (ignored: NullPointerException) {
    }

    var isHidden = false
    try {
      isHidden = task.hidden ?: false
    } catch (ignored: NullPointerException) {
    }

    this.selfLink = task.selfLink ?: ""
    this.kind = task.kind ?: ""
    this.eTag = task.etag ?: ""
    this.title = task.title ?: ""
    this.taskId = task.id ?: ""
    this.completeDate = complete
    this.del = if (isDeleted) 1 else 0
    this.hidden = if (isHidden) 1 else 0
    this.dueDate = due
    this.notes = task.notes ?: ""
    this.parent = task.parent ?: ""
    this.position = task.position ?: ""
    this.updateDate = update
    this.status = task.status ?: ""
  }
}
