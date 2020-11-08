package com.backdoor.engine.lang

import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import com.backdoor.engine.misc.LongInternal
import java.util.*
import java.util.regex.Pattern

internal class DeWorker : Worker() {

  override val weekdays = listOf(
    "sonntag",
    "montag",
    "dienstag",
    "mittwoch",
    "donnerstag",
    "freitag",
    "samstag"
  )

  override fun hasCalendar(input: String) = input.matches(".*kalender.*")

  override fun clearCalendar(input: String) =
    input.splitByWhitespaces()
      .toMutableList()
      .let {
        it.forEachIndexed { index, s ->
          if (s.matches(".*kalender.*")) {
            it[index] = ""
            if (index > 0 && it[index - 1].equals("zum", ignoreCase = true)) {
              it[index - 1] = ""
            }
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
      val part = s.trim { it <= ' ' }
      if (!part.matches("zum")) sb.append(" ").append(part)
    }
    return sb.toString().trim { it <= ' ' }
  }

  override fun getDaysRepeat(input: String) =
    input.splitByWhitespaces()
      .firstOrNull { hasDays(it) }?.toLong(1) ?: 0

  override fun clearDaysRepeat(input: String): String? {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasDays(s)) {
          try {
            s.toInt()
            it[index - 1] = ""
          } catch (ignored: NumberFormatException) {
          }
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun clearRepeat(input: String): String? {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasRepeat(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun hasTomorrow(input: String) = input.matches(".*morgen.*")

  override fun clearTomorrow(input: String): String? {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches(".*morgen.*")) {
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
      sb.toString().trim { it <= ' ' }
    }
  }

  override fun clearMessage(input: String): String? {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches("text")) {
          try {
            if (it[index - 1].matches("mit")) {
              it[index - 1] = ""
            }
          } catch (ignored: IndexOutOfBoundsException) {
          }
          it[index] = ""
        }
      }
    }.clip()
  }

