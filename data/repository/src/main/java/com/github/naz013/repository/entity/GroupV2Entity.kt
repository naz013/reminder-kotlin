package com.github.naz013.repository.entity

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.github.naz013.repository.converters.ReminderV2NullableIntListConverter
import com.github.naz013.repository.converters.ReminderV2VibrationPatternConverter
import java.util.UUID

@Entity(
  tableName = "GroupV2",
  indices = [
    Index(value = ["isDefault"]),
    Index(value = ["syncState"])
  ]
)
@TypeConverters(
  ReminderV2VibrationPatternConverter::class,
  ReminderV2NullableIntListConverter::class
)
@Keep
internal data class GroupV2Entity(
  @PrimaryKey
  val uuId: String = UUID.randomUUID().toString(),
  val title: String = "",
  val color: Int = 0,
  val isDefault: Boolean = false,

  @Embedded(prefix = "notif_")
  val notification: NotificationSettingsOverrideColumns,

  val createdAt: Long,

  val version: Long = 0L,
  val syncState: String
)
