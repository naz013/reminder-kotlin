package com.github.naz013.digestapi

/**
 * Answers "can this device produce an on-device AI digest right now" - hardware/model support
 * (ML Kit GenAI Summarization) AND device language support combined into a single boolean, since
 * that's all any consumer of this interface (Settings visibility, the background task) actually
 * needs. There is deliberately no cloud fallback behind a `false` result - see
 * `research/AI_DAILY_DIGEST_PLAN.md`.
 *
 * The real check is asynchronous, but Settings state in this codebase is built synchronously, so
 * this interface also owns a cache: [isDeviceCapableCached] is read for the first render,
 * [refreshCapability] is kicked alongside it to bring the cache up to date.
 */
interface DigestCapabilityChecker {
  /** Last-known result, synchronous. Defaults to `false` until [refreshCapability] has run once. */
  fun isDeviceCapableCached(): Boolean

  /** Performs the real check, updates the cache, and returns the fresh result. */
  suspend fun refreshCapability(): Boolean
}
