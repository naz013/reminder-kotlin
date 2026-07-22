package com.github.naz013.domain.reminder.v2

import org.threeten.bp.LocalDateTime
import java.util.UUID

data class ShopItemV2(
  val uuId: String = UUID.randomUUID().toString(),
  val summary: String = "",
  val isChecked: Boolean = false,
  val isDeleted: Boolean = false,
  val createdAt: LocalDateTime
)
