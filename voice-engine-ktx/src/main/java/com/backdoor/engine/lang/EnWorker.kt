package com.backdoor.engine.lang

import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import java.util.*
import java.util.regex.Pattern

internal class EnWorker : Worker() {
  override val weekdays = listOf(
    "sunday",
    "monday",
    "tuesday",
    "wednesday",
    "thursday",
    "friday",
    "saturday"
  )

  override fun hasCalendar(input: String) = input.matches(".*calendar.*")

  override fun clearCalendar(input: String): String? {
    return input.split(WHITESPACES).toMutableList().let {
      it.forEachIndexed { index, s ->
        if (s.matches(".*calendar.*")) {
          it[index] = ""
          return@forEachIndexed
        }
      }
      clipStrings(it)
    }
  }

  override fun getWeekDays(input: String) =
    input.split(WHITESPACES).forEach { part ->
      weekdays.forEachIndexed { index, day ->
        if (part.matches(".*$day.*")) {
          weekdayArray[index] = 1
          return@forEachIndexed
        }
      }
    }.let { weekdayArray.toList() }

  override fun clearWeekDays(input: String): String {
    val sb = StringBuilder()
    return input.split(WHITESPACES).toMutableList().let {
      it.forEachIndexed { index, s ->
        for (day in weekdays) {
          if (s.matches(".*$day.*")) {
            it[index] = ""
            break
          }
        }
      }
      it
    }.let {
      clipStrings(it)
    }.split(WHITESPACES).forEach { s ->
      val part = s.trim { it <= ' ' }
      if (!part.matches("on") && !part.matches("at")) sb.append(" ").append(part)
    }.let {
      sb.toString().trim { it <= ' ' }
    }
  }

  override fun getDaysRepeat(input: String): Long {
    val parts: Array<String> = input.split(WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      if (hasDays(part)) {
        val integer: Int
        integer = try {
          parts[i - 1].toInt()
        } catch (e: NumberFormatException) {
          1
        } catch (e: ArrayIndexOutOfBoundsException) {
          0
        }
        return integer * DAY
      }
    }
    return 0
  }

  override fun clearDaysRepeat(input: String): String? {
    val parts: Array<String?> = input.split(WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      if (hasDays(part)) {
        try {
          parts[i - 1]!!.toInt()
          parts[i - 1] = ""
        } catch (ignored: NumberFormatException) {
        }
        parts[i] = ""
        break
      }
    }
    return clipStrings(parts)
  }

  override fun hasRepeat(input: String?): Boolean {
    return input!!.matches(".*every.*") || hasEveryDay(input)
  }

  override fun hasEveryDay(input: String?): Boolean {
    return input!!.matches(".*everyday.*")
  }

  override fun clearRepeat(input: String): String? {
    val parts: Array<String?> = input.split(WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      if (hasRepeat(part)) {
        parts[i] = ""
        break
      }
    }
    return clipStrings(parts)
  }

  override fun hasTomorrow(input: String): Boolean {
    return input.matches(".*tomorrow.*") || input.matches(".*next day.*")
  }

  override fun clearTomorrow(input: String): String? {
    val parts: Array<String?> = input.split(WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      if (part!!.matches(".*tomorrow.*") || part.matches(".*next day.*")) {
        parts[i] = ""
        break
      }
    }
    return clipStrings(parts)
  }

  override fun getMessage(input: String): String {
    val parts: Array<String> = input.split(WHITESPACES).toTypedArray()
    val sb = StringBuilder()
    var isStart = false
    for (part in parts) {
      if (isStart) sb.append(" ").append(part)
      if (part.matches("text")) isStart = true
    }
    return sb.toString().trim { it <= ' ' }
  }

  override fun clearMessage(input: String): String? {
    var input = input
    val parts: Array<String?> = input.split(WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      if (part!!.matches("text")) {
        try {
          if (parts[i - 1]!!.matches("with")) {
            parts[i - 1] = ""
          }
        } catch (ignored: IndexOutOfBoundsException) {
        }
        input = input.replace(part, "")
        parts[i] = ""
      }
    }
    return clipStrings(parts)
  }

