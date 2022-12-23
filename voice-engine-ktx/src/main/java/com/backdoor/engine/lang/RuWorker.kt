package com.backdoor.engine.lang

import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import com.backdoor.engine.misc.ContactsInterface
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import java.util.regex.Pattern

internal class RuWorker(zoneId: ZoneId, contactsInterface: ContactsInterface?) :
  Worker(zoneId, contactsInterface) {
  override val weekdays = listOf(
    "воскресен",
    "понедельн",
    "вторн",
    "среду?",
    "червер",
    "пятниц",
    "суббот"
  )

  override fun hasCalendar(input: String) = input.matches(".*календарь.*")

  override fun clearCalendar(input: String) =
    input.splitByWhitespaces()
      .toMutableList()
      .let {
        it.forEachIndexed { index, s ->
          if (s.matches(".*календарь.*")) {
            it[index] = ""
            return@forEachIndexed
          }
        }
        it.clip()
      }

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
      if (!part.matches("в")) sb.append(" ").append(part)
    }
    return sb.toString().trim()
  }

  override fun getDaysRepeat(input: String) =
    input.splitByWhitespaces().firstOrNull { hasDays(it) }?.toRepeat(1) ?: 0

  override fun clearDaysRepeat(input: String) =
    input.splitByWhitespaces().toMutableList().also {
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

  override fun hasRepeat(input: String) = input.matches(".*кажд.*") || hasEveryDay(input)

  override fun hasEveryDay(input: String) = input.matches(".*ежедневн.*")

  override fun clearRepeat(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasRepeat(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()

  override fun hasTomorrow(input: String) = input.matches(".*завтра.*")

  override fun clearTomorrow(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches(".*завтра.*")) {
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
      if (it.matches("текст(ом)?")) isStart = true
    }.let {
      sb.toString().trim()
    }
  }

  override fun clearMessage(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches("текст(ом)?")) {
          ignoreAny {
            if (it[index - 1].matches("с")) it[index - 1] = ""
          }
          it[index] = ""
        }
      }
    }.clip()

  override fun getMessageType(input: String) = when {
    input.matches(".*сообщение.*") -> Action.MESSAGE
    input.matches(".*письмо?.*") -> Action.MAIL
    else -> null
  }

  override fun clearMessageType(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (getMessageType(s) != null) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()

  override fun getAmpm(input: String) = when {
    input.matches(".*утр(а|ом)?.*") -> Ampm.MORNING
    input.matches(".*вечер.*") -> Ampm.EVENING
    input.matches(".*днем.*") -> Ampm.NOON
    input.matches(".*ночью.*") -> Ampm.NIGHT
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

  override fun getShortTime(input: String?) = input?.let { s ->
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

  override fun clearTime(input: String?) =
    input?.splitByWhitespaces()?.toMutableList()?.also {
      it.forEachIndexed { i, s ->
        if (hasHours(s) != -1) {
          val index = hasHours(s)
          it[i] = ""
          var hourSuccess = false
          ignoreAny {
            it[i - index].toInt()
            hourSuccess = true
            it[i - index] = ""
          }
          if (hourSuccess) {
            ignoreAny {
              it[i + 1].toInt()
              it[i + 1] = ""
            }
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
        if (!s.matches("в")) sb.append(" ").append(s.trim())
      }
      sb.toString().trim()
    } ?: ""

  override fun getMonth(input: String?) = when {
    input == null -> -1
    input.contains("январь") || input.contains("января") -> 1
    input.contains("февраль") || input.contains("февраля") -> 2
    input.contains("март") || input.contains("марта") -> 3
    input.contains("апрель") || input.contains("апреля") -> 4
    input.contains("май") || input.contains("мая") -> 5
    input.contains("июнь") || input.contains("июня") -> 6
    input.contains("июль") || input.contains("июля") -> 7
    input.contains("август") || input.contains("августа") -> 8
    input.contains("сентябрь") || input.contains("сентября") -> 9
    input.contains("октябрь") || input.contains("октября") -> 10
    input.contains("ноябрь") || input.contains("ноября") -> 11
    input.contains("декабрь") || input.contains("декабря") -> 12
    else -> -1
  }

  override fun hasCall(input: String) = input.matches(".*звонить.*")

  override fun clearCall(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasCall(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()

  override fun hasTimer(input: String) = input.matches(".*через.*")

  override fun cleanTimer(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasTimer(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip().trim()

  override fun hasSender(input: String) = input.matches(".*отправ.*")

  override fun clearSender(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasSender(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()

  override fun hasNote(input: String) = input.contains("заметка")

  override fun clearNote(input: String) = input.replace("заметка", "").trim()

  override fun hasAction(input: String): Boolean {
    return (input.startsWith("открыть") || input.matches(".*помощь.*") ||
      input.matches(".*настро.*") || input.matches(".*громкость.*")
      || input.matches(".*сообщить.*"))
  }

  override fun getAction(input: String) = when {
    input.matches(".*помощь.*") -> Action.HELP
    input.matches(".*громкость.*") -> Action.VOLUME
    input.matches(".*настройки.*") -> Action.SETTINGS
    input.matches(".*сообщить.*") -> Action.REPORT
    else -> Action.APP
  }

  override fun hasEvent(input: String) =
    input.startsWith("добавить") || input.matches("ново?е?ы?й?.*")

  override fun getEvent(input: String) = when {
    input.matches(".*день рождения.*") -> Action.BIRTHDAY
    input.matches(".*напоминан.*") -> Action.REMINDER
    else -> Action.NO_EVENT
  }

  override fun hasEmptyTrash(input: String) = input.matches(".*очисти(ть)? корзин.*")

  override fun hasDisableReminders(input: String) =
    input.matches(".*выключи (все)? ?напоминания.*") ||
      input.matches(".*отключи(ть)? (все)? ?напоминания.*")

  override fun hasGroup(input: String) = input.matches(".*добавь группу.*")

  override fun clearGroup(input: String): String {
    val sb = StringBuilder()
    val parts: Array<String> = input.splitByWhitespaces().toTypedArray()
    var st = false
    for (s in parts) {
      if (s.matches(".*групп.*")) {
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

  override fun hasToday(input: String) = input.matches(".*сегодн.*")

  override fun hasAfterTomorrow(input: String) = input.matches(".*послезавтр.*")

  override val afterTomorrow = "послезавтра"

  override fun hasHours(input: String?) = when {
    input.matchesOrFalse(".*час.*") -> 1
    else -> -1
  }

  override fun hasMinutes(input: String?) = when {
    input.matchesOrFalse(".*минуту?.*") -> 1
    else -> -1
  }

  override fun hasSeconds(input: String?) = input.matchesOrFalse(".*секунд.*")

  override fun hasDays(input: String?) = input.matchesOrFalse(".*дня.*") ||
    input.matchesOrFalse(".*дней.*") || input.matchesOrFalse(".*день.*")

  override fun hasWeeks(input: String?) = input.matchesOrFalse(".*недел.*")

  override fun hasMonth(input: String?) = input.matchesOrFalse(".*месяц.*")

  override fun hasAnswer(input: String) = input.let { " $it " }.matches(".* (да|нет) .*")

  override fun getDateAndClear(input: String, result: (LocalDate?) -> Unit): String? {
    var localDate: LocalDate? = null
    return input.splitByWhitespaces().toMutableList().also { list ->
      list.forEachIndexed { index, s ->
        val month = getMonth(s)
        if (month != -1) {
          val dayOfMonth = ignoreAny({
            list[index - 1].toInt().also { list[index - 1] = "" }
          }) { 1 }

          val parsedDate = LocalDate.now(zoneId)
            .withDayOfMonth(dayOfMonth)
            .withMonth(month)

          localDate = parsedDate

          list[index] = ""
          return@forEachIndexed
        }
      }
    }.clip().also {
      result(localDate)
    }
  }

  override fun getAnswer(input: String) = when {
    input.matches(".* ?да ?.*") -> Action.YES
    else -> Action.NO
  }

  override fun findFloat(input: String?) = when {
    input == null -> -1f
    input.contains("полтор") -> 1.5f
    input.contains("половин") || input.contains("пол") -> 0.5f
    else -> -1f
  }

  override fun clearFloats(input: String?): String? {
    if (input == null) return null
    return input.replace("с половиной", "")
      .splitByWhitespaces()
      .toMutableList()
      .also {
        it.forEachIndexed { index, s ->
          if (s.contains("полтор") || s.matches("половин*.")) {
            it[index] = ""
            return@forEachIndexed
          }
        }
      }.clip().let {
        if (it.contains(" пол")) {
          it.replace("пол", "")
        } else it
      }
  }

  override fun findNumber(input: String?) = when {
    input == null -> -1f
    input.matches("ноль") -> 0f
    input.matches("один") || input.matches("одну") || input.matches("одна") -> 1f
    input.matches("два") || input.matches("две") -> 2f
    input.matches("три") -> 3f
    input.matches("четыре") -> 4f
    input.matches("пять") -> 5f
    input.matches("шесть") -> 6f
    input.matches("семь") -> 7f
    input.matches("восемь") -> 8f
    input.matches("девять") -> 9f
    input.matches("десять") -> 10f
    input.matches("одиннадцать") -> 11f
    input.matches("двенадцать") -> 12f
    input.matches("тринадцать") -> 13f
    input.matches("четырнадцать") -> 14f
    input.matches("пятнадцать") -> 15f
    input.matches("шестнадцать") -> 16f
    input.matches("семнадцать") -> 17f
    input.matches("восемнадцать") -> 18f
    input.matches("девятнадцать") -> 19f
    input.matches("двадцать") -> 20f
    input.matches("тридцать") -> 30f
    input.matches("сорок") -> 40f
    input.matches("пятьдесят") -> 50f
    input.matches("шестьдесят") -> 60f
    input.matches("семьдесят") -> 70f
    input.matches("восемьдесят") -> 80f
    input.matches("девяносто") -> 90f
    input.matches("первого") -> 1f
    input.matches("второго") -> 2f
    input.matches("третьего") -> 3f
    input.matches("четвертого") -> 4f
    input.matches("пятого") -> 5f
    input.matches("шестого") -> 6f
    input.matches("седьмого") -> 7f
    input.matches("восьмого") -> 8f
    input.matches("девятого") -> 9f
    input.matches("десятого") -> 10f
    input.matches("одиннадцатого") -> 11f
    input.matches("двенадцатого") -> 12f
    input.matches("тринадцатого") -> 13f
    input.matches("четырнадцатого") -> 14f
    input.matches("пятнадцатого") -> 15f
    input.matches("шестнадцатого") -> 16f
    input.matches("семнадцатого") -> 17f
    input.matches("восемнадцатого") -> 18f
    input.matches("девятнадцатого") -> 19f
    input.matches("двадцатого") -> 20f
    input.matches("тридцатого") -> 30f
    input.matches("сорокового") -> 40f
    input.matches("пятидесятого") -> 50f
    input.matches("шестидесятого") -> 60f
    input.matches("семидесятого") -> 70f
    input.matches("восьмидесятого") -> 80f
    input.matches("девяностого") -> 90f
    else -> -1f
  }

  override fun hasShowAction(input: String) = input.matches(".*пока(зать|жы?)?.*")

  override fun getShowAction(input: String) = when {
    input.matches(".*рожден.*") -> Action.BIRTHDAYS
    input.matches(".*активные напомин.*") -> Action.ACTIVE_REMINDERS
    input.matches(".*напомин.*") -> Action.REMINDERS
    input.matches(".*события.*") -> Action.EVENTS
    input.matches(".*заметки.*") -> Action.NOTES
    input.matches(".*группы.*") -> Action.GROUPS
    input.matches(".*списо?ки? покуп.*") -> Action.SHOP_LISTS
    else -> null
  }

  override fun hasNextModifier(input: String) = input.matches(".*следу.*")
}
