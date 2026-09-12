package com.github.naz013.wearsync

/** Seam other modules push glanceable reminder data through, without depending on the Wearable
 * Data Layer transport that [com.github.naz013.wearsync] implements it with. */
interface WearSyncGateway {
  suspend fun pushReminders(reminders: List<WearReminderSummary>)
}