  override fun getMessageType(input: String?): Action? {
    if (input!!.matches(".*message.*")) return Action.MESSAGE else if (input.matches(".*letter.*")) return Action.MAIL
    return null
  }

  override fun clearMessageType(input: String): String? {
    val parts: Array<String?> = input.split(WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      val type = getMessageType(part)
      if (type != null) {
        parts[i] = ""
        val nextIndex = i + 1
        if (nextIndex < parts.size && parts[nextIndex]!!.matches("to")) {
          parts[nextIndex] = ""
        }
        break
      }
    }
    return clipStrings(parts)
  }

  override fun getAmpm(input: String?): Ampm? {
    if (input!!.matches(".*morning.*")) return Ampm.MORNING else if (input.matches(".*evening.*")) return Ampm.EVENING else if (input.matches(".*noon.*")) return Ampm.NOON else if (input.matches(".*night.*")) return Ampm.NIGHT else if (input.matches(".*a m.*")) return Ampm.MORNING else if (input.matches(".*a.m..*")) return Ampm.MORNING else if (input.matches(".*am.*")) return Ampm.MORNING else if (input.matches(".*p m.*")) return Ampm.EVENING else if (input.matches(".*p.m..*")) return Ampm.EVENING else if (input.matches(".*pm.*")) return Ampm.EVENING
    return null
  }

  override fun clearAmpm(input: String): String? {
    val parts: Array<String?> = input.split(WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      if (getAmpm(part) != null) {
        parts[i] = ""
        break
      }
    }
    return clipStrings(parts)
  }

  override fun getShortTime(input: String?): Date? {
    val pattern = Pattern.compile("([01]?[0-9]|2[0-3])( |:)[0-5][0-9]")
    val matcher = pattern.matcher(input)
    if (matcher.find()) {
      val time = matcher.group().trim { it <= ' ' }
      for (format in hourFormats) {
        var date: Date?
        try {
          date = format!!.parse(time)
          if (date != null) return date
        } catch (ignored: Exception) {
        }
      }
    }
    return null
  }

  override fun clearTime(input: String?): String {
    var input = input
    var parts: Array<String?> = input!!.split(WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      if (hasHours(part) != -1) {
        val index = hasHours(part)
        parts[i] = ""
        try {
          parts[i - index]!!.toInt()
          parts[i - index] = ""
        } catch (ignored: Exception) {
        }
      }
      if (hasMinutes(part) != -1) {
        val index = hasMinutes(part)
        try {
          parts[i - index]!!.toInt()
          parts[i - index] = ""
        } catch (ignored: Exception) {
        }
        parts[i] = ""
      }
    }
    val pattern = Pattern.compile("([01]?[0-9]|2[0-3])( |:)[0-5][0-9]")
    input = clipStrings(parts)
    val matcher = pattern.matcher(input)
    if (matcher.find()) {
      val time = matcher.group().trim { it <= ' ' }
      input = input.replace(time, "")
    }
    parts = input.split(WHITESPACES).toTypedArray()
    val sb = StringBuilder()
    for (i in parts.indices) {
      val part = parts[i]!!.trim { it <= ' ' }
      if (!part.matches("at")) sb.append(" ").append(part)
    }
    return sb.toString().trim { it <= ' ' }
  }

  override fun getMonth(input: String?): Int {
    var res = -1
    if (input!!.contains("january")) res = 0 else if (input.contains("february")) res = 1 else if (input.contains("march")) res = 2 else if (input.contains("april")) res = 3 else if (input.contains("may")) res = 4 else if (input.contains("june")) res = 5 else if (input.contains("july")) res = 6 else if (input.contains("august")) res = 7 else if (input.contains("september")) res = 8 else if (input.contains("october")) res = 9 else if (input.contains("november")) res = 10 else if (input.contains("december")) res = 11
    return res
  }

  override fun hasCall(input: String?): Boolean {
    return input!!.matches(".*call.*")
  }

  override fun clearCall(input: String): String? {
    val parts: Array<String?> = input.split(WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      if (hasCall(part)) {
        parts[i] = ""
        break
      }
    }
    return clipStrings(parts)
  }

