package com.backdoor.engine.lang

import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import com.backdoor.engine.misc.ContactsInterface
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import java.util.regex.Pattern

internal class EnWorker(zoneId: ZoneId, contactsInterface: ContactsInterface?) :
  Worker(zoneId, contactsInterface) {

  override val weekdays: List<String> = listOf(
    "sunday",
    "monday",
    "tuesday",
    "wednesday",
    "thursday",
    "friday",
    "saturday"
  )

  override fun hasCalendar(input: String): Boolean = input.matches(".*calendar.*")

  override fun clearCalendar(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches(".*calendar.*")) {
          it[index] = ""
          clearAllBackward(it, index - 1, 3, "and", "add", "to")
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun clearWeekDays(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        for (day in weekdays) {
          if (s.matches(".*$day.*")) {
            it[index] = ""
            clearAllBackward(it, index - 1, 1, "and")
            break
          }
        }
      }
    }.clip()
  }

  override fun getDaysRepeat(input: String): Long =
    input.splitByWhitespaces().firstOrNull { hasDays(it) }?.toRepeat(1) ?: 0

  override fun clearDaysRepeat(input: String): String {
    return input.splitByWhitespaces()
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
  }

  override fun hasRepeat(input: String): Boolean =
    input.matches(".*every.*") || hasEveryDay(input)

  override fun hasEveryDay(input: String): Boolean = input.matches(".*every ?day.*")

  override fun clearRepeat(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasRepeat(s)) {
          it[index] = ""
          clearAllForward(it, index + 1, 1, "days?")
          return@forEachIndexed
        }
      }
    }.clip().splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches("repeat")) {
          it[index] = ""
          clearAllBackward(it, index - 1, 1, "and")
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun hasTomorrow(input: String): Boolean =
    input.matches(".*tomorrow.*") || input.matches(".*next day.*")

  override fun clearTomorrow(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches(".*tomorrow.*") || s.matches(".*next day.*")) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()
  }

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

  override fun clearMessage(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
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
  }

  override fun getMessageType(input: String): Action? = when {
    input.matches(".*message.*") -> Action.MESSAGE
    input.matches(".*letter.*") || input.matches(".*e?( |-)?mail.*") -> Action.MAIL
    else -> null
  }

  override fun clearMessageType(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (getMessageType(s) != null) {
          it[index] = ""
          clearAllForward(it, index + 1, 1, "to")
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun getAmpm(input: String): Ampm? = when {
    " $input ".matches(".* morning .*") -> Ampm.MORNING
    " $input ".matches(".* evening .*") -> Ampm.EVENING
    " $input ".matches(".* noon .*") -> Ampm.NOON
    " $input ".matches(".* night .*") -> Ampm.NIGHT
    " $input ".matches(".* a(.| |)m(.| |) .*") -> Ampm.MORNING
    " $input ".matches(".* p(.| |)?m(.| |) .*") -> Ampm.EVENING
    else -> null
  }

  override fun clearAmpm(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (getAmpm(s) != null) {
          it[index] = ""
          clearAllBackward(it, index - 1, 1, "at")
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun getShortTime(input: String?): LocalTime? {
    return input?.let { s ->
      val matcher = Pattern.compile("([01]?[0-9]|2[0-3])( |:)[0-5][0-9]").matcher(s)
      var localTime: LocalTime? = null
      if (matcher.find()) {
        val time = matcher.group().trim()
        for (format in hourFormats) {
          if (ignoreAny {
              localTime = LocalTime.parse(time, format)
              localTime
            } != null) break
        }
      }
      localTime
    }
  }

  override fun clearTime(input: String?): String {
    val pattern = Pattern.compile("([01]?[0-9]|2[0-3])( |:)[0-5][0-9]")
    return input?.splitByWhitespaces()?.toMutableList()?.also {
      it.forEachIndexed { i, s ->
        if (hasHours(s) != -1) {
          val index = hasHours(s)
          it[i] = ""
          ignoreAny {
            it[i - index].toFloat()
            it[i - index] = ""
          }
        }
        if (hasMinutes(s) != -1) {
          val index = hasMinutes(s)
          ignoreAny {
            it[i - index].toFloat()
            it[i - index] = ""
          }
          it[i] = ""
        }
        ignoreAny {
          val f = s.toFloat()
          if (f >= 0f && f < 24f) {
            it[i] = ""
            clearAllBackward(it, i - 1, 1, "(at|on)")
            if (i < it.size - 1) {
              val minutes = it[i + 1]
              val matcher = pattern.matcher("$s:$minutes")
              if (matcher.find()) {
                it[i + 1] = ""
              }
            }
          }
        }
        ignoreAny {
          val matcher = pattern.matcher(s)
          if (matcher.find()) {
            it[i] = ""
            clearAllBackward(it, i - 1, 1, "(at|on)")
          }
        }
      }
    }?.clip()?.let { s ->
      val matcher = pattern.matcher(s)
      if (matcher.find()) {
        val time = matcher.group().trim()
        s.replace(time, "")
      } else s
    } ?: ""
  }

  override fun getMonth(input: String?): Int = when {
    input == null -> -1
    input.contains("january") -> 1
    input.contains("february") -> 2
    input.contains("march") -> 3
    input.contains("april") -> 4
    input.contains("may") -> 5
    input.contains("june") -> 6
    input.contains("july") -> 7
    input.contains("august") -> 8
    input.contains("september") -> 9
    input.contains("october") -> 10
    input.contains("november") -> 11
    input.contains("december") -> 12
    else -> -1
  }

  override fun hasCall(input: String): Boolean = input.matches(".*call.*")

  override fun clearCall(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasCall(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun hasTimer(input: String): Boolean = input.let { " $it " }.let {
    (it.matches(".*after.*") || it.matches(".* in .*")) && getMonth(it) == -1
  }

  override fun cleanTimer(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasTimer(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip().trim()
  }

  override fun getDateAndClear(input: String, result: (LocalDate?) -> Unit): String {
    var localDate: LocalDate? = null
    return input.splitByWhitespaces().toMutableList().also { list ->
      list.forEachIndexed { index, s ->
        val month = getMonth(s)
        if (month != -1) {
          var dayOfMonth = ignoreAny({
            list[index + 1].toFloat().also { list[index + 1] = "" }
          }) { -1f }
          if (dayOfMonth == -1f) {
            dayOfMonth = ignoreAny({
              list[index - 1].toFloat().also { list[index - 1] = "" }
            }) { 1f }
          }

          var parsedDate = LocalDate.now(zoneId)
            .withDayOfMonth(dayOfMonth.toInt())
            .withMonth(month)

          if (parsedDate.isBefore(LocalDate.now(zoneId))) {
            parsedDate = parsedDate.plusYears(1)
          }

          localDate = parsedDate
          list[index] = ""
          clearAllBackward(list, index - 1, 1, "(on|at)")
          return@forEachIndexed
        }
      }
    }.clip().also {
      result(localDate)
    }
  }

  override fun hasSender(input: String): Boolean = input.matches(".*send.*")

  override fun clearSender(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasSender(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun hasNote(input: String): Boolean = input.contains("note")

  override fun clearNote(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches(".*note.*")) {
          it[index] = ""
          clearAllBackward(it, index - 1, 1, "create", "add")
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun hasAction(input: String): Boolean {
    return (input.startsWith("open")
      || input.matches(".*help.*")
      || input.matches(".*adjust.*")
      || input.matches(".*report.*")
      || input.matches(".*change.*"))
  }

  override fun getAction(input: String): Action = when {
    input.matches(".*help.*") -> Action.HELP
    input.matches(".*loudness.*") || input.matches(".*volume.*") -> Action.VOLUME
    input.matches(".*settings.*") -> Action.SETTINGS
    input.matches(".*report.*") -> Action.REPORT
    else -> Action.APP
  }

  override fun hasEvent(input: String): Boolean = input.startsWith("new") ||
    input.startsWith("add") || input.startsWith("create")

  override fun getEvent(input: String): Action = when {
    input.matches(".*birthday.*") -> Action.BIRTHDAY
    input.matches(".*reminder.*") -> Action.REMINDER
    else -> Action.NO_EVENT
  }

  override fun hasEmptyTrash(input: String): Boolean =
    input.matches(".*(empty|clear) (trash|completed reminders?).*")

  override fun hasDisableReminders(input: String): Boolean =
    input.matches(".*disable (all )?reminders?.*")

  override fun hasGroup(input: String): Boolean = input.matches(".*(add|create|new) group.*")

  override fun clearGroup(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches(".*group.*")) {
          it[index] = ""
          clearAllBackward(it, index - 1, 2, "an?", "(add|create|new)")
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun hasToday(input: String): Boolean = input.matches(".*today.*")

  override fun hasAfterTomorrow(input: String): Boolean = input.matches(".*after tomorrow.*")

  override val afterTomorrow: String = "after tomorrow"

  override fun hasHours(input: String?): Int = when {
    input == null -> -1
    input.matches(".*hour.*") || input.matches(".*o'clock.*") || getAmpm(input) != null -> 1
    else -> -1
  }

  override fun hasMinutes(input: String?): Int = when {
    input.matchesOrFalse(".*minute.*") -> 1
    else -> -1
  }

  override fun hasSeconds(input: String?): Boolean = input.matchesOrFalse(".*seconds?.*")

  override fun hasDays(input: String?): Boolean = input.matchesOrFalse(".* days?.*") &&
    !input.matchesOrFalse(".*birthdays?.*")

  override fun hasWeeks(input: String?): Boolean = input.matchesOrFalse(".*weeks?.*")

  override fun hasMonth(input: String?): Boolean = input.matchesOrFalse(".*months?.*")

  override fun hasAnswer(input: String): Boolean =
    input.let { " $it " }.matches(".* (yes|yeah|no) .*")

  override fun getAnswer(input: String): Action = when {
    input.matches(".* ?(yes|yeah) ?.*") -> Action.YES
    else -> Action.NO
  }

  override fun findFloat(input: String?): Float = input?.takeIf { it.matches("half") }
    ?.let { 0.5f } ?: -1f

  override fun clearFloats(input: String?): String? = when {
    input == null -> null
    input.contains("and a half") -> input.replace("and a half", "")
    input.contains("and half") -> input.replace("and half", "")
    input.contains(" half an ") -> input.replace("half an", "")
    input.contains(" in half ") -> input.replace("in half", "")
    else -> input
  }

  override fun findNumber(input: String?): Float = when {
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
    input.matches("eighteens?") || input.matches("eighteenth") -> 18f
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

  override fun hasShowAction(input: String): Boolean = input.matches(".*show.*")

  override fun getShowAction(input: String): Action? = when {
    input.matches(".*birthdays.*") -> Action.BIRTHDAYS
    input.matches(".*active reminders.*") -> Action.ACTIVE_REMINDERS
    input.matches(".*reminders.*") -> Action.REMINDERS
    input.matches(".*events.*") -> Action.EVENTS
    input.matches(".*notes.*") -> Action.NOTES
    input.matches(".*groups.*") -> Action.GROUPS
    input.matches(".*shopping lists?.*") -> Action.SHOP_LISTS
    else -> null
  }

  override fun hasNextModifier(input: String): Boolean = input.matches(".*next.*")

  override fun hasConnectSpecialWord(input: String): Boolean {
    return input.matches("(at|on)")
  }
}
