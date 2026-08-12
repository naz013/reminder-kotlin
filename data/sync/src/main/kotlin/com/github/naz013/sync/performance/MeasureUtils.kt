package com.github.naz013.sync.performance

import com.github.naz013.logging.Logger

internal suspend fun <T> measure(name: String, block: suspend () -> T): T {
  val startTime = System.currentTimeMillis()
  val result = block()
  val duration = System.currentTimeMillis() - startTime
  Logger.i("Performance", "$name took ${formatDuration(duration)}")
  return result
}

@Suppress("DefaultLocale")
private fun formatDuration(durationMs: Long): String {
  val seconds = (durationMs / 1000) % 60
  val minutes = (durationMs / (1000 * 60)) % 60
  val hours = (durationMs / (1000 * 60 * 60)) % 24
  return if (hours > 0) {
    String.format("%02d:%02d:%02d", hours, minutes, seconds)
  } else {
    String.format("%02d:%02d", minutes, seconds)
  }
}
