package com.github.naz013.sync

/**
 * Answers whether the current user has an active Pro subscription/purchase - the seam
 * [SyncApiImpl] uses to gate upload/download of [com.github.naz013.files.DataType.isProOnly]
 * data types. Implemented in `app` against `BuildInfo.isPro`; `data:sync` stays free of any
 * Android/platform dependency, same as the [SyncSettings] seam.
 */
fun interface IsProUserUseCase {
  operator fun invoke(): Boolean
}