  override fun isTimer(input: String?): Boolean {
    var input = input
    input = " $input "
    return input.matches(".*after.*") || input.matches(".* in .*")
  }

  override fun cleanTimer(input: String): String? {
    val parts: Array<String?> = input.split(WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val string = parts[i]
      if (isTimer(string)) {
        parts[i] = ""
        break
      }
    }
    return clipStrings(parts)
  }

  override fun getDate(input: String, res: com.backdoor.engine.misc.LongInternal): String? {
    var mills: Long = 0
    val parts: Array<String?> = input.split(WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      val month = getMonth(part)
      if (month != -1) {
        var integer: Int
        try {
          integer = parts[i + 1]!!.toInt()
          parts[i + 1] = ""
        } catch (e: NumberFormatException) {
          integer = 1
        } catch (e: IndexOutOfBoundsException) {
          integer = 1
        }
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar[Calendar.MONTH] = month
        calendar[Calendar.DAY_OF_MONTH] = integer
        mills = calendar.timeInMillis
        parts[i] = ""
        break
      }
    }
    res.set(mills)
    return clipStrings(parts)
  }

  override fun hasSender(input: String?): Boolean {
    return input!!.matches(".*send.*")
  }

  override fun clearSender(input: String): String? {
    val parts: Array<String?> = input.split(WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val string = parts[i]
      if (hasSender(string)) {
        parts[i] = ""
        break
      }
    }
    return clipStrings(parts)
  }

  override fun hasNote(input: String): Boolean {
    return input.contains("note")
  }

  override fun clearNote(input: String): String {
    var input = input
    input = input.replace("note", "")
    return input.trim { it <= ' ' }
  }

  override fun hasAction(input: String): Boolean {
    return (input.startsWith("open")
      || input.matches(".*help.*")
      || input.matches(".*adjust.*")
      || input.matches(".*report.*")
      || input.matches(".*change.*"))
  }

  override fun getAction(input: String): Action? {
    return if (input.matches(".*help.*")) {
      Action.HELP
    } else if (input.matches(".*loudness.*") || input.matches(".*volume.*")) {
      Action.VOLUME
    } else if (input.matches(".*settings.*")) {
      Action.SETTINGS
    } else if (input.matches(".*report.*")) {
      Action.REPORT
    } else {
      Action.APP
    }
  }

  override fun hasEvent(input: String): Boolean {
    return input.startsWith("new") || input.startsWith("add") || input.startsWith("create")
  }

  override fun getEvent(input: String): Action? {
    return if (input.matches(".*birthday.*")) {
      Action.BIRTHDAY
    } else if (input.matches(".*reminder.*")) {
      Action.REMINDER
    } else Action.NO_EVENT
  }

  override fun hasEmptyTrash(input: String): Boolean {
    return input.matches(".*empty trash.*")
  }

  override fun hasDisableReminders(input: String): Boolean {
    return input.matches(".*disable reminder.*")
  }

  override fun hasGroup(input: String): Boolean {
    return input.matches(".*add group.*")
  }

  override fun clearGroup(input: String): String {
    val sb = StringBuilder()
    val parts: Array<String> = input.split(WHITESPACES).toTypedArray()
    var st = false
    for (s in parts) {
      if (s.matches(".*group.*")) {
        st = true
        continue
      }
      if (st) {
        sb.append(s)
        sb.append(" ")
      }
    }
    return sb.toString().trim { it <= ' ' }
  }

  override fun hasToday(input: String): Boolean {
    return input.matches(".*today.*")
  }

  override fun hasAfterTomorrow(input: String): Boolean {
    return input.matches(".*after tomorrow.*")
  }

  protected override val afterTomorrow: String
    protected get() = "after tomorrow"

  override fun hasHours(input: String?): Int {
    return if (input!!.matches(".*hour.*") || input.matches(".*o'clock.*")
      || input.matches(".*am.*") || input.matches(".*pm.*")) 1 else -1
  }

  override fun hasMinutes(input: String?): Int {
    return if (input!!.matches(".*minute.*")) 1 else -1
  }

  override fun hasSeconds(input: String?): Boolean {
    return input!!.matches(".*second.*")
  }

