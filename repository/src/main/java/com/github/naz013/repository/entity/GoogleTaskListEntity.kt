package com.github.naz013.repository.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.naz013.domain.GoogleTaskList
import com.google.gson.annotations.SerializedName

@Entity(tableName = "GoogleTaskList")
internal data class GoogleTaskListEntity(
  @SerializedName("title")
  val title: String = "",
  @SerializedName("listId")
  @PrimaryKey
  val listId: String = "",
  @SerializedName("def")
  val def: Int = 0,
  @SerializedName("eTag")
  val eTag: String = "",
  @SerializedName("kind")
  val kind: String = "",
  @SerializedName("selfLink")
  val selfLink: String = "",
  @SerializedName("updated")
  val updated: Long = 0,
  @SerializedName("color")
  val color: Int = 0,
  @SerializedName("systemDefault")
  val systemDefault: Int = 0,
  @SerializedName("uploaded")
  val uploaded: Boolean = false
) {

  constructor(googleTaskList: GoogleTaskList) : this(
    title = googleTaskList.title,
    listId = googleTaskList.listId,
    def = googleTaskList.def,
    eTag = googleTaskList.eTag,
    kind = googleTaskList.kind,
    selfLink = googleTaskList.selfLink,
    updated = googleTaskList.updated,
    color = googleTaskList.color,
    systemDefault = googleTaskList.systemDefault,
    uploaded = googleTaskList.uploaded
  )

  fun toDomain() = GoogleTaskList(
    title = title,
    listId = listId,
    def = def,
    eTag = eTag,
    kind = kind,
    selfLink = selfLink,
    updated = updated,
    color = color,
    systemDefault = systemDefault,
    uploaded = uploaded
  )
}
