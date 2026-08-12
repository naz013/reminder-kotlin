package com.github.naz013.domain.reminder.v2

import com.google.gson.annotations.SerializedName

/** Gson round-tripped directly as a Room column (see `ReminderV2BuilderSchemeConverter`), so
 * every field needs [SerializedName] - see [RecurrenceRule] for why an unannotated field is a
 * production-crash risk under R8. */
data class BuilderSchemeItemV2(
  @SerializedName("type")
  val type: Int,
  @SerializedName("position")
  val position: Int
)
