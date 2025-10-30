package com.github.naz013.sync

sealed class SyncResult {
  data class Success(
    val downloaded: List<Downloaded>,
    val success: Boolean,
  ) : SyncResult()
  data object Skipped : SyncResult()
}

data class Downloaded(
  val dataType: DataType,
  val id: String
)
