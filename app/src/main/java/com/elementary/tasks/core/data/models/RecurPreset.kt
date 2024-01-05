package com.elementary.tasks.core.data.models

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.elementary.tasks.core.data.converters.DateTimeTypeConverter
import com.elementary.tasks.core.data.converters.PresetBuilderSchemeTypeConverter
import com.elementary.tasks.core.data.converters.PresetTypeConverter
import com.elementary.tasks.reminder.build.bi.BiType
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.threeten.bp.LocalDateTime
import java.util.UUID

@Entity
@TypeConverters(
  PresetBuilderSchemeTypeConverter::class,
  PresetTypeConverter::class,
  DateTimeTypeConverter::class
)
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
  @SerializedName("type")
  val type: PresetType,
  @SerializedName("createdAt")
  val createdAt: LocalDateTime,
  @SerializedName("builderScheme")
  val builderScheme: List<PresetBuilderScheme> = emptyList(),
  @SerializedName("useCount")
  val useCount: Int
) : Parcelable

enum class PresetType {
  RECUR,
  BUILDER
}

@Parcelize
data class PresetBuilderScheme(
  @SerializedName("type")
  val type: BiType,
  @SerializedName("position")
  val position: Int,
  @SerializedName("value")
  val value: String
) : Parcelable
