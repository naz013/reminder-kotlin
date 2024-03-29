package com.backdoor.engine.lang

import com.backdoor.engine.Recognizer
import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import com.backdoor.engine.misc.ContactsInterface
import com.backdoor.engine.misc.LongInternal
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

internal abstract class Worker(
  protected val zoneId: ZoneId,
  protected val contactsInterface: ContactsInterface?
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
  open fun hasConnectSpecialWord(input: String): Boolean {
    return false
  }

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
              clearArticleForMulti(it, index)
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

  protected open fun clearArticleForMulti(it: MutableList<String>, index: Int) {
  }

  private fun getMulti(input: String?) = when {
    hasSeconds(input) -> SECOND.toFloat()
    hasMinutes(input) != -1 -> MINUTE.toFloat()
    hasHours(input) != -1 -> HOUR.toFloat()
    hasWeeks(input) -> (7 * DAY).toFloat()
    hasDays(" $input") -> DAY.toFloat()
    hasMonth(input) -> (30 * DAY).toFloat()
    else -> -1f
  }

  override fun splitWords(input: String?): String {
    return input ?: ""
  }

  override fun replaceNumbers(input: String?): String? {
    val parts = input?.splitByWhitespaces()?.toMutableList() ?: mutableListOf()

    var allNumber = 0f
    var beginIndex = -1

    for (i in parts.indices) {
      val part = parts[i]
      var number = findNumber(part)
      if (number != -1f) {
        allNumber += number
        parts[i] = ""
        if (beginIndex == -1) {
          beginIndex = i
        }
      } else {
        number = findFloat(part)
        if (number != -1f) {
          parts[i] = ""
          allNumber += number
          if (beginIndex == -1) {
            beginIndex = i
          }
        } else if (beginIndex != -1 &&
          (hasHours(part) != -1 || hasMinutes(part) != -1 || hasConnectSpecialWord(part))
        ) {
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
    var reserveHour = -1f
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
            m = parts[i + 1].toFloat()
            parts[i + 1] = ""
          }
        }
      }
      if (minutesIndex != -1) {
        m = ignoreAny({
          parts[i - minutesIndex].toFloat()
        }) { 0f }
      }
      ignoreAny {
        reserveHour = parts[i].toFloat()
      }
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
    if (reserveHour != -1f) {
      localTime = LocalTime.now(zoneId)
        .withHour(reserveHour.toInt())
        .withMinute(0)
        .withSecond(0)

      if (ampm == Ampm.EVENING) {
        localTime = localTime?.plusHours(12)
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

  override fun findSenderAndClear(
    input: String,
    action: Action,
    result: (String) -> Unit
  ): String {
    if (action != Action.CALL && action != Action.MESSAGE && action != Action.MAIL) {
      return input
    }
    return input.splitByWhitespaces().toMutableList().also { list ->
      list.forEachIndexed { index, s ->
        when (action) {
          Action.CALL -> {
            if (hasCall(s)) {
              val phoneNumber = crawlForward(list, index + 1, 2, true, {
                findPhoneNumber(it)
              }) {
                list[it] = ""
              }
              if (phoneNumber != null) {
                result(phoneNumber)
              }
              return@forEachIndexed
            }
          }

          Action.MESSAGE -> {
            if (getMessageType(s) != null) {
              val phoneNumber = crawlForward(list, index + 1, 2, true, {
                findPhoneNumber(it)
              }) {
                list[it] = ""
              }
              if (phoneNumber != null) {
                result(phoneNumber)
              }
              return@forEachIndexed
            }
          }

          else -> {
            if (getMessageType(s) != null) {
              val email = crawlForward(list, index + 1, 2, true, {
                findEmail(it)
              }) {
                list[it] = ""
              }
              if (email != null) {
                result(email)
              }
              return@forEachIndexed
            }
          }
        }
      }
    }.clip()
  }

  protected fun findPhoneNumber(input: String): String? {
    return contactsInterface?.findNumber(input)
  }

  protected fun findEmail(input: String): String? {
    return contactsInterface?.findEmail(input)
  }

  protected fun <T> crawlBackward(
    list: MutableList<String>,
    index: Int,
    numberOfSteps: Int,
    clear: Boolean,
    transform: (String) -> T?,
    onClear: (Int) -> Unit
  ): T? {
    var t: T? = null
    for (i in index downTo index - numberOfSteps + 1) {
      if (i >= 0) {
        t = ignoreAny({ transform.invoke(list[i]) }) { null }
        if (t != null) {
          if (clear) {
            onClear.invoke(i)
          }
          break
        }
      } else {
        break
      }
    }
    return t
  }

  protected fun <T> crawlForward(
    list: MutableList<String>,
    index: Int,
    numberOfSteps: Int,
    clear: Boolean,
    transform: (String) -> T?,
    onClear: (Int) -> Unit
  ): T? {
    var t: T? = null
    for (i in index until index + numberOfSteps) {
      if (i < list.size) {
        t = ignoreAny({ transform.invoke(list[i]) }) { null }
        if (t != null) {
          if (clear) {
            onClear.invoke(i)
          }
          break
        }
      } else {
        break
      }
    }
    return t
  }

  protected fun clearAllBackward(
    list: MutableList<String>,
    index: Int,
    numberOfSteps: Int,
    vararg matchers: String
  ) {
    for (i in index downTo index - numberOfSteps + 1) {
      if (i >= 0) {
        val s = list[i]
        val areAnyMatches = matchers.any { s.matches(it) }
        if (areAnyMatches) {
          list[i] = ""
        }
      } else {
        break
      }
    }
  }

  protected fun clearAllForward(
    list: MutableList<String>,
    index: Int,
    numberOfSteps: Int,
    vararg matchers: String
  ) {
    for (i in index until index + numberOfSteps) {
      if (i < list.size) {
        val s = list[i]
        if (matchers.any { s.matches(it) }) {
          list[i] = ""
        }
      } else {
        break
      }
    }
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
