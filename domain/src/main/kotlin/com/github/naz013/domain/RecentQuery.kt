package com.github.naz013.domain

import org.threeten.bp.LocalDateTime

data class RecentQuery(
  val queryType: RecentQueryType,
  val queryText: String,
  val lastUsedAt: LocalDateTime = LocalDateTime.now(),
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
