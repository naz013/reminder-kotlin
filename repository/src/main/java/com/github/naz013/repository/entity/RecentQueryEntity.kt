package com.github.naz013.repository.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.github.naz013.domain.RecentQuery
import com.github.naz013.domain.RecentQueryTarget
import com.github.naz013.domain.RecentQueryType
import com.github.naz013.repository.converters.DateTimeTypeConverter
import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDateTime

@Entity(tableName = "RecentQuery")
@Keep
@TypeConverters(DateTimeTypeConverter::class)
internal data class RecentQueryEntity(
  @SerializedName("queryType")
  val queryType: RecentQueryType,
  @SerializedName("queryText")
  val queryText: String,
  @SerializedName("lastUsedAt")
  val lastUsedAt: LocalDateTime = LocalDateTime.now(),
  @SerializedName("id")
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  @SerializedName("targetId")
  val targetId: String? = null,
  @SerializedName("targetType")
  val targetType: RecentQueryTarget? = null
) {

  constructor(recentQuery: RecentQuery) : this(
    queryType = recentQuery.queryType,
    queryText = recentQuery.queryText,
    lastUsedAt = recentQuery.lastUsedAt,
    id = recentQuery.id,
    targetId = recentQuery.targetId,
    targetType = recentQuery.targetType
  )

  fun toDomain(): RecentQuery {
    return RecentQuery(
      queryType = queryType,
      queryText = queryText,
      lastUsedAt = lastUsedAt,
      id = id,
      targetId = targetId,
      targetType = targetType
    )
  }
}
