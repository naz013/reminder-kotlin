package com.github.naz013.domain.reminder

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.UUID

data class ShopItem(
  @SerializedName("summary")
  var summary: String = "",
  @SerializedName("isDeleted")
  var isDeleted: Boolean = false,
  @SerializedName("checked")
  var isChecked: Boolean = false,
  @SerializedName("uuId")
  var uuId: String = UUID.randomUUID().toString(),
  @SerializedName("createTime")
  var createTime: String,
  @Expose(serialize = false, deserialize = false)
  var position: Int = 0,
  @Expose(serialize = false, deserialize = false)
  var showInput: Boolean = false,
  @Expose(serialize = false, deserialize = false)
  var canRemove: Boolean = false
)