  override fun hasDays(input: String?): Boolean {
    return input!!.matches(".* day.*")
  }

  override fun hasWeeks(input: String?): Boolean {
    return input!!.matches(".*week.*")
  }

  override fun hasMonth(input: String?): Boolean {
    return input!!.matches(".*month.*")
  }

  override fun hasAnswer(input: String): Boolean {
    var input = input
    input = " $input "
    return input.matches(".* (yes|yeah|no) .*")
  }

  override fun getAnswer(input: String): Action? {
    return if (input.matches(".* ?(yes|yeah) ?.*")) {
      Action.YES
    } else Action.NO
  }

  override fun findFloat(input: String?): Float {
    return if (input!!.matches("half")) {
      0.5f
    } else {
      (-1).toFloat()
    }
  }

  override fun clearFloats(input: String?): String? {
    if (input!!.contains("and a half")) {
      return input.replace("and a half", "")
    }
    if (input.contains("and half")) {
      return input.replace("and half", "")
    }
    if (input.contains(" half an ")) {
      return input.replace("half an", "")
    }
    return if (input.contains(" in half ")) {
      input.replace("in half", "")
    } else input
  }

  override fun findNumber(input: String?): Float {
    var number = -1f
    if (input!!.matches("zero") || input.matches("nil")) number = 0f
    if (input.matches("one") || input.matches("first")) number = 1f
    if (input.matches("two") || input.matches("second")) number = 2f
    if (input.matches("three") || input.matches("third")) number = 3f
    if (input.matches("four") || input.matches("fourth")) number = 4f
    if (input.matches("five") || input.matches("fifth")) number = 5f
    if (input.matches("six") || input.matches("sixth")) number = 6f
    if (input.matches("seven") || input.matches("seventh")) number = 7f
    if (input.matches("eight") || input.matches("eighth")) number = 8f
    if (input.matches("nine") || input.matches("ninth")) number = 9f
    if (input.matches("ten") || input.matches("tenth")) number = 10f
    if (input.matches("eleven") || input.matches("eleventh")) number = 11f
    if (input.matches("twelve") || input.matches("twelfth")) number = 12f
    if (input.matches("thirteen") || input.matches("thirteenth")) number = 13f
    if (input.matches("fourteen") || input.matches("fourteenth")) number = 14f
    if (input.matches("fifteen") || input.matches("fifteenth")) number = 15f
    if (input.matches("sixteen") || input.matches("sixteenth")) number = 16f
    if (input.matches("seventeen") || input.matches("seventeenth")) number = 17f
    if (input.matches("eighteen") || input.matches("eighteenth")) number = 18f
    if (input.matches("nineteen") || input.matches("nineteenth")) number = 19f
    if (input.matches("twenty") || input.matches("twentieth")) number = 20f
    if (input.matches("thirty") || input.matches("thirtieth")) number = 30f
    if (input.matches("forty") || input.matches("fortieth")) number = 40f
    if (input.matches("fifty") || input.matches("fiftieth")) number = 50f
    if (input.matches("sixty") || input.matches("sixtieth")) number = 60f
    if (input.matches("seventy") || input.matches("seventieth")) number = 70f
    if (input.matches("eighty") || input.matches("eightieth")) number = 80f
    if (input.matches("ninety") || input.matches("ninetieth")) number = 90f
    return number
  }

  override fun hasShowAction(input: String): Boolean {
    return input.matches(".*show.*")
  }

  override fun getShowAction(input: String): Action? {
    if (input.matches(".*birthdays.*")) {
      return Action.BIRTHDAYS
    } else if (input.matches(".*active reminders.*")) {
      return Action.ACTIVE_REMINDERS
    } else if (input.matches(".*reminders.*")) {
      return Action.REMINDERS
    } else if (input.matches(".*events.*")) {
      return Action.EVENTS
    } else if (input.matches(".*notes.*")) {
      return Action.NOTES
    } else if (input.matches(".*groups.*")) {
      return Action.GROUPS
    } else if (input.matches(".*shopping lists?.*")) {
      return Action.SHOP_LISTS
    }
    return null
  }

  override fun hasNextModifier(input: String): Boolean {
    return input.matches(".*next.*")
  }
}