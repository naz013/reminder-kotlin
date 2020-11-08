package com.backdoor.engine.lang

import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import com.backdoor.engine.misc.LongInternal
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

  override fun clearCalendar(input: String) =
    input.splitByWhitespaces()
      .toMutableList()
      .let {
        it.forEachIndexed { index, s ->
          if (s.matches(".*calendar.*")) {
            it[index] = ""
            return@forEachIndexed
          }
        }
        it.clip()
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

  override fun clearWeekDays(input: String): String {
    val sb = StringBuilder()
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        for (day in weekdays) {
          if (s.matches(".*$day.*")) {
            it[index] = ""
            break
          }
        }
      }
    }.clip().splitByWhitespaces().forEach { s ->
      val part = s.trim()
      if (!part.matches("on") && !part.matches("at")) sb.append(" ").append(part)
    }
    return sb.toString().trim()
  }

  override fun getDaysRepeat(input: String) =
    input.splitByWhitespaces().firstOrNull { hasDays(it) }?.toRepeat(1) ?: 0

  override fun clearDaysRepeat(input: String) =
    input.splitByWhitespaces()
      .toMutableList()
      .also {
        it.forEachIndexed { index, s ->
          if (hasDays(s)) {
            ignoreAny {
              s.toInt()
              it[index - 1] = ""
            }
            it[index] = ""
            return@forEachIndexed
          }
        }
      }.clip()

  override fun hasRepeat(input: String) = input.matches(".*every.*") || hasEveryDay(input)

  override fun hasEveryDay(input: String) = input.matches(".*everyday.*")

  override fun clearRepeat(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasRepeat(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()

  override fun hasTomorrow(input: String) =
    input.matches(".*tomorrow.*") || input.matches(".*next day.*")

  override fun clearTomorrow(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches(".*tomorrow.*") || s.matches(".*next day.*")) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()

  override fun getMessage(input: String): String {
    val sb = StringBuilder()
    var isStart = false
    return input.splitByWhitespaces().forEach {
      if (isStart) sb.append(" ").append(it)
      if (it.matches("text")) isStart = true
    }.let {
      sb.toString().trim()
    }
  }

  override fun clearMessage(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches("text")) {
          ignoreAny {
            if (it[index - 1].matches("with")) {
              it[index - 1] = ""
            }
          }
          it[index] = ""
        }
      }
    }.clip()

  override fun getMessageType(input: String) = when {
    input.matches(".*message.*") -> Action.MESSAGE
    input.matches(".*letter.*") -> Action.MAIL
    else -> null
  }

  override fun clearMessageType(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (getMessageType(s) != null) {
          it[index] = ""
          val nextIndex = index + 1
          if (nextIndex < it.size && it[nextIndex].matches("to")) {
            it[nextIndex] = ""
          }
          return@forEachIndexed
        }
      }
    }.clip()

  override fun getAmpm(input: String) = when {
    input.matches(".*morning.*") -> Ampm.MORNING
    input.matches(".*evening.*") -> Ampm.EVENING
    input.matches(".*noon.*") -> Ampm.NOON
    input.matches(".*night.*") -> Ampm.NIGHT
    input.matches(".*a m.*") -> Ampm.MORNING
    input.matches(".*a.m..*") -> Ampm.MORNING
    input.matches(".*am.*") -> Ampm.MORNING
    input.matches(".*p m.*") -> Ampm.EVENING
    input.matches(".*p.m..*") -> Ampm.EVENING
    input.matches(".*pm.*") -> Ampm.EVENING
    else -> null
  }

  override fun clearAmpm(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (getAmpm(s) != null) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()

  override fun getShortTime(input: String?) =
    input?.let { s ->
      val matcher = Pattern.compile("([01]?[0-9]|2[0-3])( |:)[0-5][0-9]").matcher(s)
      var date: Date? = null
      if (matcher.find()) {
        val time = matcher.group().trim()
        for (format in hourFormats) {
          if (ignoreAny {
              date = format.parse(time)
              date
            } != null) break
        }
      }
      date
    }

  override fun clearTime(input: String?) =
    input?.splitByWhitespaces()?.toMutableList()?.also {
      it.forEachIndexed { i, s ->
        if (hasHours(s) != -1) {
          val index = hasHours(s)
          it[i] = ""
          ignoreAny {
            it[i - index].toInt()
            it[i - index] = ""
          }
        }
        if (hasMinutes(s) != -1) {
          val index = hasMinutes(s)
          ignoreAny {
            it[i - index].toInt()
            it[i - index] = ""
          }
          it[i] = ""
        }
      }
    }?.clip()?.let { s ->
      val matcher = Pattern.compile("([01]?[0-9]|2[0-3])( |:)[0-5][0-9]").matcher(s)
      if (matcher.find()) {
        val time = matcher.group().trim()
        s.replace(time, "")
      } else s
    }?.splitByWhitespaces()?.toMutableList()?.let { list ->
      val sb = StringBuilder()
      list.forEach { s ->
        if (!s.matches("at")) sb.append(" ").append(s.trim())
      }
      sb.toString().trim()
    } ?: ""

  override fun getMonth(input: String?) = when {
    input == null -> -1
    input.contains("january") -> 0
    input.contains("february") -> 1
    input.contains("march") -> 2
    input.contains("april") -> 3
    input.contains("may") -> 4
    input.contains("june") -> 5
    input.contains("july") -> 6
    input.contains("august") -> 7
    input.contains("september") -> 8
    input.contains("october") -> 9
    input.contains("november") -> 10
    input.contains("december") -> 11
    else -> -1
  }

  override fun hasCall(input: String) = input.matches(".*call.*")

  override fun clearCall(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasCall(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()

  override fun hasTimer(input: String) = input.let { " $it " }.let {
    it.matches(".*after.*") || it.matches(".* in .*")
  }

  override fun cleanTimer(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasTimer(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip().trim()

  override fun getDate(input: String, res: LongInternal): String? {
    var mills: Long = 0
    return input.splitByWhitespaces().toMutableList().also { list ->
      list.forEachIndexed { index, s ->
        val month = getMonth(s)
        if (month != -1) {
          val integer = ignoreAny({
            list[index - 1].toInt().also { list[index - 1] = "" }
          }) { 1 }
          val calendar = Calendar.getInstance()
          calendar.timeInMillis = System.currentTimeMillis()
          calendar[Calendar.MONTH] = month
          calendar[Calendar.DAY_OF_MONTH] = integer
          mills = calendar.timeInMillis
          list[index] = ""
          return@forEachIndexed
        }
      }
    }.clip().also {
      res.value = mills
    }
  }

  override fun hasSender(input: String) = input.matches(".*send.*")

  override fun clearSender(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasSender(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()

  override fun hasNote(input: String) = input.contains("note")

  override fun clearNote(input: String) = input.replace("note", "").trim()

  override fun hasAction(input: String): Boolean {
    return (input.startsWith("open")
      || input.matches(".*help.*")
      || input.matches(".*adjust.*")
      || input.matches(".*report.*")
      || input.matches(".*change.*"))
  }

  override fun getAction(input: String) = when {
    input.matches(".*help.*") -> Action.HELP
    input.matches(".*loudness.*") || input.matches(".*volume.*") -> Action.VOLUME
    input.matches(".*settings.*") -> Action.SETTINGS
    input.matches(".*report.*") -> Action.REPORT
    else -> Action.APP
  }

  override fun hasEvent(input: String) = input.startsWith("new") ||
    input.startsWith("add") || input.startsWith("create")

  override fun getEvent(input: String) = when {
    input.matches(".*birthday.*") -> Action.BIRTHDAY
    input.matches(".*reminder.*") -> Action.REMINDER
    else -> Action.NO_EVENT
  }

  override fun hasEmptyTrash(input: String) = input.matches(".*empty trash.*")

  override fun hasDisableReminders(input: String) = input.matches(".*disable reminder.*")

  override fun hasGroup(input: String) = input.matches(".*add group.*")

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
    return sb.toString().trim()
  }

  override fun hasToday(input: String) = input.matches(".*today.*")

  override fun hasAfterTomorrow(input: String) = input.matches(".*after tomorrow.*")

  override val afterTomorrow = "after tomorrow"

  override fun hasHours(input: String?) = when {
    input == null -> -1
    input.matches(".*hour.*") || input.matches(".*o'clock.*") ||
      input.matches(".*am.*") || input.matches(".*pm.*") -> 1
    else -> -1
  }

  override fun hasMinutes(input: String?) = when {
    input.matchesOrFalse(".*minute.*") -> 1
    else -> -1
  }

  override fun hasSeconds(input: String?) = input.matchesOrFalse(".*second.*")

  override fun hasDays(input: String?) = input.matchesOrFalse(".* day.*")

  override fun hasWeeks(input: String?) = input.matchesOrFalse(".*week.*")

  override fun hasMonth(input: String?) = input.matchesOrFalse(".*month.*")

  override fun hasAnswer(input: String) = input.let { " $it " }.matches(".* (yes|yeah|no) .*")

  override fun getAnswer(input: String) = when {
    input.matches(".* ?(yes|yeah) ?.*") -> Action.YES
    else -> Action.NO
  }

  override fun findFloat(input: String?) = input?.takeIf { it.matches("half") }
    ?.let { 0.5f } ?: -1f

  override fun clearFloats(input: String?) = when {
    input == null -> null
    input.contains("and a half") -> input.replace("and a half", "")
    input.contains("and half") -> input.replace("and half", "")
    input.contains(" half an ") -> input.replace("half an", "")
    input.contains(" in half ") -> input.replace("in half", "")
    else -> input
  }

  override fun findNumber(input: String?) = when {
    input == null -> -1f
    input.matches("zero") || input.matches("nil") -> 0f
    input.matches("one") || input.matches("first") -> 1f
    input.matches("two") || input.matches("second") -> 2f
    input.matches("three") || input.matches("third") -> 3f
    input.matches("four") || input.matches("fourth") -> 4f
    input.matches("five") || input.matches("fifth") -> 5f
    input.matches("six") || input.matches("sixth") -> 6f
    input.matches("seven") || input.matches("seventh") -> 7f
    input.matches("eight") || input.matches("eighth") -> 8f
    input.matches("nine") || input.matches("ninth") -> 9f
    input.matches("ten") || input.matches("tenth") -> 10f
    input.matches("eleven") || input.matches("eleventh") -> 11f
    input.matches("twelve") || input.matches("twelfth") -> 12f
    input.matches("thirteen") || input.matches("thirteenth") -> 13f
    input.matches("fourteen") || input.matches("fourteenth") -> 14f
    input.matches("fifteen") || input.matches("fifteenth") -> 15f
    input.matches("sixteen") || input.matches("sixteenth") -> 16f
    input.matches("seventeen") || input.matches("seventeenth") -> 17f
    input.matches("eighteen") || input.matches("eighteenth") -> 18f
    input.matches("nineteen") || input.matches("nineteenth") -> 19f
    input.matches("twenty") || input.matches("twentieth") -> 20f
    input.matches("thirty") || input.matches("thirtieth") -> 30f
    input.matches("forty") || input.matches("fortieth") -> 40f
    input.matches("fifty") || input.matches("fiftieth") -> 50f
    input.matches("sixty") || input.matches("sixtieth") -> 60f
    input.matches("seventy") || input.matches("seventieth") -> 70f
    input.matches("eighty") || input.matches("eightieth") -> 80f
    input.matches("ninety") || input.matches("ninetieth") -> 90f
    else -> -1f
  }

  override fun hasShowAction(input: String) = input.matches(".*show.*")

  override fun getShowAction(input: String) = when {
    input.matches(".*birthdays.*") -> Action.BIRTHDAYS
    input.matches(".*active reminders.*") -> Action.ACTIVE_REMINDERS
    input.matches(".*reminders.*") -> Action.REMINDERS
    input.matches(".*events.*") -> Action.EVENTS
    input.matches(".*notes.*") -> Action.NOTES
    input.matches(".*groups.*") -> Action.GROUPS
    input.matches(".*shopping lists?.*") -> Action.SHOP_LISTS
    else -> null
  }

  override fun hasNextModifier(input: String) = input.matches(".*next.*")
}