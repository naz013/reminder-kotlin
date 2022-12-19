package com.backdoor.engine.lang

import com.backdoor.engine.Recognizer
import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import com.backdoor.engine.misc.LongInternal
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

internal abstract class Worker(
  protected val zoneId: ZoneId
) : WorkerInterface {
  internal val weekdayArray = intArrayOf(0, 0, 0, 0, 0, 0, 0)
  internal val hourFormats = listOf(
    DateTimeFormatter.ofPattern("HH mm", Recognizer.LOCALE),
    DateTimeFormatter.ofPattern("HH:mm", Recognizer.LOCALE),
    DateTimeFormatter.ofPattern("HH mm a", Recognizer.LOCALE),
    DateTimeFormatter.ofPattern("HH:mm a", Recognizer.LOCALE)
  )
  internal val hourFormat = DateTimeFormatter.ofPattern("HH:mm", Recognizer.LOCALE)

  fun <T> ignoreAny(f: () -> T): T? {
    return try {
      f.invoke()
    } catch (e: Exception) {
      null
    }
  }

  fun <T> ignoreAny(f: () -> T, f2: () -> T): T {
    return try {
      f.invoke()
    } catch (e: Exception) {
      f2.invoke()
    }
  }

  protected abstract val weekdays: List<String>
  abstract fun findNumber(input: String?): Float
  abstract fun hasHours(input: String?): Int
  abstract fun hasMinutes(input: String?): Int
  abstract fun hasSeconds(input: String?): Boolean
  abstract fun hasDays(input: String?): Boolean
  abstract fun hasWeeks(input: String?): Boolean
  abstract fun hasMonth(input: String?): Boolean
  abstract fun getMonth(input: String?): Int

  override fun getWeekDays(input: String) =
    input.splitByWhitespaces().forEach { part ->
      weekdays.forEachIndexed { index, day ->
        if (part.matches(".*$day.*")) {
          weekdayArray[index] = 1
          return@forEachIndexed
        }
      }
    }.let { weekdayArray.toList() }

  override fun getMultiplier(input: String, res: LongInternal): String {
    println("getMultiplier: $input")
    return input.splitByWhitespace().toMutableList().also {
      it.forEachIndexed lit@{ index, s ->
        try {
          var number = 0f
          try {
            number = s.toFloat()
            if (number != -1f) {
              it[index] = ""
            }
            for (j in index + 1 until it.size) {
              val multi = getMulti(it[j])
              if (multi != -1f) {
                number *= multi
                it[j] = ""
                break
              }
            }
          } catch (e: NumberFormatException) {
            val multi = getMulti(s)
            if (multi != -1f) {
              number += multi
              it[index] = ""
            } else {
              return@lit
            }
          }
          if (number > 0) {
            res.value = res.value + number.toLong()
          }
        } catch (e: Exception) {
        }
      }
    }.clip().also {
      println("getMultiplier: out -> " + it + ", res -> " + res.value)
    }
  }

  private fun getMulti(input: String?) = when {
    hasSeconds(input) -> SECOND.toFloat()
    hasMinutes(input) != -1 -> MINUTE.toFloat()
    hasHours(input) != -1 -> HOUR.toFloat()
    hasWeeks(input) -> (7 * DAY).toFloat()
    hasDays(input) -> DAY.toFloat()
    hasMonth(input) -> (30 * DAY).toFloat()
    else -> -1f
  }

  override fun replaceNumbers(input: String?): String? {
    val parts = input?.splitByWhitespaces()?.toMutableList() ?: mutableListOf()

    var allNumber = 0f
    var beginIndex = -1

    for (i in parts.indices) {
      var number = findNumber(parts[i])
      if (number != -1f) {
        allNumber += number
        parts[i] = ""
        if (beginIndex == -1) {
          beginIndex = i
        }
      } else {
        number = findFloat(parts[i])
        if (number != -1f) {
          parts[i] = ""
          allNumber += number
          if (beginIndex == -1) {
            beginIndex = i
          }
        } else if (beginIndex != -1 && (hasHours(parts[i]) != -1 || hasMinutes(parts[i]) != -1)) {
          parts[beginIndex] = allNumber.toString()
          allNumber = 0f
          beginIndex = -1
        }
      }
    }
    if (beginIndex != -1) {
      parts[beginIndex] = allNumber.toString()
    }
    println("replaceNumbers: parts -> $parts")
    return clearFloats(parts.clip()).also {
      println("replaceNumbers: out -> $it")
    }
  }

  protected abstract fun clearFloats(input: String?): String?

  protected abstract fun findFloat(input: String?): Float

  override fun getTime(input: String, ampm: Ampm?, times: List<String>): LocalTime? {
    println("getTime: $ampm, input $input")
    val parts = input.splitByWhitespaces().toTypedArray()
    var h = -1f
    var m = -1f
    var reserveHour = 0f
    for (i in parts.indices.reversed()) {
      val part = parts[i]
      val hoursIndex = hasHours(part)
      val minutesIndex = hasMinutes(part)
      if (hoursIndex != -1) {
        var integer = 1f
        var hourSuccess = false
        ignoreAny {
          integer = parts[i - hoursIndex].toFloat()
          hourSuccess = true
          parts[i - hoursIndex] = ""
        }
        if (ampm == Ampm.EVENING) {
          integer += 12f
        }
        h = integer
        if (hourSuccess) {
          ignoreAny {
            m = parts[i + 1].toInt().toFloat()
            parts[i + 1] = ""
          }
        }
      }
      if (minutesIndex != -1) {
        m = ignoreAny({
          parts[i - minutesIndex].toFloat()
        }) { 0f }
      }
      ignoreAny { reserveHour = parts[i].toFloat() }
    }
    var localTime: LocalTime? = null
    val parsedTime = getShortTime(input)

    if (parsedTime != null) {
      localTime = parsedTime
      if (ampm == Ampm.EVENING) {
        val hour = localTime.hour
        localTime = localTime.withHour(if (hour < 12) hour + 12 else hour)
      }
      return localTime
    }
    if (h != -1f) {
      localTime = LocalTime.now(zoneId)
      localTime = localTime.withHour(h.toInt())
      localTime = if (m != -1f) {
        localTime.withMinute(m.toInt())
      } else {
        localTime.withMinute(0)
      }
      return localTime.withSecond(0)
    }
    if (reserveHour != 0f) {
      localTime = LocalTime.now(zoneId)
        .withHour(reserveHour.toInt())
        .withMinute(0)
        .withSecond(0)

      if (ampm == Ampm.EVENING) {
        localTime = localTime?.withHour(12)
      }
    }
    if (localTime == null && ampm != null) {
      localTime = LocalTime.now(zoneId)
      ignoreAny {
        val hourFormat = hourFormat
        if (ampm == Ampm.MORNING) {
          localTime = LocalTime.parse(times[0], hourFormat)
        }
        if (ampm == Ampm.NOON) {
          localTime = LocalTime.parse(times[1], hourFormat)
        }
        if (ampm == Ampm.EVENING) {
          localTime = LocalTime.parse(times[2], hourFormat)
        }
        if (ampm == Ampm.NIGHT) {
          localTime = LocalTime.parse(times[3], hourFormat)
        }
      }
    }
    return localTime
  }

  protected abstract fun getShortTime(input: String?): LocalTime?

  override fun clearToday(input: String): String {
    return input.let { s ->
      s to s.splitByWhitespace().filter { hasToday(it) }
    }.takeIf {
      it.second.isNotEmpty()
    }?.let { pair ->
      var s = pair.first
      pair.second.forEach {
        s = s.replace(it, "")
      }
      s.trim()
    } ?: input
  }

  override fun clearAfterTomorrow(input: String) = input.takeIf {
    hasAfterTomorrow(it)
  }?.replace(afterTomorrow, "") ?: input

  override fun clearShowAction(input: String): String {
    return input
  }

  protected abstract val afterTomorrow: String

  abstract override fun hasCall(input: String): Boolean
  abstract override fun hasSender(input: String): Boolean
  abstract override fun hasRepeat(input: String): Boolean
  abstract override fun hasEveryDay(input: String): Boolean
  abstract override fun hasTimer(input: String): Boolean
  abstract override fun getMessageType(input: String): Action?
  abstract override fun getAmpm(input: String): Ampm?

  companion object {
    /**
     * Millisecond constants.
     */
    const val SECOND: Long = 1000
    const val MINUTE = 60 * SECOND
    const val HOUR = MINUTE * 60
    const val HALF_DAY = HOUR * 12
    const val DAY = HALF_DAY * 2
    val WHITESPACES = "\\s+".toRegex()
    val WHITESPACE = "\\s".toRegex()

    fun getNumberOfSelectedWeekdays(days: List<Int>) = days.count { it == 1 }
  }
}