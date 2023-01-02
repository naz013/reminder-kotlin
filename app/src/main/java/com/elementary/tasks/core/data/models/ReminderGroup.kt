package com.elementary.tasks.core.data.models

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity
@Keep
@Parcelize
data class ReminderGroup(
  @SerializedName("title")
  val groupTitle: String,
  @SerializedName("uuId")
  @PrimaryKey
  val groupUuId: String,
  @SerializedName("color")
  val groupColor: Int,
  @SerializedName("dateTime")
  val groupDateTime: String,
  @SerializedName("isDefaultGroup")
  val isDefaultGroup: Boolean
) : Parcelable
