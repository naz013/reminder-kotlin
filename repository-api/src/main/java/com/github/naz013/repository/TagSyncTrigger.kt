package com.github.naz013.repository

/**
 * Notifies `app` that a tag was saved/deleted so it can schedule a cloud-sync upload/delete for
 * it - [TagRepositoryImpl][com.github.naz013.repository.impl] cannot call `app`'s WorkManager
 * scheduling directly (wrong dependency direction), so this interface is defined here and its only
 * real implementation lives in `app`.
 */
interface TagSyncTrigger {
  fun onTagSaved(id: String)

  fun onTagDeleted(id: String)
}
