package com.github.naz013.digestapi

/**
 * Facade over the background scheduling of the daily digest, so callers (e.g. the Digest settings
 * screen) never need a direct dependency on `work-api`.
 */
interface DigestScheduler {
  /** Enqueues the periodic daily-digest check, e.g. when the user turns the feature on. */
  fun enable()

  /** Cancels the periodic check. Already-posted notifications are left in place. */
  fun disable()
}
