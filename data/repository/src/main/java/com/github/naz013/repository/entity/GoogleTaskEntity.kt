package com.github.naz013.repository.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.naz013.domain.GoogleTask
import com.google.gson.annotations.SerializedName

@Entity(tableName = "GoogleTask")
internal data class GoogleTaskEntity(
  @SerializedName("title")
  val title: String = "",
  @SerializedName("taskId")
  @PrimaryKey
  val taskId: String = "",
  @SerializedName("completeDate")
  val completeDate: Long = 0,
  @SerializedName("del")
  val del: Int = 0,
  @SerializedName("dueDate")
  val dueDate: Long = 0,
  @SerializedName("eTag")
  val eTag: String = "",
  @SerializedName("kind")
  val kind: String = "",
  @SerializedName("notes")
  val notes: String = "",
  @SerializedName("parent")
  val parent: String = "",
  @SerializedName("position")
  val position: String = "",
  @SerializedName("selfLink")
  val selfLink: String = "",
  @SerializedName("updateDate")
  val updateDate: Long = 0,
  @SerializedName("listId")
  val listId: String = "",
  @SerializedName("status")
  val status: String = "",
  @SerializedName("uuId")
  val uuId: String = "",
  @SerializedName("hidden")
  val hidden: Int = 0,
  @SerializedName("uploaded")
  val uploaded: Boolean = false
) {

  constructor(googleTask: GoogleTask) : this(
    title = googleTask.title,
    taskId = googleTask.taskId,
    completeDate = googleTask.completeDate,
    del = googleTask.del,
    dueDate = googleTask.dueDate,
    eTag = googleTask.eTag,
    kind = googleTask.kind,
    notes = googleTask.notes,
    parent = googleTask.parent,
    position = googleTask.position,
    selfLink = googleTask.selfLink,
    updateDate = googleTask.updateDate,
    listId = googleTask.listId,
    status = googleTask.status,
    uuId = googleTask.uuId,
    hidden = googleTask.hidden,
    uploaded = googleTask.uploaded
  )

  fun toDomain(): GoogleTask {
    return GoogleTask(
      title = title,
      taskId = taskId,
      completeDate = completeDate,
      del = del,
      dueDate = dueDate,
      eTag = eTag,
      kind = kind,
      notes = notes,
      parent = parent,
      position = position,
      selfLink = selfLink,
      updateDate = updateDate,
      listId = listId,
      status = status,
      uuId = uuId,
      hidden = hidden,
      uploaded = uploaded
    )
  }
}
