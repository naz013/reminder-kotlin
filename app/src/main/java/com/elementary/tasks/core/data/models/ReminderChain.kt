package com.elementary.tasks.core.data.models

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.elementary.tasks.core.utils.datetime.TimeUtil
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

@Entity
@Keep
data class ReminderChain(
  @SerializedName("previousId")
  var previousId: String = "",
  @SerializedName("nextId")
  var nextId: String = "",
  @SerializedName("activationType")
  var activationType: Int = 0,
  @SerializedName("gmtTime")
  var gmtTime: String = TimeUtil.gmtDateTime,
  @SerializedName("uuId")
  @PrimaryKey
  var uuId: String = UUID.randomUUID().toString()
) : Serializable
