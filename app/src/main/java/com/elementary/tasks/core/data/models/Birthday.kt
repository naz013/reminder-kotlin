package com.elementary.tasks.core.data.models

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity
@Keep
data class Birthday(
  @SerializedName("name")
  val name: String = "",
  @SerializedName("date")
  val date: String = "",
  @SerializedName("number")
  val number: String = "",
  @SerializedName("key")
  val key: String = "",
  @SerializedName("showedYear")
  val showedYear: Int = 0,
  @SerializedName("contactId")
  val contactId: Long = 0L,
  @SerializedName("day")
  val day: Int = 0,
  @SerializedName("month")
  val month: Int = 0,
  @SerializedName("uniqueId")
  val uniqueId: Int = Random().nextInt(Integer.MAX_VALUE),
  @SerializedName("dayMonth")
  val dayMonth: String = "",
  @SerializedName("uuId")
  @PrimaryKey
  val uuId: String = UUID.randomUUID().toString(),
  @SerializedName("updatedAt")
  val updatedAt: String? = null,
  @SerializedName("ignoreYear")
  val ignoreYear: Boolean = false
)
