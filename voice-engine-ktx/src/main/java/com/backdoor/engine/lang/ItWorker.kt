package com.backdoor.engine.lang

import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import com.backdoor.engine.misc.ContactsInterface
import com.backdoor.engine.misc.Logger
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import java.util.regex.Pattern

internal class ItWorker(zoneId: ZoneId, contactsInterface: ContactsInterface?) :
  Worker(zoneId, contactsInterface) {

  override val weekdays: List<String> = listOf(
    "domenica",
    "lunedì",
    "martedì",
    "mercoledì",
    "giovedì",
    "venerdì",
    "sabato"
  )

  override fun hasCalendar(input: String): Boolean = input.matches(".*calendario.*")

  override fun clearCalendar(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches(".*calendario.*")) {
          it[index] = ""
          clearAllBackward(it, index - 1, 3, "(un|al|aggiungere|il|aggiungerlo|e)")
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
            clearAllBackward(it, index - 1, 1, "(al|e)")
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
    input.matches(".*ogni.*") || hasEveryDay(input)

  override fun hasEveryDay(input: String): Boolean = input.matches(".*tutti i giorn(o|i).*")

  override fun clearRepeat(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasRepeat(s)) {
          it[index] = ""
          clearAllForward(it, index + 1, 1, "giorni?")
          return@forEachIndexed
        } else if (s.matches("tutti") && hasEveryDay(input)) {
          it[index] = ""
          clearAllForward(it, index + 1, 2, "(i|giorni?)")
          return@forEachIndexed
        }
      }
    }.clip().splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches("ripetere")) {
          it[index] = ""
          clearAllBackward(it, index - 1, 1, "e")
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun hasTomorrow(input: String): Boolean =
    input.matches(".*domani.*") || input.matches(".*giorno successivo.*")

  override fun clearTomorrow(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches(".*domani.*") || s.matches(".*giorno successivo.*")) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun getMessage(input: String): String {
    val sb = StringBuilder()
    var isStart = false
    return if (input.endsWith("testo")) {
      input.splitByWhitespaces().toMutableList().also {
        it.forEachIndexed { index, s ->
          if (s.matches("testo")) {
            it[index] = ""
            clearAllBackward(it, index - 1, 1, "(con|del)")
          }
        }
      }.clip()
    } else {
      input.splitByWhitespaces().forEach {
        if (isStart) sb.append(" ").append(it)
        if (it.matches("testo") || it.matches("messaggio")) isStart = true
      }.let {
        sb.toString().trim()
      }
    }
  }

  override fun clearMessage(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches("testo")) {
          ignoreAny {
            if (it[index - 1].matches("con")) {
              it[index - 1] = ""
            }
          }
          it[index] = ""
        }
      }
    }.clip()
  }

  override fun getMessageType(input: String): Action? = when {
    input.matches(".*messaggio.*") -> Action.MESSAGE
    input.matches(".*lettera.*") || input.matches("posta elettronica") ||
      input.matches(".*e?( |-)?mail.*") || input.matches("carta") -> Action.MAIL
    else -> null
  }

  override fun clearMessageType(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (getMessageType(s) != null) {
          it[index] = ""
          clearAllForward(it, index + 1, 1, "(a|un)")
          return@also
        }
      }
    }.clip()
  }

  override fun getAmpm(input: String): Ampm? = when {
    input.matches(".*mattin(a|o).*") -> Ampm.MORNING
    input.matches(".*sera.*") -> Ampm.EVENING
    input.matches(".*mezzogiorno.*") -> Ampm.NOON
    input.matches(".*notte?.*") -> Ampm.NIGHT
    else -> null
  }

  override fun clearAmpm(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (getAmpm(s) != null) {
          it[index] = ""
          clearAllBackward(it, index - 1, 1, "(a|la|al|di)")
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
          clearAllBackward(it, index - 1, 2, "alle")
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
            clearAllBackward(it, i - 1, 2, "(il|a|su|alle)")
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
            clearAllBackward(it, i - 1, 1, "(a|su|alle)")
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
    input.contains("gennaio") -> 1
    input.contains("febbraio") -> 2
    input.contains("marzo") -> 3
    input.contains("aprile") -> 4
    input.contains("maggio") -> 5
    input.contains("giugno") -> 6
    input.contains("luglio") -> 7
    input.contains("agosto") -> 8
    input.contains("settembre") -> 9
    input.contains("ottobre") -> 10
    input.contains("novembre") -> 11
    input.contains("dicembre") -> 12
    else -> -1
  }

  override fun hasCall(input: String): Boolean = input.matches(".*chiama.*") ||
    input.matches(".*telefona.*")

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

  override fun hasTimer(input: String): Boolean = input.let { " $it " }.matches(".*dopo.*")

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
            .withMonth(month)
            .withDayOfMonth(dayOfMonth.toInt())

          if (parsedDate.isBefore(LocalDate.now(zoneId))) {
            parsedDate = parsedDate.plusYears(1)
          }

          localDate = parsedDate
          list[index] = ""
          clearAllBackward(list, index - 1, 2, "(a|su|il|del)")
          return@forEachIndexed
        }
      }
    }.clip().also {
      result(localDate)
    }
  }

  override fun hasSender(input: String): Boolean = input.matches(".*invia(re)?.*") ||
    input.matches(".*manda.*") || input.matches(".*spedisci.*")

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

  override fun hasNote(input: String): Boolean = input.contains("nota")

  override fun clearNote(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches(".*nota.*")) {
          it[index] = ""
          clearAllBackward(it, index - 1, 3, "(creare|una|nuova)")
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun hasAction(input: String): Boolean {
    return input.matches(".*aprire.*") || input.matches(".*aperta.*")
      || input.matches(".*aiuto.*") || input.matches(".*regolare.*")
      || input.matches(".*rapporto.*") || input.matches(".*modifica.*")
  }

  override fun getAction(input: String): Action = when {
    input.matches(".*aiuto.*") -> Action.HELP
    input.matches(".*rumorosità?.*") || input.matches(".*volume.*") -> Action.VOLUME
    input.matches(".*impostazioni.*") || input.matches(".*configurazion.*") -> Action.SETTINGS
    input.matches(".*rapporto.*") -> Action.REPORT
    else -> Action.APP
  }

  override fun hasEvent(input: String): Boolean = input.matches(".*nuovo?.*") ||
    input.startsWith("aggiungere") || input.startsWith("creare")

  override fun getEvent(input: String): Action = when {
    input.matches(".*compleanno.*") -> Action.BIRTHDAY
    input.matches(".*promemoria.*") -> Action.REMINDER
    else -> Action.NO_EVENT
  }

  override fun hasEmptyTrash(input: String): Boolean =
    input.matches(".*(vuot|chiar|pulire la)?o?a? ?(spazzatura|promemoria completati)( vuota)?.*")

  override fun hasDisableReminders(input: String): Boolean =
    input.matches(".*(disabilita|disattivare) (tutti i )?promemoria.*")

  override fun hasGroup(input: String): Boolean = input.matches(".*(aggiungere|creare|nuovo?).* gruppo.*")

  override fun clearGroup(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches(".*gruppo.*")) {
          it[index] = ""
          clearAllBackward(it, index - 1, 1, "(un|di)")
          clearAllForward(it, 0, 3, "(aggiungere|creare|nuovo?)")
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun hasToday(input: String): Boolean = input.matches(".*oggi.*")

  override fun hasAfterTomorrow(input: String): Boolean = input.matches(".*dopodomani.*")

  override val afterTomorrow: String = "dopodomani"

  override fun hasHours(input: String?): Int = when {
    input == null -> -1
    " $input ".matches(".* or(a|e).*") || getAmpm(input) != null -> 1
    else -> -1
  }

  override fun hasMinutes(input: String?): Int = when {
    input.matchesOrFalse(".*minut(o|a|i).*") -> 1
    else -> -1
  }

  override fun hasSeconds(input: String?): Boolean = input.matchesOrFalse(".*second(i|o|a).*")

  override fun hasDays(input: String?): Boolean = input.matchesOrFalse(".*giorn(i|o).*") &&
    !input.matchesOrFalse(".*compleann(o|i).*")

  override fun hasWeeks(input: String?): Boolean = input.matchesOrFalse(".*settiman(a|e).*")

  override fun hasMonth(input: String?): Boolean = input.matchesOrFalse(".*mes(e|i).*")

  override fun hasAnswer(input: String): Boolean {
    return " $input ".matches(".* (si|no),? .*")
  }

  override fun getAnswer(input: String): Action = when {
    input.matches(".* ?si,? ?.*") -> Action.YES
    else -> Action.NO
  }

  override fun findFloat(input: String?): Float = input
    ?.takeIf { it.matches("(metà|mezzo|mezz')") }
    ?.let { 0.5f }
    ?: -1f

  override fun clearFloats(input: String?): String? = when {
    input == null -> null
    input.contains("e mezzo") -> input.replace("e mezzo", "")
    input.contains("e metà") -> input.replace("e metà", "")
    input.contains(" mezzo ") -> input.replace("mezzo", "")
    input.contains(" a metà ") -> input.replace("a metà", "")
    else -> input
  }

  override fun findNumber(input: String?): Float = when {
    input == null -> -1f
    input.matches("zero") -> 0f
    input.matches("uno") || input.matches("prim$OA") -> 1f
    input.matches("due") || input.matches("second$OA") -> 2f
    input.matches("tre") || input.matches("terz$OA") -> 3f
    input.matches("quattro") || input.matches("quarto") -> 4f
    input.matches("cinque") || input.matches("quint$OA") -> 5f
    input.matches("sei") || input.matches("sest$OA") -> 6f
    input.matches("sette") || input.matches("settim$OA") -> 7f
    input.matches("otto") || input.matches("ottav$OA") -> 8f
    input.matches("nove") || input.matches("non$OA") -> 9f
    input.matches("dieci") || input.matches("decina") || input.matches("decim$OA") -> 10f
    input.matches("undici") || input.matches("undicesim$OA") -> 11f
    input.matches("dodici") || input.matches("dodicesim$OA") -> 12f
    input.matches("tredici") || input.matches("tredicesim$OA") -> 13f
    input.matches("quattordici") || input.matches("quattordicesim$OA") -> 14f
    input.matches("quindici") || input.matches("quindicesim$OA") -> 15f
    input.matches("sedici") || input.matches("sedicesim$OA") -> 16f
    input.matches("diciassette") || input.matches("diciassettesim$OA") -> 17f
    input.matches("diciotto") || input.matches("diciottesim$OA") -> 18f
    input.matches("diciannove") || input.matches("diciannovesim$OA") -> 19f
    input.matches("venti") || input.matches("ventesim$OA") -> 20f
    input.matches("trenta") || input.matches("trentesim$OA") -> 30f
    input.matches("quaranta") || input.matches("quarantesim$OA") -> 40f
    input.matches("cinquanta") || input.matches("cinquantesim$OA") -> 50f
    input.matches("sessanta") || input.matches("sessantesim$OA") -> 60f
    input.matches("settanta") || input.matches("settantesim$OA") -> 70f
    input.matches("ottanta") || input.matches("ottantesim$OA") -> 80f
    input.matches("novanta") || input.matches("novantesim$OA") -> 90f
    else -> -1f
  }

  override fun hasShowAction(input: String): Boolean = input.matches(".*spettacol.*") ||
    input.matches(".*mostra.*")

  override fun getShowAction(input: String): Action? = when {
    input.matches(".*compleann(i|o).*") -> Action.BIRTHDAYS
    input.matches(".*promemoria attivi.*") -> Action.ACTIVE_REMINDERS
    input.matches(".*promemoria.*") -> Action.REMINDERS
    input.matches(".*eventi.*") -> Action.EVENTS
    input.matches(".*appunti.*") || input.matches(".* note.*") -> Action.NOTES
    input.matches(".*gruppi.*") -> Action.GROUPS
    input.matches(".*list(e|a) della spesa.*") -> Action.SHOP_LISTS
    else -> null
  }

  override fun hasNextModifier(input: String): Boolean = input.matches(".*prossim$OA.*")

  override fun hasConnectSpecialWord(input: String): Boolean {
    return input.matches("(a|su)")
  }

  override fun splitWords(input: String?): String {
    val result = mutableListOf<String>()
    input?.splitByWhitespaces()
      ?.forEach {
        if (it.matches("mezz'ora.*")) {
          result.add("mezzo")
          result.add("ora")
        } else if (it.startsWith("l'")) {
          val digit = it.substring(2).toIntOrNull()
          if (digit != null) {
            result.add(it.substring(2))
          } else {
            result.add(it)
          }
        } else if (it.contains("°")) {
          result.add(it.replace("°", ""))
        } else {
          result.add(it)
        }
      }
    return result.clip()
  }

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
        var hour = -1f
        var hourSuccess = false
        ignoreAny {
          hour = parts[i - 1].toFloat()
          hourSuccess = true
          parts[i - 1] = ""
        }
        if (hour == -1f) {
          ignoreAny {
            hour = parts[i - 2].toFloat()
            hourSuccess = true
            parts[i - 2] = ""
          }
        }
        if (ampm == Ampm.EVENING) {
          hour += 12f
        }
        h = hour
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

  companion object {
    private const val OA = "(o|a)"
  }
}
