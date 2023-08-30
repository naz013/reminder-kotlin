package com.elementary.tasks.core.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.elementary.tasks.core.data.converters.DateTimeTypeConverter
import com.google.errorprone.annotations.Keep
import org.threeten.bp.LocalDateTime

@Entity
@Keep
@TypeConverters(DateTimeTypeConverter::class)
data class RecentQuery(
  val queryType: RecentQueryType,
  val queryText: String,
  val lastUsedAt: LocalDateTime = LocalDateTime.now(),
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val targetId: String? = null,
  val targetType: RecentQueryTarget? = null
)

enum class RecentQueryTarget {
  REMINDER, NOTE, BIRTHDAY, PLACE, GOOGLE_TASK, GROUP, SCREEN, NONE
}

enum class RecentQueryType {
  TEXT, OBJECT
}
