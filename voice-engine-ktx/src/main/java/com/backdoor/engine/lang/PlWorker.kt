package com.backdoor.engine.lang

import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import com.backdoor.engine.misc.Logger
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import java.util.regex.Pattern

internal class PlWorker(zoneId: ZoneId) : Worker(zoneId) {

  companion object {
    const val CALENDAR_KEY = "kalendarz"
  }

  override val weekdays = listOf(
    "niedzie",
    "poniedzia",
    "wtor",
    "środ",
    "czwart",
    "piąt",
    "sobot"
  )

  override fun hasCalendar(input: String) = input.matches(".*$CALENDAR_KEY.*")

  override fun clearCalendar(input: String) =
    input.splitByWhitespaces()
      .toMutableList()
      .let {
        it.forEachIndexed { index, s ->
          if (s.matches(".*$CALENDAR_KEY.*")) {
            it[index] = ""
            if (index > 0 && (it[index - 1].equals("do", ignoreCase = true) ||
                it[index - 1].matches("doda(ć|j)"))) {
              it[index - 1] = ""
            }
            if (index > 1 && (it[index - 2].equals("do", ignoreCase = true) ||
                it[index - 2].matches("doda(ć|j)"))) {
              it[index - 2] = ""
            }
            return@forEachIndexed
          }
        }
        it.clip()
      }

