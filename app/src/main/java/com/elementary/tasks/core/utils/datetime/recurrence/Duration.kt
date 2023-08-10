package com.elementary.tasks.core.utils.datetime.recurrence

data class Duration(
  private val value: String // Format P15DT5H0M20S
) : Buildable {

  var weeks: Int = 0
    private set
  var days: Int = 0
    private set
  var hours: Int = 0
    private set
  var minutes: Int = 0
    private set
  var seconds: Int = 0
    private set

  init {
    parse()
  }

  constructor(
    weeks: Int = 0,
    days: Int = 0,
    hours: Int = 0,
    minutes: Int = 0,
    seconds: Int = 0
  ) : this("") {
    this.weeks = weeks
    this.days = days
    this.hours = hours
    this.minutes = minutes
    this.seconds = seconds
  }

  fun update(
    weeks: Int? = null,
    days: Int? = null,
    hours: Int? = null,
    minutes: Int? = null,
    seconds: Int? = null
  ): Duration {
    if (weeks != null) {
      this.weeks = weeks
    }
    if (days != null) {
      this.days = days
    }
    if (hours != null) {
      this.hours = hours
    }
    if (minutes != null) {
      this.minutes = minutes
    }
    if (seconds != null) {
      this.seconds = seconds
    }
    return this
  }

  override fun buildString(): String {
    return if (isAllZero()) {
      NO_DURATION
    } else {
      val builder = StringBuilder()
      if (weeks != 0) {
        builder.append(weeks).append(WEEKS)
      }
      if (days != 0) {
        builder.append(days).append(DAYS)
      }
      if (hasTime()) {
        builder.append(TIME)
        if (hours != 0) {
          builder.append(hours).append(HOURS)
        }
        if (minutes != 0) {
          builder.append(minutes).append(MINUTES)
        }
        if (seconds != 0) {
          builder.append(seconds).append(SECONDS)
        }
      }
      "$START$builder"
    }
  }

  private fun hasTime(): Boolean {
    return hours != 0 || minutes != 0 || seconds != 0
  }

  private fun isAllZero(): Boolean {
    return weeks == 0 && days == 0 && hours == 0 && minutes == 0 && seconds == 0
  }

  private fun parse() {
    if (value == NO_DURATION) return
    if (!value.startsWith(START)) return

    val numberBuilder = StringBuilder()
    for (char in value.toCharArray()) {
      if (char.isDigit()) {
        // Append to number builder

        numberBuilder.append(char)
      } else {
        // Try to find modifier

        val number = runCatching { numberBuilder.toString().toInt() }.getOrNull()
        if (number != null && number > 0) {
          when (char.toString()) {
            WEEKS -> weeks = number
            DAYS -> days = number
            HOURS -> hours = number
            MINUTES -> minutes = number
            SECONDS -> seconds = number
            TIME -> {
              numberBuilder.clear()
            }
          }
        }

        numberBuilder.clear()
      }
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Duration

    if (weeks != other.weeks) return false
    if (days != other.days) return false
    if (hours != other.hours) return false
    if (minutes != other.minutes) return false
    return seconds == other.seconds
  }

  override fun hashCode(): Int {
    var result = weeks
    result = 31 * result + days
    result = 31 * result + hours
    result = 31 * result + minutes
    result = 31 * result + seconds
    return result
  }

  companion object {
    private const val NO_DURATION = ""
    private const val START = "P"

    private const val WEEKS = "W"
    private const val DAYS = "D"

    private const val TIME = "T"

    private const val HOURS = "H"
    private const val MINUTES = "M"
    private const val SECONDS = "S"
  }
}
