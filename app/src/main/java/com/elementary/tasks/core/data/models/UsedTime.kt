package com.elementary.tasks.core.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UsedTime(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val timeString: String = "",
  val timeMills: Long = 0,
  val useCount: Int = 0
)
