package com.github.naz013.domain.reminder.v2

import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDateTime
import java.util.UUID

/** Gson round-tripped directly as a Room column (see `ReminderV2ShopItemsConverter`), so every
 * field needs [SerializedName] - see [RecurrenceRule] for why an unannotated field is a
 * production-crash risk under R8. */
data class ShopItemV2(
  @SerializedName("uuId")
  val uuId: String = UUID.randomUUID().toString(),
  @SerializedName("summary")
  val summary: String = "",
  @SerializedName("isChecked")
  val isChecked: Boolean = false,
  @SerializedName("isDeleted")
  val isDeleted: Boolean = false,
  @SerializedName("createdAt")
  val createdAt: LocalDateTime
)
