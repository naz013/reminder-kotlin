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
data class SmsTemplate(
  @SerializedName("title")
  var title: String = "",
  @SerializedName("key")
  @PrimaryKey
  var key: String = UUID.randomUUID().toString(),
  @SerializedName("date")
  var date: String = ""
) : Parcelable