  override fun clearMessageType(input: String): String? {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (getMessageType(s) != null) {
          it[index] = ""
          val nextIndex = index + 1
          if (nextIndex < it.size && it[nextIndex].matches("an")) {
            it[nextIndex] = ""
          }
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun getAmpm(input: String) = when {
    input.matches(".*morgen.*") -> Ampm.MORNING
    input.matches(".*abend.*") -> Ampm.EVENING
    input.matches(".*mittag.*") -> Ampm.NOON
    input.matches(".*nacht.*") -> Ampm.NIGHT
    else -> null
  }

  override fun getMessageType(input: String): Action? {
    return if (input.matches(".*nachricht.*")) Action.MESSAGE
    else if (input.matches(".*brief.*") || input.matches(".*buchstabe.*")) Action.MAIL
    else null
  }

  override fun hasTimer(input: String): Boolean {
    return input.let { " $it " }.let {
      it.matches(".*nach.*") || it.matches(".*nach dem.*") || it.matches(".* in .*")
    }
  }

  override fun hasCall(input: String) = input.matches(".*anruf.*")

  override fun hasSender(input: String) = input.matches(".*senden.*")

  override fun hasRepeat(input: String) = input.matches(".*jeden.*") || hasEveryDay(input)

  override fun hasEveryDay(input: String) = input.matches(".*täglich.*")

  override fun clearAmpm(input: String): String? {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (getAmpm(s) != null) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun getShortTime(input: String?): Date? {
    return input?.let { s ->
      val matcher = Pattern.compile("([01]?[0-9]|2[0-3])( |:)[0-5][0-9]").matcher(s)
      var date: Date? = null
      if (matcher.find()) {
        val time = matcher.group().trim { it <= ' ' }
        for (format in hourFormats) {
          try {
            date = format.parse(time)
            if (date != null) break
          } catch (e: Exception) {
          }
        }
      }
      date
    }
  }

  override fun clearTime(input: String?): String {
    return input?.splitByWhitespaces()?.toMutableList()?.also {
      it.forEachIndexed { i, s ->
        if (hasHours(s) != -1) {
          val index = hasHours(s)
          it[i] = ""
          var hourSuccess = false
          try {
            it[i - index].toInt()
            hourSuccess = true
            it[i - index] = ""
          } catch (e: Exception) {
          }
          if (hourSuccess) {
            try {
              it[i + 1].toInt()
              it[i + 1] = ""
            } catch (e: Exception) {
            }
          }
        }
        if (hasMinutes(s) != -1) {
          val index = hasMinutes(s)
          try {
            it[i - index].toInt()
            it[i - index] = ""
          } catch (e: Exception) {
          }
          it[i] = ""
        }
      }
    }?.clip()?.let { s ->
      val matcher = Pattern.compile("([01]?[0-9]|2[0-3])( |:)[0-5][0-9]").matcher(s)
      if (matcher.find()) {
        val time = matcher.group().trim { it <= ' ' }
        s.replace(time, "")
      } else s
    }?.splitByWhitespaces()?.toMutableList()?.let { list ->
      val sb = StringBuilder()
      list.forEach { s ->
        if (!s.matches("bei")) sb.append(" ").append(s.trim { it <= ' ' })
      }
      sb.toString().trim { it <= ' ' }.replace("um", "")
    } ?: ""
  }

  override fun getMonth(input: String?): Int {
    return when {
      input == null -> -1
      input.contains("januar") -> 0
      input.contains("februar") -> 1
      input.contains("märz") -> 2
      input.contains("april") -> 3
      input.contains("mai") -> 4
      input.contains("juni") -> 5
      input.contains("juli") -> 6
      input.contains("august") -> 7
      input.contains("september") -> 8
      input.contains("oktober") -> 9
      input.contains("november") -> 10
      input.contains("dezember") -> 11
      else -> -1
    }
  }

  override fun clearCall(input: String): String? {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasCall(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun cleanTimer(input: String): String? {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasTimer(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip().trim { it <= ' ' }
  }

  override fun getDate(input: String, res: LongInternal): String? {
    var mills: Long = 0
    return input.splitByWhitespaces().toMutableList().also { list ->
      list.forEachIndexed { index, s ->
        val month = getMonth(s)
        if (month != -1) {
          val integer = try {
            list[index - 1].toInt().also { list[index - 1] = "" }
          } catch (e: Exception) {
            1
          }
          val calendar = Calendar.getInstance()
          calendar.timeInMillis = System.currentTimeMillis()
          calendar[Calendar.MONTH] = month
          calendar[Calendar.DAY_OF_MONTH] = integer
          mills = calendar.timeInMillis
          list[index] = ""
          return@forEachIndexed
        }
      }
    }.clip().replace("am", "").also {
      res.value = mills
    }
  }

  override fun clearSender(input: String): String? {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasSender(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun hasNote(input: String) = input.contains("notiz")

  override fun clearNote(input: String) = input.replace("notiz", "").trim { it <= ' ' }

  override fun hasAction(input: String): Boolean {
    return (input.startsWith("öffnen") || input.matches(".*hilfe.*")
      || input.matches(".*einstellen.*") || input.matches(".*bericht.*") ||
      input.matches(".*verändern.*"))
  }

  override fun getAction(input: String) = if (input.matches(".*hilfe.*")) {
    Action.HELP
  } else if (input.matches(".*lautstärke.*") || input.matches(".*volumen.*")) {
    Action.VOLUME
  } else if (input.matches(".*einstellungen.*")) {
    Action.SETTINGS
  } else if (input.matches(".*bericht.*")) {
    Action.REPORT
  } else {
    Action.APP
  }

  override fun hasEvent(input: String) = input.startsWith("neu") ||
    input.startsWith("hinzufügen") ||
    input.startsWith("addieren")

  override fun getEvent(input: String) = if (input.matches(".*geburtstag.*")) {
    Action.BIRTHDAY
  } else if (input.matches(".*erinnerung.*") || input.matches(".*mahnung.*")) {
    Action.REMINDER
  } else Action.NO_EVENT

  override fun hasEmptyTrash(input: String) = input.matches(".*klar trash.*")

  override fun hasDisableReminders(input: String) = input.matches(".*erinnerung deaktivieren.*")

  override fun hasGroup(input: String) = input.matches(".*gruppe hinzufügen.*")

  override fun clearGroup(input: String): String {
    val sb = StringBuilder()
    val parts: Array<String> = input.split(WHITESPACES).toTypedArray()
    var st = false
    for (s in parts) {
      if (s.matches(".*gruppe.*")) {
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

  override fun hasToday(input: String) = input.matches(".*heute.*")

  override fun hasAfterTomorrow(input: String): Boolean {
    return input.matches(".*übermorgen.*") || input.matches(".*nach morgen.*")
  }

  override fun clearAfterTomorrow(input: String): String {
    return when {
      input.matches(".*übermorgen.*") -> {
        input.replace("übermorgen", "")
      }
      input.matches(".*nach morgen.*") -> {
        input.replace("nach morgen", "")
      }
      else -> {
        input
      }
    }
  }

  override val afterTomorrow = "übermorgen"

  override fun hasHours(input: String?) =
    if (input?.matches(".*stunde.*") == true || input?.matches("uhr.*") == true) 1 else -1

  override fun hasMinutes(input: String?) = if (input?.matches(".*minute.*") == true) 1 else -1

  override fun hasSeconds(input: String?) = input?.matches(".*zweite.*") == true

  override fun hasDays(input: String?) = input?.matches(".*tag.*") == true

  override fun hasWeeks(input: String?) = input?.matches(".*woche.*") == true

  override fun hasMonth(input: String?) = input?.matches(".*monat.*") == true

  override fun hasAnswer(input: String) =
    input.let { " $it " }.matches(".* (ja|nein|kein|nicht) .*")

  override fun getAnswer(input: String) = if (input.matches(".* ?(ja) ?.*")) {
    Action.YES
  } else Action.NO

  override fun findFloat(input: String?) = input?.takeIf { it.matches("halb") }
    ?.let { 0.5f } ?: -1f

  override fun replaceNumbers(input: String?) = super.replaceNumbers(splitNumbers(input))

  private fun splitNumbers(input: String?): String? {
    println("splitNumbers: in -> $input")
    return (input?.splitByWhitespace()?.toMutableList() ?: mutableListOf()).also { list ->
      list.forEachIndexed { index, s ->
        if (hasNumber(s) != -1f && (s.contains("und") || s.contains("ein") || s.contains("in"))) {
          list[index] = s.replace("und", " ")
            .replace("ein", " ")
            .replace("in", " ")
        }
      }
    }.clip().also {
      println("splitNumbers: out -> $it")
    }
  }

  private val floats = listOf(
    "einhalb",
    "und eine halbe",
    "halbe",
    "halb"
  )

  override fun clearFloats(input: String?): String? {
    return input?.let { s -> s to floats.firstOrNull { s.contains(it) } }
      ?.let { it.first.replace(it.second ?: "", "") }
      ?: input
  }

  private fun hasNumber(input: String?): Float {
    var number = -1f
    if (input!!.contains("zero") || input.contains("null")) number = 0f
    else if (input.matches("ein(e|es|er|s)?") || input.contains("zuerst") || input.matches("erste.*")) number = 1f
    else if (input.contains("zwei") || input.matches("zweite.*")) number = 2f
    else if (input.contains("drei") || input.matches("dritte.*")) number = 3f
    else if (input.contains("vier") || input.matches("vierte.*")) number = 4f
    else if (input.contains("fünf") || input.matches("fünfte.*")) number = 5f
    else if (input.contains("sechs") || input.matches("sechste.*")) number = 6f
    else if (input.contains("sieben") || input.matches("siebte.*")) number = 7f
    else if (input.contains("acht") || input.matches("achte.*")) number = 8f
    else if (input.contains("neun") || input.matches("neunte.*")) number = 9f
    else if (input.contains("zehn") || input.matches("zehnte.*")) number = 10f
    else if (input.contains("elf") || input.matches("elfte.*")) number = 11f
    else if (input.contains("zwölf") || input.matches("zwölfte.*")) number = 12f
    else if (input.contains("dreizehn") || input.matches("dreizehnte.*")) number = 13f
    else if (input.contains("vierzehn") || input.matches("vierzehnte.*")) number = 14f
    else if (input.contains("fünfzehn") || input.matches("fünfzehnte.*")) number = 15f
    else if (input.contains("sechzehn") || input.matches("sechzehnte.*")) number = 16f
    else if (input.contains("siebzehn") || input.matches("siebzehnte.*")) number = 17f
    else if (input.contains("achtzehn") || input.matches("achtzehnte.*")) number = 18f
    else if (input.contains("neunzehn") || input.matches("neunzehnte.*")) number = 19f
    else if (input.contains("zwanzig") || input.contains("zwanzigste")) number = 20f
    else if (input.contains("dreißig") || input.contains("dreißigste")) number = 30f
    else if (input.contains("vierzig") || input.contains("vierzigste")) number = 40f
    else if (input.contains("fünfzig") || input.contains("fünfzigste")) number = 50f
    else if (input.contains("sechzig") || input.contains("sechzigste")) number = 60f
    else if (input.contains("siebzig") || input.contains("siebzigste")) number = 70f
    else if (input.contains("achtzig") || input.contains("achtzigste")) number = 80f
    else if (input.contains("neunzig") || input.contains("neunzigste")) number = 90f
    return number
  }

  override fun findNumber(input: String?): Float {
    if (input == null) return -1f
    return if (input.matches("zero") || input.matches("null")) 0f
    else if (input.matches("ein(e|es|er|s)?") || input.matches("zuerst") || input.matches("erste.*")) 1f
    else if (input.matches("zwei") || input.matches("zweite.*")) 2f
    else if (input.matches("drei") || input.matches("dritte.*")) 3f
    else if (input.matches("vier") || input.matches("vierte.*")) 4f
    else if (input.matches("fünf") || input.matches("fünfte.*")) 5f
    else if (input.matches("sechs") || input.matches("sechste.*")) 6f
    else if (input.matches("sieben") || input.matches("siebte.*")) 7f
    else if (input.matches("acht") || input.matches("achte.*")) 8f
    else if (input.matches("neun") || input.matches("neunte.*")) 9f
    else if (input.matches("zehn") || input.matches("zehnte.*")) 10f
    else if (input.matches("elf") || input.matches("elfte.*")) 11f
    else if (input.matches("zwölf") || input.matches("zwölfte.*")) 12f
    else if (input.matches("dreizehn") || input.matches("dreizehnte.*")) 13f
    else if (input.matches("vierzehn") || input.matches("vierzehnte.*")) 14f
    else if (input.matches("fünfzehn") || input.matches("fünfzehnte.*")) 15f
    else if (input.matches("sechzehn") || input.matches("sechzehnte.*")) 16f
    else if (input.matches("siebzehn") || input.matches("siebzehnte.*")) 17f
    else if (input.matches("achtzehn") || input.matches("achtzehnte.*")) 18f
    else if (input.matches("neunzehn") || input.matches("neunzehnte.*")) 19f
    else if (input.matches("zwanzig") || input.matches("zwanzigste")) 20f
    else if (input.matches("dreißig") || input.matches("dreißigste")) 30f
    else if (input.matches("vierzig") || input.matches("vierzigste")) 40f
    else if (input.matches("fünfzig") || input.matches("fünfzigste")) 50f
    else if (input.matches("sechzig") || input.matches("sechzigste")) 60f
    else if (input.matches("siebzig") || input.matches("siebzigste")) 70f
    else if (input.matches("achtzig") || input.matches("achtzigste")) 80f
    else if (input.matches("neunzig") || input.matches("neunzigste")) 90f
    else -1f
  }

  override fun hasShowAction(input: String) =
    input.matches(".*show.*") || input.matches(".*zeigen.*")

  override fun getShowAction(input: String): Action? {
    return when {
      input.matches(".*geburtstage.*") -> Action.BIRTHDAYS
      input.matches(".*aktive erinnerungen.*") -> Action.ACTIVE_REMINDERS
      input.matches(".*erinnerungen.*") -> Action.REMINDERS
      input.matches(".*veranstaltungen.*") -> Action.EVENTS
      input.matches(".*notizen.*") -> Action.NOTES
      input.matches(".*gruppen.*") -> Action.GROUPS
      input.matches(".*einkaufslisten?.*") -> Action.SHOP_LISTS
      else -> Action.NONE
    }
  }

  override fun hasNextModifier(input: String) = input.matches(".*nächste.*")
}