package com.elementary.tasks.core.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Deprecated("After S")
@Entity
data class MissedCall(
  @PrimaryKey
  var number: String = "",
  var dateTime: Long = 0,
  var uniqueId: Int = Random().nextInt(Integer.MAX_VALUE)
) : Serializable
