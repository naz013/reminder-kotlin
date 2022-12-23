package com.backdoor.engine.lang

import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import java.util.regex.Pattern

internal class EsWorker(zoneId: ZoneId) : Worker(zoneId) {

  override val weekdays = listOf(
    "domin",
    "lunes",
    "martes",
    "miércoles",
    "jueves",
    "viernes",
    "sábado"
  )

  override fun hasCalendar(input: String) = input.matches(".*calendario.*")

  override fun clearCalendar(input: String) =
    input.splitByWhitespaces()
      .toMutableList()
      .let {
        it.forEachIndexed { index, s ->
          if (s.matches(".*calendario.*")) {
            it[index] = ""
            clearAllBackward(it, index - 1, 3, "(y|agréguela|al|añádelo)")
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
            clearAllBackward(it, index - 1, 1, "(los|y)")
            break
          }
        }
      }
    }.clip().splitByWhitespaces().forEach { s ->
      val part = s.trim()
      if (!part.matches("los")) sb.append(" ").append(part)
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
          clearAllBackward(it, index - 1, 1, "los")
          return@forEachIndexed
        }
      }
    }.clip()

  override fun hasRepeat(input: String) =
    input.matches(".*cada.*") || input.matches(".*todos.*") || hasEveryDay(input)

  override fun hasEveryDay(input: String) = input.matches(".*diario.*")

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
    (input.matches(".*mañana.*") && !hasDate(input)) || input.matches(".*(día )?siguiente.*")

  override fun clearTomorrow(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches(".*mañana.*") || s.matches(".*(día )?siguiente.*")) {
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
      if (it.matches("texto")) isStart = true
    }.let {
      sb.toString().trim()
    }
  }

  override fun clearMessage(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches("texto")) {
          ignoreAny {
            if (it[index - 1].matches("con( el)?")) it[index - 1] = ""
          }
          it[index] = ""
        }
      }
    }.clip()

  override fun getMessageType(input: String) = when {
    input.matches(".*(un )?mensaje.*") -> Action.MESSAGE
    input.matches(".*carta.*") || input.matches(".*(correo )?electrónico?.*") -> Action.MAIL
    else -> null
  }

  override fun clearMessageType(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (getMessageType(s) != null) {
          it[index] = ""
          clearAllForward(it, index + 1, 1, "al")
          if (s.matches(".*electrónico?.*")) {
            clearAllBackward(it, index - 1, 1, "correo?")
          }
          return@forEachIndexed
        }
      }
    }.clip()

  override fun getAmpm(input: String) = when {
    input.matches(".*mañana.*") || input.matches(".*madrugada.*") ||
      input.matches(".*matutino.*") || input.matches(".*mañanero.*") -> Ampm.MORNING
    input.matches(".*vespertino.*") || input.matches(".*(la )?tarde.*") -> Ampm.EVENING
    input.matches(".*(de )?mediodía.*") -> Ampm.NOON
    input.matches(".*(de )?noche.*") -> Ampm.NIGHT
    else -> null
  }

  private fun hasDate(input: String): Boolean {
    var has = false
    getDate(input) { has = it != null }
    return has
  }

  override fun clearAmpm(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (getAmpm(s) != null) {
          it[index] = ""
          clearAllBackward(it, index - 1, 2, "(por|la|de|al)")
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
    input.matches("enero") -> 1
    input.matches("febrero") -> 2
    input.matches("marzo") || input.matches("marcha") -> 3
    input.matches("abril") -> 4
    input.matches("mayo") -> 5
    input.matches("junio") -> 6
    input.matches("julio") -> 7
    input.matches("agosto") -> 8
    input.matches("septiembre") || input.matches("setiembre") -> 9
    input.matches("octubre") -> 10
    input.matches("noviembre") -> 11
    input.matches("diciembre") -> 12
    else -> -1
  }

  override fun hasCall(input: String) = input.matches(".*llama(da|r).*")

  override fun clearCall(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasCall(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()

  override fun hasTimer(input: String) = input.let { " $it " }.matches(".*después.*")

  override fun cleanTimer(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasTimer(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip().trim()

  override fun getDate(input: String, result: (LocalDate?) -> Unit): String {
    var localDate: LocalDate? = null
    return input.splitByWhitespaces().toMutableList().also { list ->
      list.forEachIndexed { index, s ->
        val month = getMonth(s)
        if (month != -1) {
          var dayOfMonth = -1
          ignoreAny {
            dayOfMonth = list[index - 1].toFloat().toInt()
            if (dayOfMonth != -1) {
              list[index - 1] = ""
            }
          }
          if (dayOfMonth == -1) {
            ignoreAny {
              dayOfMonth = list[index - 2].toFloat().toInt()
              if (dayOfMonth != -1) {
                list[index - 2] = ""
                clearAllBackward(list, index - 3, 1, "el")
              }
            }
          }

          if (dayOfMonth != -1) {
            val parsedDate = LocalDate.now(zoneId)
              .withDayOfMonth(dayOfMonth)
              .withMonth(month)

            localDate = parsedDate
            list[index] = ""
            clearAllBackward(list, index - 1, 1, "de")
            return@forEachIndexed
          }
        }
      }
    }.clip().also {
      result(localDate)
    }
  }

  override fun hasSender(input: String): Boolean {
    return input.matches(".*env(í|i)ar?.*")
  }

  override fun clearSender(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasSender(s)) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()

  override fun hasNote(input: String) = input.contains("nota")

  override fun clearNote(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches("nota")) {
          it[index] = ""
          clearAllBackward(it, index - 1, 2, "nuev(a|o)", "un(a|o)")
        } else if (s.matches("crear")) {
          it[index] = ""
          clearAllBackward(it, index - 1, 1, "de")
        }
      }
    }.clip()

  override fun hasAction(input: String): Boolean {
    return input.matches(".*abiert(o|a).*") || input.matches(".*ayuda.*")
      || input.matches(".*ajustar.*") || input.matches(".*modificar.*")
      || input.matches(".*informe.*") || input.matches(".*cambi(o|ar).*")
  }

  override fun getAction(input: String) = when {
    input.matches(".*ayuda.*") -> Action.HELP
    input.matches(".*lo chillón.*") || input.matches(".*volumen.*") -> Action.VOLUME
    input.matches(".*ajustes.*") || input.matches(".*configuración.*") -> Action.SETTINGS
    input.matches(".*informe.*") -> Action.REPORT
    else -> Action.APP
  }

  override fun hasEvent(input: String) = input.startsWith("nueva") ||
    input.startsWith("nuevo") || input.startsWith("añadir") ||
    input.startsWith("crear")

  override fun getEvent(input: String) = when {
    input.matches(".*(el )?cumpleaños.*") -> Action.BIRTHDAY
    input.matches(".*(el )?recordatorio.*") -> Action.REMINDER
    else -> Action.NO_EVENT
  }

  override fun hasEmptyTrash(input: String) = input.matches(".*(papelera|limpiar) (la)? ?(vacía|basura).*")

  override fun hasDisableReminders(input: String) =
    input.matches(".*(deshabilitar|desactivar) (el|todos los)? ?recordatorios?.*")

  override fun hasGroup(input: String) = input.matches(".*(añadir|crear|nuevo)(.*)grupo.*")

  override fun clearGroup(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches(".*grupo.*")) {
          it[index] = ""
          clearAllBackward(it, index - 1, 1, "(el|en)")
          clearAllForward(it, 0, 2, "(añadir|crear|nuevo)")
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun hasToday(input: String) = input.matches(".*(los )?hoy.*")

  override fun hasAfterTomorrow(input: String) =
    input.matches(".*pasado mañana.*") || input.matches(".*después de mañana.*")

  override fun clearAfterTomorrow(input: String) = when {
    input.matches(".*pasado mañana.*") -> input.replace("pasado mañana", "")
    input.matches(".*después de mañana.*") -> input.replace("después de mañana", "")
    else -> input
  }

  override val afterTomorrow = "pasado mañana"

  override fun hasHours(input: String?) = when {
    input.matchesOrFalse(".*(la )?hora.*") || input.matchesOrFalse("(en )?punto.*") -> 1
    else -> -1
  }

  override fun hasMinutes(input: String?) = when {
    input.matchesOrFalse(".*(el )?minutos?.*") -> 1
    else -> -1
  }

  override fun hasSeconds(input: String?) = input.matchesOrFalse(".*(el |la )?segundos?.*")

  override fun hasDays(input: String?) =
    input.matchesOrFalse(".*(el )?día.*") || input.matchesOrFalse(".*(el )?dia.*")

  override fun hasWeeks(input: String?) = input.matchesOrFalse(".*(la )?semanas?.*")

  override fun hasMonth(input: String?) = input.matchesOrFalse(".*(el )?mes(es)?.*")

  override fun hasAnswer(input: String) = input.let { " $it " }.matches(".* (sí|si|no),? .*")

  override fun getAnswer(input: String) = when {
    input.matches(".* ?(no) ?.*") -> Action.NO
    else -> Action.YES
  }

  override fun findFloat(input: String?) = when {
    "$input ".matchesOrFalse("(la )?mitad") || "$input ".matchesOrFalse("(el )?medi(o|a) ") -> 0.5f
    else -> -1f
  }

  override fun clearFloats(input: String?) = when {
    input == null -> null
    "$input ".matches("y media ") -> input.replace("y media", "")
    "$input ".matches("y medio ") -> input.replace("y medio", "")
    "$input ".matches("media ") -> input.replace("media", "")
    "$input ".matches("medio ") -> input.replace("medio", "")
    else -> input
  }

  override fun findNumber(input: String?) = when {
    input == null -> -1f
    input.matches("cero") || input.matches("nulo") -> 0f
    input.matches("un(a|o)") || input.matches("primer(a|o)") -> 1f
    input.matches("dos") || input.matches("segund(a|o)") -> 2f
    input.matches("(los )?tres") || input.matches("tercer(a|o)") -> 3f
    input.matches("cuatro") || input.matches("cuart(a|o)") -> 4f
    input.matches("(los )?cinco") || input.matches("quint(a|o)") -> 5f
    input.matches("(los )?seis") || input.matches("sext(a|o)") -> 6f
    input.matches("(los )?siete") || input.matches("séptim(a|o)") -> 7f
    input.matches("(los )?ocho") || input.matches("octav(a|o)") -> 8f
    input.matches("(el )?nueve") || input.matches("noven(a|o)") -> 9f
    input.matches("(los )?diez") || input.matches("décim(a|o)") -> 10f
    input.matches("(el )?once") || input.matches("undécim(a|o)") -> 11f
    input.matches("(los )?doce") || input.matches("duodécim(a|o)") -> 12f
    input.matches("(los )?trece") || input.matches("decimotercer(a|o)") -> 13f
    input.matches("(los )?catorce") || input.matches("decimocuart(a|o)") -> 14f
    input.matches("(los )?quince") || input.matches("decimoquint(a|o)") -> 15f
    input.matches("(el )?dieciséis") || input.matches("decimosext(a|o)") -> 16f
    input.matches("(los )?diecisiete") || input.matches("decimoséptim(a|o)") -> 17f
    input.matches("(los )?dieciocho") || input.matches("decimoctav(a|o)") -> 18f
    input.matches("(el )?diecinueve") || input.matches("decimonoven(a|o)") -> 19f
    input.matches("(los )?veinte") || input.matches("vigésim(a|o)") -> 20f
    input.matches("(los )?treinta") || input.matches("trigésim(a|o)") -> 30f
    input.matches("(los )?cuarenta") || input.matches("cuadragésim(a|o)") -> 40f
    input.matches("(los )?cincuenta") || input.matches("quincuagésim(a|o)") -> 50f
    input.matches("(las )?sesenta") || input.matches("sexagésim(a|o)") -> 60f
    input.matches("(los )?setenta") || input.matches("septuagésim(a|o)") -> 70f
    input.matches("(los )?ochenta") || input.matches("diecioch(a|o)") -> 80f
    input.matches("(la )?noventa") || input.matches("nonagésim(a|o)") -> 90f
    else -> -1f
  }

  override fun hasShowAction(input: String) = input.matches(".*mostrar.*")

  override fun getShowAction(input: String) = when {
    input.matches(".*cumpleaños.*") -> Action.BIRTHDAYS
    input.matches(".*recordatorios activos.*") -> Action.ACTIVE_REMINDERS
    input.matches(".*(el )?recordatorios.*") -> Action.REMINDERS
    input.matches(".*eventos.*") -> Action.EVENTS
    input.matches(".*(las )?notas.*") -> Action.NOTES
    input.matches(".*grupos.*") -> Action.GROUPS
    input.matches(".*lista de (la )?compra.*") || input.matches(".*listas de compras.*") -> Action.SHOP_LISTS
    else -> null
  }

  override fun hasNextModifier(input: String) =
    input.matches(".*siguiente.*") || input.matches(".*próximo.*")

  override fun clearArticleForMulti(it: MutableList<String>, index: Int) {
    clearAllBackward(it, index - 1, 1, "de")
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

  override fun clearTime(input: String?): String {
    val pattern = Pattern.compile("([01]?[0-9]|2[0-3])( |:)[0-5][0-9]")
    return input?.splitByWhitespaces()?.toMutableList()?.also {
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
          if (hourSuccess) {
            ignoreAny {
              it[i + 1].toFloat()
              it[i + 1] = ""
            }
          }
          clearAllBackward(it, i - 1, 2, "en")
        }
        if (hasMinutes(s) != -1) {
          val index = hasMinutes(s)
          ignoreAny {
            it[i - index].toInt()
            it[i - index] = ""
          }
          it[i] = ""
        }
        ignoreAny {
          val f = s.toFloat()
          if (f >= 0f && f < 24f) {
            it[i] = ""
            clearAllBackward(it, i - 1, 2, "(a|las)")
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
            clearAllBackward(it, i - 1, 2, "(a|las)")
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

  override fun hasConnectSpecialWord(input: String): Boolean {
    return input.matches("a")
  }
}
