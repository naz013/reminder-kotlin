package com.elementary.tasks.core.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.api.services.tasks.model.TaskList
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
  constructor(taskList: TaskList, color: Int) : this() {
    this.color = color
    this.title = taskList.title ?: ""
    this.listId = taskList.id ?: ""
    this.eTag = taskList.etag ?: ""
    this.kind = taskList.kind ?: ""
    this.selfLink = taskList.selfLink ?: ""
    this.updated = if (taskList.updated != null) taskList.updated.value else 0
  }

  fun update(taskList: TaskList) {
    this.title = taskList.title ?: ""
    this.listId = taskList.id ?: ""
    this.eTag = taskList.etag ?: ""
    this.kind = taskList.kind ?: ""
    this.selfLink = taskList.selfLink ?: ""
    this.updated = if (taskList.updated != null) taskList.updated.value else 0
  }

  @Ignore
  fun isAppDefault() = systemDefault == 1
}