  override fun clearWeekDays(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        for (day in weekdays) {
          if (s.matches(".*$day.*")) {
            it[index] = ""
            if (index > 0) {
              if (it[index - 1].matches("we?") || it[index - 1].matches("i")) {
                it[index - 1] = ""
              } else if (it[index - 1].matches("każd(y|e)")) {
                it[index - 1] = ""
                if (index > 1 && it[index - 2].matches("we?")) {
                  it[index - 2] = ""
                }
              }
            }
            break
          }
        }
      }
    }.clip()
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

  override fun hasRepeat(input: String) = input.matches(".*każdego.*") ||
    input.matches(".*powtarza(j|ć).*") || hasEveryDay(input)

  override fun hasEveryDay(input: String) =
    input.matches(".*codzie(n|ń).*") || input.matches(".*każdego dnia.*")

  override fun clearRepeat(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasRepeat(s)) {
          it[index] = ""
          if (index > 0 && it[index - 1].matches("i")) {
            it[index - 1] = ""
          }
          return@forEachIndexed
        }
      }
    }.clip()

  override fun hasTomorrow(input: String) = input.matches(".*jutr(o|a).*")

  override fun clearTomorrow(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches(".*jutr(o|a).*")) {
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
      if (it.matches("tekst(em)?") || it.matches("treścią")) isStart = true
    }.let {
      sb.toString().trim()
    }
  }

  override fun clearMessage(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches("tekst(em)?")) {
          ignoreAny {
            if (it[index - 1].matches("z")) it[index - 1] = ""
          }
          it[index] = ""
        }
      }
    }.clip()

  override fun getMessageType(input: String) = when {
    input.matches(".*wiadomoś.*") -> Action.MESSAGE
    input.matches(".*list.*") -> Action.MAIL
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
    input.matches(".*wcześnie.*") || input.matches(".*rankiem.*") ||
      input.matches(".*rano.*") -> Ampm.MORNING
    input.matches(".*wiecz(orem|ór).*") -> Ampm.EVENING
    input.matches(".*dzień.*") -> Ampm.NOON
    input.matches(".*nocy?.*") -> Ampm.NIGHT
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

  override fun getMonth(input: String?) = when {
    input == null -> -1
    input.contains("styczeń") || input.contains("styczn") -> 1
    input.contains("luty") || input.contains("lutego") -> 2
    input.contains("marzec") || input.contains("marca") -> 3
    input.contains("kwiecień") || input.contains("kwietnia") -> 4
    input.contains("maj") || input.contains("maja") -> 5
    input.contains("czerwiec") || input.contains("czerwca") -> 6
    input.contains("lipiec") || input.contains("lipca") -> 7
    input.contains("sierpień") || input.contains("sierpnia") -> 8
    input.contains("wrzesień") || input.contains("września") -> 9
    input.contains("październik") || input.contains("października") -> 10
    input.contains("listopad") || input.contains("listopada") -> 11
    input.contains("grudzień") || input.contains("grudnia") -> 12
    else -> -1
  }

  override fun hasCall(input: String) = input.matches(".*(za)?dzwo(n|ń).*")

  override fun clearCall(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasCall(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()

  override fun hasTimer(input: String) = input.matches(".* za .*")

  override fun cleanTimer(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches("za")) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip().trim()

  override fun hasSender(input: String) = input.matches(".*wyś.*") ||
    input.matches(".*wyslij.*")

  override fun clearSender(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasSender(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()

  override fun hasNote(input: String) = input.contains("notatka")

  override fun clearNote(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches(".*notatk(ę|a).*")) {
          it[index] = ""
          if (index > 0 && (it[index - 1].matches(".*nowa?.*") || it[index - 1].matches(".*doda(j|ć).*"))) {
            it[index - 1] = ""
          }
          return@forEachIndexed
        }
      }
    }.clip()

  override fun hasAction(input: String): Boolean {
    return (input.matches(".*otw(orzyć|órz).*") || input.matches(".*pomoc.*")
      || input.matches(".*głośno.*") || input.matches(".*ustawienia.*")
      || input.matches(".*poinform.*") || input.matches(".*zgło.*"))
  }

  override fun getAction(input: String) = when {
    input.matches(".*pomoc.*") -> Action.HELP
    input.matches(".*głośno.*") -> Action.VOLUME
    input.matches(".*ustawienia.*") -> Action.SETTINGS
    input.matches(".*poinform.*") || input.matches(".*zgło.*") -> Action.REPORT
    else -> Action.APP
  }

  override fun hasEvent(input: String) =
    input.startsWith("doda") || input.matches("nowy?a?e?.*")

  override fun hasEmptyTrash(input: String) = input.matches(".*opróżni(ć|j)? kosz.*")

  override fun hasDisableReminders(input: String) =
    input.matches(".*wyłącz(yć)? (wszystkie)? ?przypomnien.*")

  override fun hasGroup(input: String) = input.matches(".*doda(j|ć)? grup.*")

  override fun clearGroup(input: String): String {
    val sb = StringBuilder()
    val parts: Array<String> = input.splitByWhitespaces().toTypedArray()
    var st = false
    for (s in parts) {
      if (s.matches(".*grup.*")) {
        st = true
        continue
      }
      if (st) sb.append(s).append(" ")
    }
    return sb.toString().trim()
  }

  override fun getEvent(input: String) = when {
    input.matches(".*urodzin.*") -> Action.BIRTHDAY
    input.matches(".*przypomnien.*") -> Action.REMINDER
    else -> Action.NO_EVENT
  }

  override fun hasToday(input: String) = input.matches(".*dziś.*") ||
    input.matches(".*dzisia.*")

  override fun hasAfterTomorrow(input: String) = input.matches(".*pojutrz.*")

  override val afterTomorrow = "pojutrze"

  override fun hasHours(input: String?) = when {
    input.matchesOrFalse(".*godzin.*") -> 1
    else -> -1
  }

  override fun hasMinutes(input: String?) = when {
    input.matchesOrFalse(".*minut.*") -> 1
    else -> -1
  }

  override fun hasSeconds(input: String?) = input.matchesOrFalse(".*sekund.*")

  override fun hasDays(input: String?) = input.matchesOrFalse(".*dni.*") ||
    input.matchesOrFalse(".*dzień.*") || input.matchesOrFalse(".*dnia.*")

  override fun hasWeeks(input: String?) =
    input.matchesOrFalse(".*tydzień.*") || input.matchesOrFalse(".*tygodn.*")

  override fun hasMonth(input: String?) = input.matchesOrFalse(".*miesią.*")

  override fun hasAnswer(input: String) = input.let { " $it " }.matches(".* (tak|nie) .*")

  override fun getAnswer(input: String) = when {
    input.matches(".* ?tak ?.*") -> Action.YES
    else -> Action.NO
  }

  override fun getDate(input: String, result: (LocalDate?) -> Unit): String {
    var localDate: LocalDate? = null
    return input.splitByWhitespaces().toMutableList().also { list ->
      list.forEachIndexed { index, s ->
        val month = getMonth(s)
        if (month != -1) {
          val dayOfMonth = ignoreAny({
            list[index - 1].toFloat().toInt().also { list[index - 1] = "" }
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

  override fun findFloat(input: String?) = when {
    input == null -> -1f
    input.contains("półtor") -> 1.5f
    input.contains("pół") -> 0.5f
    else -> -1f
  }

  override fun clearFloats(input: String?): String? {
    if (input == null) return null
    return input.replace("i pół", "")
      .splitByWhitespaces()
      .toMutableList()
      .also {
        it.forEachIndexed { index, s ->
          if (s.contains("półtor") || s.matches("pół*.")) {
            it[index] = ""
            return@forEachIndexed
          }
        }
      }.clip().let {
        if (it.contains(" pół")) {
          it.replace("pół", "")
        } else it
      }
  }

  override fun findNumber(input: String?) = when {
    input == null -> -1f
    input.matches("jedenaś.*") || input.matches("jedenast.*") -> 11f
    input.matches("dwanaś.*") || input.matches("dwunast.*") -> 12f
    input.matches("trzynaś.*") || input.matches("trzynast.*") -> 13f
    input.matches("czternaś.*") || input.matches("czternast.*") -> 14f
    input.matches("piętnaś.*") || input.matches("piętnast.*") -> 15f
    input.matches("szesnaś.*") || input.matches("szesnast.*") -> 16f
    input.matches("siedemnaś.*") || input.matches("siedemnast.*") -> 17f
    input.matches("osiemnaś.*") || input.matches("osiemnast.*") -> 18f
    input.matches("dziewiętnaś.*") || input.matches("dziewiętnast.*") -> 19f

    input.matches("dwadzieś.*") || input.matches("dwudziest.*") -> 20f
    input.matches("trzydzieś.*") || input.matches("trzydziest.*") -> 30f
    input.matches("czterdzieś.*") || input.matches("czterdziest.*") -> 40f
    input.matches("pięćdziesiąt.*") || input.matches("pięćdziesięc.*") -> 50f
    input.matches("sześćdziesiąt.*") || input.matches("sześćdziesięc.*") -> 60f
    input.matches("siedemdziesiąt.*") || input.matches("siedemdziesięc.*") -> 70f
    input.matches("osiemdziesiąt.*") || input.matches("osiemdziesięc.*") -> 80f
    input.matches("dziewięćdziesiąt.*") || input.matches("dziewięćdziesięc.*") -> 90f

    input.matches("zero") -> 0f
    input.matches("jeden") || input.matches("jedn.*") || input.matches("pierwsz(y|a|ego)") -> 1f
    input.matches("dwaj?") || input.matches("dwie") || input.matches("dwóch") ||
      input.matches("drug(i|a|iego)") -> 2f
    input.matches("trzy") || input.matches("trzej") || input.matches("trzech") ||
      input.matches("trzecia?(ego)?") -> 3f
    input.matches("cztery") || input.matches("czterech") || input.matches("czwart(y|a|ego)") -> 4f
    input.matches("pięć") || input.matches("piąt(y|a|ego)") -> 5f
    input.matches("sześć") || input.matches("szóst(y|a|ego)") -> 6f
    input.matches("siedem") || input.matches("siódm(y|a|ego)") || input.matches("siódmej") -> 7f
    input.matches("osiem") || input.matches("ośmiu") || input.matches("ósm(y|a|ego)") -> 8f
    input.matches("dziewięć") || input.matches("dziewię.*") || input.matches("dziewiąt.*") -> 9f
    input.matches("dziesięć") || input.matches("dziesię.*") || input.matches("dziesiąt.*") -> 10f
    else -> -1f
  }

  override fun hasShowAction(input: String) = input.matches(".*poka(zać|ż)?.*")

  override fun getShowAction(input: String) = when {
    input.matches(".*urodzin.*") -> Action.BIRTHDAYS
    input.matches(".*aktywne przypomnien.*") -> Action.ACTIVE_REMINDERS
    input.matches(".*przypomnien.*") -> Action.REMINDERS
    input.matches(".*wydarzeni.*") -> Action.EVENTS
    input.matches(".*notatk.*") -> Action.NOTES
    input.matches(".*grup.*") -> Action.GROUPS
    input.matches(".*list(y|ę) zakupów.*") -> Action.SHOP_LISTS
    else -> null
  }

  override fun hasNextModifier(input: String) = input.matches(".*następn.*")

  override fun getTime(input: String, ampm: Ampm?, times: List<String>): LocalTime? {
    Logger.log("getTime: $ampm, input $input")
    val parts = input.splitByWhitespaces().toTypedArray()

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

    var h = -1f
    var m = -1f
    var reserveHour = 0f
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
        if (!hourSuccess) {
          ignoreAny {
            integer = parts[i + hoursIndex].toFloat()
            hourSuccess = true
            parts[i + hoursIndex] = ""
          }
        }
        if (ampm == Ampm.EVENING) {
          integer += 12f
        }
        h = integer
        if (hourSuccess) {
          ignoreAny {
            m = parts[i + 1].toInt().toFloat()
            parts[i + 1] = ""
          }
        }
      }
      if (minutesIndex != -1) {
        m = ignoreAny({
          parts[i - minutesIndex].toFloat()
        }) { 0f }
      }
      ignoreAny { reserveHour = parts[i].toFloat() }
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
    if (reserveHour != 0f) {
      localTime = LocalTime.now(zoneId)
        .withHour(reserveHour.toInt())
        .withMinute(0)
        .withSecond(0)

      if (ampm == Ampm.EVENING) {
        localTime = localTime?.withHour(12)
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

  override fun clearTime(input: String?) =
    input?.let { s ->
      val matcher = Pattern.compile("([01]?[0-9]|2[0-3])( |:)[0-5][0-9]").matcher(s)
      if (matcher.find()) {
        val time = matcher.group().trim()
        s.replace(time, "")
      } else s
    }?.splitByWhitespaces()?.toMutableList()?.also {
      it.forEachIndexed { i, s ->
        if (hasHours(s) != -1) {
          val index = hasHours(s)
          it[i] = ""
          var hourSuccess = false
          ignoreAny {
            it[i - index].toFloat()
            hourSuccess = true
            it[i - index] = ""
          }
          if (!hourSuccess) {
            ignoreAny {
              it[i + index].toFloat()
              hourSuccess = true
              it[i + index] = ""
            }
          }
          if (hourSuccess) {
            ignoreAny {
              it[i + 1].toFloat()
              it[i + 1] = ""
            }
          } else {
            if (i > 0 && it[i - 1].matches("o")) {
              it[i - 1] = ""
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
    }?.clip()?.splitByWhitespaces()?.toMutableList()?.let { list ->
      val sb = StringBuilder()
      list.forEach { s ->
        if (!s.matches("we?") && !s.matches("o")) sb.append(" ").append(s.trim())
      }
      sb.toString().trim()
    } ?: ""
}
