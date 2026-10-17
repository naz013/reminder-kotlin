package com.github.naz013.feature.home.dayoverview

import org.threeten.bp.LocalDateTime

/** Half-open block of the day, in minutes since midnight. */
internal data class FreeBlock(
  val startMinute: Int,
  val endMinute: Int,
) {
  val durationMinutes: Int get() = endMinute - startMinute
}

internal data class FreeTime(
  val isFreeNow: Boolean,
  val freeUntilMinute: Int?,
  val largestBlock: FreeBlock?,
)

internal object DayOverviewCalculator {
  const val DAY_END_MINUTE = 24 * 60
  const val ITEM_DURATION_MINUTES = 30
  const val MIN_FREE_BLOCK_MINUTES = 30

  /** Each timed item occupies [ITEM_DURATION_MINUTES]; only the part after [nowMinute] counts. */
  fun calculateFreeTime(itemStartMinutes: List<Int>, nowMinute: Int): FreeTime {
    val busy = mergeBusy(itemStartMinutes, nowMinute)
    val gaps = mutableListOf<FreeBlock>()
    var cursor = nowMinute
    busy.forEach { (start, end) ->
      if (start > cursor) gaps.add(FreeBlock(cursor, start))
      cursor = maxOf(cursor, end)
    }
    if (cursor < DAY_END_MINUTE) gaps.add(FreeBlock(cursor, DAY_END_MINUTE))
    val usable = gaps.filter { it.durationMinutes >= MIN_FREE_BLOCK_MINUTES }
    val current = usable.firstOrNull()?.takeIf { it.startMinute == nowMinute }
    return FreeTime(
      isFreeNow = current != null,
      freeUntilMinute = current?.endMinute?.takeIf { it < DAY_END_MINUTE },
      largestBlock = usable.maxByOrNull { it.durationMinutes },
    )
  }

  /** Index of the first item that starts at or after [now], or null when none is left. */
  fun findNextIndex(sortedTimes: List<LocalDateTime>, now: LocalDateTime): Int? =
    sortedTimes.indexOfFirst { !it.isBefore(now) }.takeIf { it >= 0 }

  private fun mergeBusy(itemStartMinutes: List<Int>, nowMinute: Int): List<Pair<Int, Int>> {
    val intervals = itemStartMinutes
      .sorted()
      .map { it to minOf(it + ITEM_DURATION_MINUTES, DAY_END_MINUTE) }
      .filter { (_, end) -> end > nowMinute }
    val merged = mutableListOf<Pair<Int, Int>>()
    intervals.forEach { (start, end) ->
      val last = merged.lastOrNull()
      if (last != null && start <= last.second) {
        merged[merged.lastIndex] = last.first to maxOf(last.second, end)
      } else {
        merged.add(start to end)
      }
    }
    return merged
  }
}
