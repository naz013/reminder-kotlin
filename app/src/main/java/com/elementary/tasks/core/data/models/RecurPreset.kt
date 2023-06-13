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
data class RecurPreset(
  @SerializedName("id")
  @PrimaryKey
  val id: String = UUID.randomUUID().toString(),
  @SerializedName("recurObject")
  val recurObject: String,
  @SerializedName("name")
  var name: String,
) : Parcelable
