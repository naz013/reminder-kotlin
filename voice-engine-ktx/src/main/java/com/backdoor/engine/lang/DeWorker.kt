package com.backdoor.engine.lang

import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import com.backdoor.engine.misc.ContactsInterface
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import java.util.regex.Pattern

internal class DeWorker(zoneId: ZoneId, contactsInterface: ContactsInterface?) :
  Worker(zoneId, contactsInterface) {

  override val weekdays: List<String> = listOf(
    "sonntag",
    "montag",
    "dienstag",
    "mittwoch",
    "donnerstag",
    "freitag",
    "samstag"
  )

  override fun hasCalendar(input: String): Boolean = input.matches(".*kalender.*")

  override fun clearCalendar(input: String): String {
    return input.splitByWhitespaces()
      .toMutableList()
      .also {
        it.forEachIndexed { index, s ->
          if (s.matches(".*kalender.*")) {
            it[index] = ""
            if (index > 0 && it[index - 1].equals("zum", ignoreCase = true)) {
              it[index - 1] = ""
            }
            return@forEachIndexed
          }
        }
      }.clip()
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
      if (!part.matches("zum")) sb.append(" ").append(part)
    }
    return sb.toString().trim()
  }

  override fun getDaysRepeat(input: String): Long =
    input.splitByWhitespaces().firstOrNull { hasDays(it) }?.toRepeat(1) ?: 0

  override fun clearDaysRepeat(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
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

  override fun clearRepeat(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasRepeat(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun hasTomorrow(input: String): Boolean = input.matches(".*morgen.*")

  override fun clearTomorrow(input: String): String {
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
      sb.toString().trim()
    }
  }

  override fun clearMessage(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches("text")) {
          ignoreAny {
            if (it[index - 1].matches("mit")) it[index - 1] = ""
          }
          it[index] = ""
        }
      }
    }.clip()
  }

  override fun clearMessageType(input: String): String {
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

  override fun getAmpm(input: String): Ampm? = when {
    input.matches(".*morgen.*") -> Ampm.MORNING
    input.matches(".*abend.*") -> Ampm.EVENING
    input.matches(".*mittag.*") -> Ampm.NOON
    input.matches(".*nacht.*") -> Ampm.NIGHT
    else -> null
  }

  override fun getMessageType(input: String): Action? = when {
    input.matches(".*nachricht.*") -> Action.MESSAGE
    input.matches(".*brief.*") || input.matches(".*buchstabe.*") -> Action.MAIL
    else -> null
  }

  override fun hasTimer(input: String): Boolean = input.let { " $it " }.let {
    it.matches(".*nach.*") || it.matches(".*nach dem.*") || it.matches(".* in .*")
  }

  override fun hasCall(input: String): Boolean = input.matches(".*anruf.*")

  override fun hasSender(input: String): Boolean = input.matches(".*senden.*")

  override fun hasRepeat(input: String): Boolean = input.matches(".*jeden.*") || hasEveryDay(input)

  override fun hasEveryDay(input: String): Boolean = input.matches(".*täglich.*")

  override fun clearAmpm(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (getAmpm(s) != null) {
          it[index] = ""
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
    return input?.splitByWhitespaces()?.toMutableList()?.also {
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
        if (!s.matches("bei")) sb.append(" ").append(s.trim())
      }
      sb.toString().trim().replace("um", "")
    } ?: ""
  }

  override fun getMonth(input: String?): Int {
    return when {
      input == null -> -1
      input.contains("januar") -> 1
      input.contains("februar") -> 2
      input.contains("märz") -> 3
      input.contains("april") -> 4
      input.contains("mai") -> 5
      input.contains("juni") -> 6
      input.contains("juli") -> 7
      input.contains("august") -> 8
      input.contains("september") -> 9
      input.contains("oktober") -> 10
      input.contains("november") -> 11
      input.contains("dezember") -> 12
      else -> -1
    }
  }

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
    }.clip().replace("am", "").also {
      result(localDate)
    }
  }

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

  override fun hasNote(input: String): Boolean = input.contains("notiz")

  override fun clearNote(input: String): String = input.replace("notiz", "").trim()

  override fun hasAction(input: String): Boolean {
    return (input.startsWith("öffnen") || input.matches(".*hilfe.*")
      || input.matches(".*einstellen.*") || input.matches(".*bericht.*") ||
      input.matches(".*verändern.*"))
  }

  override fun getAction(input: String): Action = when {
    input.matches(".*hilfe.*") -> Action.HELP
    input.matches(".*lautstärke.*") || input.matches(".*volumen.*") -> Action.VOLUME
    input.matches(".*einstellungen.*") -> Action.SETTINGS
    input.matches(".*bericht.*") -> Action.REPORT
    else -> Action.APP
  }

  override fun hasEvent(input: String): Boolean = input.startsWith("neu") ||
    input.startsWith("hinzufügen") ||
    input.startsWith("addieren")

  override fun getEvent(input: String): Action = when {
    input.matches(".*geburtstag.*") -> Action.BIRTHDAY
    input.matches(".*erinnerung.*") || input.matches(".*mahnung.*") -> Action.REMINDER
    else -> Action.NO_EVENT
  }

  override fun hasEmptyTrash(input: String): Boolean = input.matches(".*klar trash.*")

  override fun hasDisableReminders(input: String): Boolean = input.matches(".*erinnerung deaktivieren.*")

  override fun hasGroup(input: String): Boolean = input.matches(".*gruppe hinzufügen.*")

  override fun clearGroup(input: String): String {
    val sb = StringBuilder()
    val parts: Array<String> = input.splitByWhitespaces().toTypedArray()
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
    return sb.toString().trim()
  }

  override fun hasToday(input: String): Boolean = input.matches(".*heute.*")

  override fun hasAfterTomorrow(input: String): Boolean =
    input.matches(".*übermorgen.*") || input.matches(".*nach morgen.*")

  override fun clearAfterTomorrow(input: String): String = when {
    input.matches(".*übermorgen.*") -> input.replace("übermorgen", "")
    input.matches(".*nach morgen.*") -> input.replace("nach morgen", "")
    else -> input
  }

  override val afterTomorrow: String = "übermorgen"

  override fun hasHours(input: String?): Int = when {
    input.matchesOrFalse(".*stunde.*") || input.matchesOrFalse("uhr.*") -> 1
    else -> -1
  }

  override fun hasMinutes(input: String?): Int = when {
    input.matchesOrFalse(".*minute.*") -> 1
    else -> -1
  }

  override fun hasSeconds(input: String?): Boolean = input.matchesOrFalse(".*zweite.*")

  override fun hasDays(input: String?): Boolean = input.matchesOrFalse(".*tag.*")

  override fun hasWeeks(input: String?): Boolean = input.matchesOrFalse(".*woche.*")

  override fun hasMonth(input: String?): Boolean = input.matchesOrFalse(".*monat.*")

  override fun hasAnswer(input: String): Boolean = input.let { " $it " }.matches(".* (ja|nein|kein|nicht) .*")

  override fun getAnswer(input: String): Action = when {
    input.matches(".* ?(ja) ?.*") -> Action.YES
    else -> Action.NO
  }

  override fun findFloat(input: String?): Float = input?.takeIf { it.matches("halb") }
    ?.let { 0.5f } ?: -1f

  override fun replaceNumbers(input: String?): String? = super.replaceNumbers(splitNumbers(input))

  private fun splitNumbers(input: String?): String {
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

  private val floats: List<String> = listOf(
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

  private fun hasNumber(input: String?): Float = when {
    input == null -> -1f
    input.contains("zero") || input.contains("null") -> 0f
    input.matches("ein(e|es|er|s)?") || input.contains("zuerst") || input.matches("erste.*") -> 1f
    input.contains("zwei") || input.matches("zweite.*") -> 2f
    input.contains("drei") || input.matches("dritte.*") -> 3f
    input.contains("vier") || input.matches("vierte.*") -> 4f
    input.contains("fünf") || input.matches("fünfte.*") -> 5f
    input.contains("sechs") || input.matches("sechste.*") -> 6f
    input.contains("sieben") || input.matches("siebte.*") -> 7f
    input.contains("acht") || input.matches("achte.*") -> 8f
    input.contains("neun") || input.matches("neunte.*") -> 9f
    input.contains("zehn") || input.matches("zehnte.*") -> 10f
    input.contains("elf") || input.matches("elfte.*") -> 11f
    input.contains("zwölf") || input.matches("zwölfte.*") -> 12f
    input.contains("dreizehn") || input.matches("dreizehnte.*") -> 13f
    input.contains("vierzehn") || input.matches("vierzehnte.*") -> 14f
    input.contains("fünfzehn") || input.matches("fünfzehnte.*") -> 15f
    input.contains("sechzehn") || input.matches("sechzehnte.*") -> 16f
    input.contains("siebzehn") || input.matches("siebzehnte.*") -> 17f
    input.contains("achtzehn") || input.matches("achtzehnte.*") -> 18f
    input.contains("neunzehn") || input.matches("neunzehnte.*") -> 19f
    input.contains("zwanzig") || input.contains("zwanzigste") -> 20f
    input.contains("dreißig") || input.contains("dreißigste") -> 30f
    input.contains("vierzig") || input.contains("vierzigste") -> 40f
    input.contains("fünfzig") || input.contains("fünfzigste") -> 50f
    input.contains("sechzig") || input.contains("sechzigste") -> 60f
    input.contains("siebzig") || input.contains("siebzigste") -> 70f
    input.contains("achtzig") || input.contains("achtzigste") -> 80f
    input.contains("neunzig") || input.contains("neunzigste") -> 90f
    else -> -1f
  }

  override fun findNumber(input: String?): Float = when {
    input == null -> -1f
    input.matches("zero") || input.matches("null") -> 0f
    input.matches("ein(e|es|er|s)?") || input.matches("zuerst") || input.matches("erste.*") -> 1f
    input.matches("zwei") || input.matches("zweite.*") -> 2f
    input.matches("drei") || input.matches("dritte.*") -> 3f
    input.matches("vier") || input.matches("vierte.*") -> 4f
    input.matches("fünf") || input.matches("fünfte.*") -> 5f
    input.matches("sechs") || input.matches("sechste.*") -> 6f
    input.matches("sieben") || input.matches("siebte.*") -> 7f
    input.matches("acht") || input.matches("achte.*") -> 8f
    input.matches("neun") || input.matches("neunte.*") -> 9f
    input.matches("zehn") || input.matches("zehnte.*") -> 10f
    input.matches("elf") || input.matches("elfte.*") -> 11f
    input.matches("zwölf") || input.matches("zwölfte.*") -> 12f
    input.matches("dreizehn") || input.matches("dreizehnte.*") -> 13f
    input.matches("vierzehn") || input.matches("vierzehnte.*") -> 14f
    input.matches("fünfzehn") || input.matches("fünfzehnte.*") -> 15f
    input.matches("sechzehn") || input.matches("sechzehnte.*") -> 16f
    input.matches("siebzehn") || input.matches("siebzehnte.*") -> 17f
    input.matches("achtzehn") || input.matches("achtzehnte.*") -> 18f
    input.matches("neunzehn") || input.matches("neunzehnte.*") -> 19f
    input.matches("zwanzig") || input.matches("zwanzigste") -> 20f
    input.matches("dreißig") || input.matches("dreißigste") -> 30f
    input.matches("vierzig") || input.matches("vierzigste") -> 40f
    input.matches("fünfzig") || input.matches("fünfzigste") -> 50f
    input.matches("sechzig") || input.matches("sechzigste") -> 60f
    input.matches("siebzig") || input.matches("siebzigste") -> 70f
    input.matches("achtzig") || input.matches("achtzigste") -> 80f
    input.matches("neunzig") || input.matches("neunzigste") -> 90f
    else -> -1f
  }

  override fun hasShowAction(input: String): Boolean =
    input.matches(".*show.*") || input.matches(".*zeigen.*")

  override fun getShowAction(input: String): Action = when {
    input.matches(".*geburtstage.*") -> Action.BIRTHDAYS
    input.matches(".*aktive erinnerungen.*") -> Action.ACTIVE_REMINDERS
    input.matches(".*erinnerungen.*") -> Action.REMINDERS
    input.matches(".*veranstaltungen.*") -> Action.EVENTS
    input.matches(".*notizen.*") -> Action.NOTES
    input.matches(".*gruppen.*") -> Action.GROUPS
    input.matches(".*einkaufslisten?.*") -> Action.SHOP_LISTS
    else -> Action.NONE
  }

  override fun hasNextModifier(input: String): Boolean = input.matches(".*nächste.*")
}
