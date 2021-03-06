package com.backdoor.engine.lang

import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import com.backdoor.engine.misc.LongInternal
import java.util.*
import java.util.regex.Pattern

internal class UkWorker : Worker() {
  override val weekdays = listOf(
    " неділ",
    "понеділ",
    "вівтор",
    "середу?а?и?",
    "четвер",
    "п'ятниц",
    "субот"
  )

  override fun hasCalendar(input: String) = input.matches(".*календар.*")

  override fun clearCalendar(input: String) =
    input.splitByWhitespaces()
      .toMutableList()
      .let {
        it.forEachIndexed { index, s ->
          if (s.matches(".*календар.*")) {
            it[index] = ""
            if (index > 0 && it[index - 1].equals("до", ignoreCase = true)) {
              it[index - 1] = ""
            }
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

  override fun hasRepeat(input: String) = input.matches(".*кожн.*") || hasEveryDay(input)

  override fun hasEveryDay(input: String) =
    input.matches(".*щоден.*") || input.matches(".*щодня.*")

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
            if (it[index - 1].matches("з")) it[index - 1] = ""
          }
          it[index] = ""
        }
      }
    }.clip()

  override fun getMessageType(input: String) = when {
    input.matches(".*повідомлення.*") -> Action.MESSAGE
    input.matches(".*листа?.*") -> Action.MAIL
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
    input.matches(".*з?ран(ку|о)?.*") || input.matches(".*вранці.*") -> Ampm.MORNING
    input.matches(".*в?веч(о|е)р.*") -> Ampm.EVENING
    input.matches(".*в?день.*") -> Ampm.NOON
    input.matches(".*в?ночі.*") -> Ampm.NIGHT
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
        if (!s.matches("об?")) sb.append(" ").append(s.trim())
      }
      sb.toString().trim()
    } ?: ""

  override fun getMonth(input: String?) = when {
    input == null -> -1
    input.contains("січень") || input.contains("січня") -> 0
    input.contains("лютий") || input.contains("лютого") -> 1
    input.contains("березень") || input.contains("березня") -> 2
    input.contains("квітень") || input.contains("квітня") -> 3
    input.contains("травень") || input.contains("травня") -> 4
    input.contains("червень") || input.contains("червня") -> 5
    input.contains("липень") || input.contains("липня") -> 6
    input.contains("серпень") || input.contains("серпня") -> 7
    input.contains("вересень") || input.contains("вересня") -> 8
    input.contains("жовтень") || input.contains("жовтня") -> 9
    input.contains("листопад") || input.contains("листопада") -> 10
    input.contains("грудень") || input.contains("грудня") -> 11
    else -> -1
  }

  override fun hasCall(input: String) = input.matches(".*дзвонити.*")

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

  override fun hasSender(input: String) = input.matches(".*надісл.*")

  override fun clearSender(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasSender(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()

  override fun hasNote(input: String) = input.contains("нотатка")

  override fun clearNote(input: String) = input.replace("нотатка", "").trim()

  override fun hasAction(input: String): Boolean {
    return (input.startsWith("відкрити") || input.matches(".*допом.*")
      || input.matches(".*гучніст.*") || input.matches(".*налаштув.*")
      || input.matches(".*повідомити.*"))
  }

  override fun getAction(input: String) = when {
    input.matches(".*допомог.*") -> Action.HELP
    input.matches(".*гучніст.*") -> Action.VOLUME
    input.matches(".*налаштування.*") -> Action.SETTINGS
    input.matches(".*повідомити.*") -> Action.REPORT
    else -> Action.APP
  }

  override fun hasEvent(input: String) =
    input.startsWith("додати") || input.matches("нове?и?й?.*")

  override fun hasEmptyTrash(input: String) = input.matches(".*очисти(ти)? кошик.*")

  override fun hasDisableReminders(input: String) =
    input.matches(".*вимкн(и|ути)? (всі)? ?нагадування.*")

  override fun hasGroup(input: String) = input.matches(".*дода(ти|й)? групу.*")

  override fun clearGroup(input: String): String {
    val sb = StringBuilder()
    val parts: Array<String> = input.splitByWhitespaces().toTypedArray()
    var st = false
    for (s in parts) {
      if (s.matches(".*групу.*")) {
        st = true
        continue
      }
      if (st) sb.append(s).append(" ")
    }
    return sb.toString().trim()
  }

  override fun getEvent(input: String) = when {
    input.matches(".*день народжен.*") -> Action.BIRTHDAY
    input.matches(".*нагадуван.*") -> Action.REMINDER
    else -> Action.NO_EVENT
  }

  override fun hasToday(input: String) = input.matches(".*сьогодн.*")

  override fun hasAfterTomorrow(input: String) = input.matches(".*післязавтр.*")

  override val afterTomorrow = "післязавтра"

  override fun hasHours(input: String?) = when {
    input.matchesOrFalse(".*годині?у?.*") -> 1
    else -> -1
  }

  override fun hasMinutes(input: String?) = when {
    input.matchesOrFalse(".*хвилин.*") -> 1
    else -> -1
  }

  override fun hasSeconds(input: String?) = input.matchesOrFalse(".*секунд.*")

  override fun hasDays(input: String?) = input.matchesOrFalse(".*дні.*") ||
    input.matchesOrFalse(".*день.*") || input.matchesOrFalse(".*дня.*")

  override fun hasWeeks(input: String?) =
    input.matchesOrFalse(".*тиждень.*") || input.matchesOrFalse(".*тижні.*")

  override fun hasMonth(input: String?) = input.matchesOrFalse(".*місяц.*")

  override fun hasAnswer(input: String) = input.let { " $it " }.matches(".* (так|ні) .*")

  override fun getAnswer(input: String) = when {
    input.matches(".* ?так ?.*") -> Action.YES
    else -> Action.NO
  }

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

  override fun findFloat(input: String?) = when {
    input == null -> -1f
    input.contains("півтор") -> 1.5f
    input.contains("половин") || input.contains("пів") -> 0.5f
    else -> -1f
  }

  override fun clearFloats(input: String?): String? {
    if (input == null) return null
    return input.replace("з половиною", "")
      .splitByWhitespaces()
      .toMutableList()
      .also {
        it.forEachIndexed { index, s ->
          if (s.contains("півтор") || s.matches("половин*.")) {
            it[index] = ""
            return@forEachIndexed
          }
        }
      }.clip().let {
        if (it.contains(" пів")) {
          it.replace("пів", "")
        } else it
      }
  }

  override fun findNumber(input: String?) = when {
    input == null -> -1f
    input.matches("нуль") -> 0f
    input.matches("один") || input.matches("одну") || input.matches("одна") -> 1f
    input.matches("два") || input.matches("дві") -> 2f
    input.matches("три") -> 3f
    input.matches("чотири") -> 4f
    input.matches("п'ять") -> 5f
    input.matches("шість") -> 6f
    input.matches("сім") -> 7f
    input.matches("вісім") -> 8f
    input.matches("дев'ять") -> 9f
    input.matches("десять") -> 10f
    input.matches("одинадцять") -> 11f
    input.matches("дванадцять") -> 12f
    input.matches("тринадцять") -> 13f
    input.matches("чотирнадцять") -> 14f
    input.matches("п'ятнадцять") -> 15f
    input.matches("шістнадцять") -> 16f
    input.matches("сімнадцять") -> 17f
    input.matches("вісімнадцять") -> 18f
    input.matches("дев'ятнадцять") -> 19f
    input.matches("двадцять") -> 20f
    input.matches("тридцять") -> 30f
    input.matches("сорок") -> 40f
    input.matches("п'ятдесят") -> 50f
    input.matches("шістдесят") -> 60f
    input.matches("сімдесят") -> 70f
    input.matches("вісімдесят") -> 80f
    input.matches("дев'яносто") -> 90f
    input.matches("першого") || input.matches("першій") -> 1f
    input.matches("другого") || input.matches("другій") -> 2f
    input.matches("третього") || input.matches("третій") -> 3f
    input.matches("четвертого") || input.matches("четвертій") -> 4f
    input.matches("п'ятого") || input.matches("п'ятій") -> 5f
    input.matches("шостого") || input.matches("шостій") -> 6f
    input.matches("сьомого") || input.matches("сьомій") -> 7f
    input.matches("восьмого") || input.matches("восьмій") -> 8f
    input.matches("дев'ятого") || input.matches("дев'ятій") -> 9f
    input.matches("десятого") || input.matches("десятій") -> 10f
    input.matches("одинадцятого") || input.matches("одинадцятій") -> 11f
    input.matches("дванадцятого") || input.matches("дванадцятій") -> 12f
    input.matches("тринадцятого") || input.matches("тринадцятій") -> 13f
    input.matches("чотирнадцятого") || input.matches("чотирнадцятій") -> 14f
    input.matches("п'ятнадцятого") || input.matches("п'ятнадцятій") -> 15f
    input.matches("шістнадцятого") || input.matches("шістнадцятій") -> 16f
    input.matches("сімнадцятого") || input.matches("сімнадцятій") -> 17f
    input.matches("вісімнадцятого") || input.matches("вісімнадцятій") -> 18f
    input.matches("дев'ятнадцятого") || input.matches("дев'ятнадцятій") -> 19f
    input.matches("двадцятого") || input.matches("двадцятій") -> 20f
    input.matches("тридцятого") -> 30f
    input.matches("сорокового") -> 40f
    input.matches("п'ятдесятого") -> 50f
    input.matches("шістдесятого") -> 60f
    input.matches("сімдесятого") -> 70f
    input.matches("вісімдесятого") -> 80f
    input.matches("дев'яностого") -> 90f
    else -> -1f
  }

  override fun hasShowAction(input: String) = input.matches(".*пока(зати|жи)?.*")

  override fun getShowAction(input: String) = when {
    input.matches(".*д?е?н?і?ь? народжен.*") -> Action.BIRTHDAYS
    input.matches(".*активні нагадуван.*") -> Action.ACTIVE_REMINDERS
    input.matches(".*нагадуван.*") -> Action.REMINDERS
    input.matches(".*події.*") -> Action.EVENTS
    input.matches(".*нотатки.*") -> Action.NOTES
    input.matches(".*групи.*") -> Action.GROUPS
    input.matches(".*списо?ки? покупок.*") -> Action.SHOP_LISTS
    else -> null
  }

  override fun hasNextModifier(input: String) = input.matches(".*наступн.*")
}