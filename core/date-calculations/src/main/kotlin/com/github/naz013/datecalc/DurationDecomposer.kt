package com.github.naz013.datecalc

/**
 * Decomposes a millisecond duration into the largest whole unit that evenly divides it
 * (e.g. 172800000ms -> 2 DAY, not 2880 MINUTE).
 */
object DurationDecomposer {
  private const val SECOND: Long = 1000
  private const val MINUTE: Long = 60 * SECOND
  private const val HOUR: Long = MINUTE * 60
  private const val DAY: Long = HOUR * 24
  private const val WEEK: Long = DAY * 7
  private const val MONTH: Long = DAY * 30

  enum class Unit {
    SECOND,
    MINUTE,
    HOUR,
    DAY,
    WEEK,
    MONTH,
  }

  data class Duration(
    val value: Long,
    val unit: Unit,
  )

  fun decompose(millis: Long): Duration {
    if (millis == 0L) {
      return Duration(0, Unit.SECOND)
    }
    return when {
      millis % MONTH == 0L -> Duration(millis / MONTH, Unit.MONTH)
      millis % WEEK == 0L -> Duration(millis / WEEK, Unit.WEEK)
      millis % DAY == 0L -> Duration(millis / DAY, Unit.DAY)
      millis % HOUR == 0L -> Duration(millis / HOUR, Unit.HOUR)
      millis % MINUTE == 0L -> Duration(millis / MINUTE, Unit.MINUTE)
      millis % SECOND == 0L -> Duration(millis / SECOND, Unit.SECOND)
      else -> Duration(0, Unit.SECOND)
    }
  }
}
