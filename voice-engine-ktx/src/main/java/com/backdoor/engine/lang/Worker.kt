package com.backdoor.engine.lang

import com.backdoor.engine.Recognizer
import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import com.backdoor.engine.misc.LongInternal
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

internal abstract class Worker : WorkerInterface {
  internal val weekdayArray = intArrayOf(0, 0, 0, 0, 0, 0, 0)
  internal val hourFormats = listOf(
    SimpleDateFormat("HH mm", Recognizer.LOCALE),
    SimpleDateFormat("HH:mm", Recognizer.LOCALE)
  )
  internal val hourFormat = SimpleDateFormat("HH:mm", Recognizer.LOCALE)

  internal fun isCorrectTime(hourOfDay: Int, minuteOfHour: Int): Boolean {
    return hourOfDay < 24 && minuteOfHour < 60
  }

  internal fun isLeapYear(year: Int): Boolean {
    return year % 4 == 0 && year % 100 != 0 ||
      year % 4 == 0 && year % 100 == 0 && year % 400 == 0
  }

  protected abstract val weekdays: List<String>
  protected abstract fun findNumber(input: String?): Float
  protected abstract fun hasHours(input: String?): Int
  protected abstract fun hasMinutes(input: String?): Int
  protected abstract fun hasSeconds(input: String?): Boolean
  protected abstract fun hasDays(input: String?): Boolean
  protected abstract fun hasWeeks(input: String?): Boolean
  protected abstract fun hasMonth(input: String?): Boolean

  protected abstract fun getMonth(input: String?): Int

