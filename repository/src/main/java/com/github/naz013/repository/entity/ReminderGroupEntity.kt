package com.github.naz013.repository.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.naz013.domain.ReminderGroup
import com.google.gson.annotations.SerializedName

@Entity(tableName = "ReminderGroup")
@Keep
internal data class ReminderGroupEntity(
  @SerializedName("groupTitle")
  val groupTitle: String,
  @SerializedName("groupUuId")
  @PrimaryKey
  val groupUuId: String,
  @SerializedName("groupColor")
  val groupColor: Int,
  @SerializedName("groupDateTime")
  val groupDateTime: String,
  @SerializedName("isDefaultGroup")
  val isDefaultGroup: Boolean
) {

  constructor(reminderGroup: ReminderGroup) : this(
    groupTitle = reminderGroup.groupTitle,
    groupUuId = reminderGroup.groupUuId,
    groupColor = reminderGroup.groupColor,
    groupDateTime = reminderGroup.groupDateTime,
    isDefaultGroup = reminderGroup.isDefaultGroup
  )

  fun toDomain(): ReminderGroup {
    return ReminderGroup(
      groupTitle = groupTitle,
      groupUuId = groupUuId,
      groupColor = groupColor,
      groupDateTime = groupDateTime,
      isDefaultGroup = isDefaultGroup
    )
  }
}
