package com.github.naz013.domain

import com.google.gson.annotations.SerializedName

data class GoogleTaskList(
  @SerializedName("title")
  var title: String = "",
  @SerializedName("listId")
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
) {

  fun isDefault() = def == 1
}
