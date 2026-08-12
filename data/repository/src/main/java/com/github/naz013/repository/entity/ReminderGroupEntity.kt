package com.github.naz013.repository.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.sync.SyncState
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
  val isDefaultGroup: Boolean,
  @SerializedName("version")
  val version: Long = 0L,
  @SerializedName("syncState")
  val syncState: String
) {

  constructor(reminderGroup: ReminderGroup) : this(
    groupTitle = reminderGroup.groupTitle,
    groupUuId = reminderGroup.groupUuId,
    groupColor = reminderGroup.groupColor,
    groupDateTime = reminderGroup.groupDateTime,
    isDefaultGroup = reminderGroup.isDefaultGroup,
    version = reminderGroup.version,
    syncState = reminderGroup.syncState.name
  )

  fun toDomain(): ReminderGroup {
    return ReminderGroup(
      groupTitle = groupTitle,
      groupUuId = groupUuId,
      groupColor = groupColor,
      groupDateTime = groupDateTime,
      isDefaultGroup = isDefaultGroup,
      version = version,
      syncState = SyncState.valueOf(syncState)
    )
  }
}
