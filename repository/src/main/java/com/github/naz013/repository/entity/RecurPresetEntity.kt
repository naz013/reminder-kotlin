package com.github.naz013.repository.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.github.naz013.domain.PresetBuilderScheme
import com.github.naz013.domain.PresetType
import com.github.naz013.domain.RecurPreset
import com.github.naz013.repository.converters.DateTimeTypeConverter
import com.github.naz013.repository.converters.PresetBuilderSchemeTypeConverter
import com.github.naz013.repository.converters.PresetTypeConverter
import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDateTime
import java.util.UUID

@Entity(tableName = "RecurPreset")
@TypeConverters(
  PresetBuilderSchemeTypeConverter::class,
  PresetTypeConverter::class,
  DateTimeTypeConverter::class
)
@Keep
internal data class RecurPresetEntity(
  @SerializedName("id")
  @PrimaryKey
  val id: String = UUID.randomUUID().toString(),
  @SerializedName("recurObject")
  val recurObject: String,
  @SerializedName("name")
  val name: String,
  @SerializedName("type")
  val type: PresetType,
  @SerializedName("createdAt")
  val createdAt: LocalDateTime,
  @SerializedName("builderScheme")
  val builderScheme: List<PresetBuilderScheme> = emptyList(),
  @SerializedName("useCount")
  val useCount: Int,
  @SerializedName("description")
  val description: String?,
  @SerializedName("isDefault")
  val isDefault: Boolean,
  @SerializedName("recurItemsToAdd")
  val recurItemsToAdd: String?
) {

  constructor(recurPreset: RecurPreset) : this(
    id = recurPreset.id,
    recurObject = recurPreset.recurObject,
    name = recurPreset.name,
    type = recurPreset.type,
    createdAt = recurPreset.createdAt,
    builderScheme = recurPreset.builderScheme,
    useCount = recurPreset.useCount,
    description = recurPreset.description,
    isDefault = recurPreset.isDefault,
    recurItemsToAdd = recurPreset.recurItemsToAdd
  )

  fun toDomain(): RecurPreset {
    return RecurPreset(
      id = id,
      recurObject = recurObject,
      name = name,
      type = type,
      createdAt = createdAt,
      builderScheme = builderScheme,
      useCount = useCount,
      description = description,
      isDefault = isDefault,
      recurItemsToAdd = recurItemsToAdd
    )
  }
}
