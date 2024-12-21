package com.github.naz013.repository.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.naz013.domain.UsedTime
import com.google.gson.annotations.SerializedName

@Entity(tableName = "UsedTime")
internal data class UsedTimeEntity(
  @SerializedName("id")
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  @SerializedName("timeString")
  val timeString: String = "",
  @SerializedName("timeMills")
  val timeMills: Long = 0,
  @SerializedName("useCount")
  val useCount: Int = 0
) {

  constructor(usedTime: UsedTime) : this(
    id = usedTime.id,
    timeString = usedTime.timeString,
    timeMills = usedTime.timeMills,
    useCount = usedTime.useCount
  )

  fun toDomain(): UsedTime {
    return UsedTime(
      id = id,
      timeString = timeString,
      timeMills = timeMills,
      useCount = useCount
    )
  }
}