  override fun getMultiplier(input: String, res: LongInternal): String {
    println("getMultiplier: $input")
    return input.splitByWhitespace().toMutableList().let {
      it.forEachIndexed lit@{ index, s ->
        try {
          var number = 0f
          try {
            number = s.toFloat()
            if (number != -1f) {
              it[index] = ""
            }
            for (j in index until it.size) {
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
      it
    }.clip().also {
      println("getMultiplier: out -> " + it + ", res -> " + res.value)
    }
  }

  private fun getMulti(input: String?) = when {
    hasSeconds(input) -> SECOND.toFloat()
    hasMinutes(input) != -1 -> MINUTE.toFloat()
    hasHours(input) != -1 -> HOUR.toFloat()
    hasDays(input) -> DAY.toFloat()
    hasWeeks(input) -> (7 * DAY).toFloat()
    hasMonth(input) -> (30 * DAY).toFloat()
    else -> (-1).toFloat()
  }

  override fun replaceNumbers(input: String?): String? {
    var parts = input?.split("\\s+")?.toMutableList() ?: mutableListOf()
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
          allNumber += number
          if (beginIndex == -1) {
            beginIndex = i
          }
        }
      }
    }
    println("replaceNumbers: parts -> $parts")
    if (beginIndex != -1 && allNumber != 0f) {
      val newP = arrayOfNulls<String>(parts.size + 1)
      for (i in parts.indices) {
        when {
          i > beginIndex -> {
            newP[i + 1] = parts[i]
          }
          i == beginIndex -> {
            newP[beginIndex] = allNumber.toString()
            newP[i + 1] = parts[i]
          }
          else -> {
            newP[i] = parts[i]
          }
        }
      }
      parts = newP.filterNotNull().toMutableList()
    }
    println("replaceNumbers: after parts -> $parts")
    var out: String? = clipStrings(parts)
    out = clearFloats(out)
    println("replaceNumbers: out -> $out")
    return out
  }

  protected abstract fun clearFloats(input: String?): String?

  protected abstract fun findFloat(input: String?): Float

  override fun getTime(input: String, ampm: Ampm?, times: List<String>): Long {
    println("getTime: $ampm, input $input")
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = 0
    val parts = input.split("\\s+").toTypedArray()
    var h = -1f
    var m = -1f
    var reserveHour = 0f
    for (i in parts.indices.reversed()) {
      val part = parts[i]
      val hoursIndex = hasHours(part)
      val minutesIndex = hasMinutes(part)
      if (hoursIndex != -1) {
        var integer: Float
        var hourSuccess = false
        try {
          integer = parts[i - hoursIndex].toFloat()
          hourSuccess = true
          parts[i - hoursIndex] = ""
        } catch (e: NumberFormatException) {
          integer = 1f
        } catch (e: ArrayIndexOutOfBoundsException) {
          integer = 1f
        }
        if (ampm === Ampm.EVENING) {
          integer += 12f
        }
        h = integer
        if (hourSuccess) {
          try {
            m = parts[i + 1].toInt().toFloat()
            parts[i + 1] = ""
          } catch (ignored: Exception) {
          }
        }
      }
      if (minutesIndex != -1) {
        val integer: Float = try {
          parts[i - minutesIndex].toFloat()
        } catch (e: NumberFormatException) {
          0f
        } catch (e: ArrayIndexOutOfBoundsException) {
          0f
        }
        m = integer
      }
      try {
        reserveHour = parts[i].toFloat()
      } catch (ignored: NumberFormatException) {
      }
    }
    val date = getShortTime(input)
    if (date != null) {
      calendar.time = date
      if (ampm === Ampm.EVENING) {
        val hour = calendar[Calendar.HOUR_OF_DAY]
        calendar[Calendar.HOUR_OF_DAY] = if (hour < 12) hour + 12 else hour
      }
      return calendar.timeInMillis
    }
    if (h != -1f) {
      calendar.timeInMillis = System.currentTimeMillis()
      calendar[Calendar.HOUR_OF_DAY] = h.toInt()
      if (m != -1f) {
        calendar[Calendar.MINUTE] = m.toInt()
      } else {
        calendar[Calendar.MINUTE] = 0
      }
      calendar[Calendar.SECOND] = 0
      calendar[Calendar.MILLISECOND] = 0
      return calendar.timeInMillis
    }
    if (calendar.timeInMillis == 0L && reserveHour != 0f) {
      calendar.timeInMillis = System.currentTimeMillis()
      calendar[Calendar.HOUR_OF_DAY] = reserveHour.toInt()
      calendar[Calendar.MINUTE] = 0
      calendar[Calendar.SECOND] = 0
      calendar[Calendar.MILLISECOND] = 0
      if (calendar.timeInMillis < System.currentTimeMillis()) {
        calendar.add(Calendar.DAY_OF_MONTH, 1)
      }
      if (ampm === Ampm.EVENING) {
        calendar.add(Calendar.HOUR_OF_DAY, 12)
      }
    }
    if (calendar.timeInMillis == 0L && ampm != null) {
      calendar.timeInMillis = System.currentTimeMillis()
      try {
        val hourFormat = hourFormat
        if (ampm === Ampm.MORNING) {
          calendar.time = hourFormat.parse(times[0])
        }
        if (ampm === Ampm.NOON) {
          calendar.time = hourFormat.parse(times[1])
        }
        if (ampm === Ampm.EVENING) {
          calendar.time = hourFormat.parse(times[2])
        }
        if (ampm === Ampm.NIGHT) {
          calendar.time = hourFormat.parse(times[3])
        }
      } catch (e: ParseException) {
        e.printStackTrace()
      }
    }
    return calendar.timeInMillis
  }

  protected abstract fun getShortTime(input: String?): Date?

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
      s.trim { it <= ' ' }
    } ?: input
  }

  override fun clearAfterTomorrow(input: String) = input.takeIf {
    hasAfterTomorrow(it)
  }?.replace(afterTomorrow, "") ?: input

  protected abstract val afterTomorrow: String

  abstract override fun String.toAmpm(): Ampm?
  abstract override fun String.toMessageType(): Action?
  abstract override fun String.hasRepeat(): Boolean
  abstract override fun String.hasCall(): Boolean
  abstract override fun String.isTimer(): Boolean
  abstract override fun String.hasSender(): Boolean
  abstract override fun String.hasEveryDay(): Boolean

  companion object {
    /**
     * Millisecond constants.
     */
    const val SECOND: Long = 1000
    const val MINUTE = 60 * SECOND
    const val HOUR = MINUTE * 60
    const val HALF_DAY = HOUR * 12
    const val DAY = HALF_DAY * 2
    const val WHITESPACES = "\\s+"
    const val WHITESPACE = "\\s"

    fun getNumberOfSelectedWeekdays(days: List<Int>) = days.count { it == 1 }

    fun getSelectedWeekday(days: List<Int>) = days.firstOrNull { it == 1 } ?: -1
  }
}