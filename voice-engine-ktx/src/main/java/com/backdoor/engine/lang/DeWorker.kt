package com.backdoor.engine.lang

import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
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
    input.splitByWhitespaces().toMutableList().let {
      it.forEachIndexed { index, s ->
        for (day in weekdays) {
          if (s.matches(".*$day.*")) {
            it[index] = ""
            break
          }
        }
      }
      it
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
    return input.splitByWhitespaces().toMutableList().let {
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
      it
    }.clip()
  }

  override fun clearRepeat(input: String): String? {
    return input.splitByWhitespaces().toMutableList().let {
      it.forEachIndexed { index, s ->
        if (hasRepeat(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
      it
    }.clip()
  }

  override fun hasTomorrow(input: String) = input.matches(".*morgen.*")

  override fun clearTomorrow(input: String): String? {
    return input.splitByWhitespaces().toMutableList().let {
      it.forEachIndexed { index, s ->
        if (s.matches(".*morgen.*")) {
          it[index] = ""
          return@forEachIndexed
        }
      }
      it
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
    return input.splitByWhitespaces().toMutableList().let {
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
      it
    }.clip()
  }

  override fun clearMessageType(input: String): String? {
    val parts: Array<String?> = input.split(WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      val type = part?.toMessageType()
      if (type != null) {
        parts[i] = ""
        val nextIndex = i + 1
        if (nextIndex < parts.size && parts[nextIndex]!!.matches("an")) {
          parts[nextIndex] = ""
        }
        break
      }
    }
    return clipStrings(parts)
  }

  override fun String.toAmpm() = when {
    matches(".*morgen.*") -> Ampm.MORNING
    matches(".*abend.*") -> Ampm.EVENING
    matches(".*mittag.*") -> Ampm.NOON
    matches(".*nacht.*") -> Ampm.NIGHT
    else -> null
  }

  override fun String.toMessageType(): Action? {
    return if (matches(".*nachricht.*")) Action.MESSAGE
    else if (matches(".*brief.*") || matches(".*buchstabe.*")) Action.MAIL
    else null
  }

  override fun String.isTimer(): Boolean {

  }

  override fun String.hasCall(): Boolean {

  }

  override fun String.hasEveryDay() = matches(".*täglich.*")

  override fun String.hasRepeat() = matches(".*jeden.*") || hasEveryDay()

  override fun String.hasSender(): Boolean {

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
        var hourSuccess = false
        try {
          parts[i - index]!!.toInt()
          hourSuccess = true
          parts[i - index] = ""
        } catch (ignored: Exception) {
        }
        if (hourSuccess) {
          try {
            parts[i + 1]!!.toInt()
            parts[i + 1] = ""
          } catch (ignored: Exception) {
          }
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
      if (!part.matches("bei")) sb.append(" ").append(part)
    }
    return sb.toString().trim { it <= ' ' }.replace("um", "")
  }

  override fun getMonth(input: String?): Int {
    var res = -1
    if (input!!.contains("januar")) res = 0
    if (input.contains("februar")) res = 1
    if (input.contains("märz")) res = 2
    if (input.contains("april")) res = 3
    if (input.contains("mai")) res = 4
    if (input.contains("juni")) res = 5
    if (input.contains("juli")) res = 6
    if (input.contains("august")) res = 7
    if (input.contains("september")) res = 8
    if (input.contains("oktober")) res = 9
    if (input.contains("november")) res = 10
    if (input.contains("dezember")) res = 11
    return res
  }

  override fun hasCall(input: String?): Boolean {
    return input!!.matches(".*anruf.*")
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
    return input.matches(".*nach.*") || input.matches(".*nach dem.*") || input.matches(".* in .*")
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
    return clipStrings(parts).trim { it <= ' ' }
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
          integer = parts[i - 1]!!.toInt()
          parts[i - 1] = ""
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
    return clipStrings(parts).replace("am", "")
  }

  override fun hasSender(input: String?): Boolean {
    return input!!.matches(".*senden.*")
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
    return input.contains("notiz")
  }

  override fun clearNote(input: String): String {
    var input = input
    input = input.replace("notiz", "")
    return input.trim { it <= ' ' }
  }

  override fun hasAction(input: String): Boolean {
    return (input.startsWith("öffnen") || input.matches(".*hilfe.*")
      || input.matches(".*einstellen.*") || input.matches(".*bericht.*") ||
      input.matches(".*verändern.*"))
  }

  override fun getAction(input: String): Action? {
    return if (input.matches(".*hilfe.*")) {
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
  }

  override fun hasEvent(input: String): Boolean {
    return input.startsWith("neu") || input.startsWith("hinzufügen") || input.startsWith("addieren")
  }

  override fun getEvent(input: String): Action? {
    return if (input.matches(".*geburtstag.*")) {
      Action.BIRTHDAY
    } else if (input.matches(".*erinnerung.*") || input.matches(".*mahnung.*")) {
      Action.REMINDER
    } else Action.NO_EVENT
  }

  override fun hasEmptyTrash(input: String): Boolean {
    return input.matches(".*klar trash.*")
  }

  override fun hasDisableReminders(input: String): Boolean {
    return input.matches(".*erinnerung deaktivieren.*")
  }

  override fun hasGroup(input: String): Boolean {
    return input.matches(".*gruppe hinzufügen.*")
  }

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

  override fun hasToday(input: String): Boolean {
    return input.matches(".*heute.*")
  }

  override fun hasAfterTomorrow(input: String): Boolean {
    return input.matches(".*übermorgen.*") || input.matches(".*nach morgen.*")
  }

  override fun clearAfterTomorrow(input: String): String {
    return if (input.matches(".*übermorgen.*")) {
      input.replace("übermorgen", "")
    } else if (input.matches(".*nach morgen.*")) {
      input.replace("nach morgen", "")
    } else {
      input
    }
  }

  protected override val afterTomorrow: String
    protected get() = "übermorgen"

  override fun hasHours(input: String?): Int {
    return if (input!!.matches(".*stunde.*") || input.matches("uhr.*")) 1 else -1
  }

  override fun hasMinutes(input: String?): Int {
    return if (input!!.matches(".*minute.*")) 1 else -1
  }

  override fun hasSeconds(input: String?): Boolean {
    return input!!.matches(".*zweite.*")
  }

  override fun hasDays(input: String?): Boolean {
    return input!!.matches(".*tag.*")
  }

  override fun hasWeeks(input: String?): Boolean {
    return input!!.matches(".*woche.*")
  }

  override fun hasMonth(input: String?): Boolean {
    return input!!.matches(".*monat.*")
  }

  override fun hasAnswer(input: String): Boolean {
    var input = input
    input = " $input "
    return input.matches(".* (ja|nein|kein|nicht) .*")
  }

  override fun getAnswer(input: String): Action? {
    return if (input.matches(".* ?(ja) ?.*")) {
      Action.YES
    } else Action.NO
  }

  override fun findFloat(input: String?): Float {
    return if (input!!.matches("halb")) {
      0.5f
    } else {
      (-1).toFloat()
    }
  }

  override fun replaceNumbers(input: String?): String? {
    return super.replaceNumbers(splitNumbers(input))
  }

  private fun splitNumbers(input: String?): String? {
    var input = input
    val parts: Array<String?> = input!!.split("\\s").toTypedArray()
    for (i in parts.indices) {
      var part = parts[i]
      if (hasNumber(part) != -1f && (part!!.contains("und") || part.contains("ein") || part.contains("in"))) {
        part = part.replace("und", " ")
        part = part.replace("ein", " ")
        part = part.replace("in", " ")
        parts[i] = part
      }
    }
    println("splitNumbers: in -> $input")
    input = clipStrings(parts)
    println("splitNumbers: out -> $input")
    return input
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