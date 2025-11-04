package com.github.naz013.domain.sync

enum class SyncState {
  WaitingForUpload,
  Uploading,
  Synced,
  FailedToUpload,
}
