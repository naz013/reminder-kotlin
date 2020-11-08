package com.backdoor.engine.lang

import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import com.backdoor.engine.misc.LongInternal
import java.util.*
import java.util.regex.Pattern

//TODO("Change translation to polish")
internal class PlWorker : Worker() {

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
            if (index > 0 && it[index - 1].equals("al", ignoreCase = true)) {
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
    input.matches(".*mañana.*") || input.matches(".*(día )?siguiente.*")

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
    input.matches(".*carta.*") -> Action.MAIL
    else -> null
  }

  override fun clearMessageType(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (getMessageType(s) != null) {
          it[index] = ""
          val nextIndex = index + 1
          if (nextIndex < it.size && it[nextIndex].matches("al")) {
            it[nextIndex] = ""
          }
          return@forEachIndexed
        }
      }
    }.clip()

  override fun getAmpm(input: String) = when {
    input.matches(".*mañana.*") || input.matches(".*madrugada.*") ||
      input.matches(".*matutino.*") || input.matches(".*mañanero.*") -> Ampm.MORNING
    input.matches(".*vespertino.*") -> Ampm.EVENING
    input.matches(".*(de )?mediodía.*") -> Ampm.NOON
    input.matches(".*(de )?noche.*") -> Ampm.NIGHT
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
        if (!s.matches("(a )?las")) sb.append(" ").append(s.trim())
      }
      sb.toString().trim()
    } ?: ""

  override fun getMonth(input: String?) = when {
    input == null -> -1
    input.contains("enero") -> 0
    input.contains("febrero") -> 1
    input.contains("marzo") || input.contains("marcha") -> 2
    input.contains("abril") -> 3
    input.contains("mayo") -> 4
    input.contains("junio") -> 5
    input.contains("julio") -> 6
    input.contains("agosto") -> 7
    input.contains("septiembre") || input.contains("setiembre") -> 8
    input.contains("octubre") -> 9
    input.contains("noviembre") -> 10
    input.contains("diciembre") -> 11
    else -> -1
  }

  override fun hasCall(input: String) = input.matches(".*llamada.*")

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
    it.matches(".*después.*") || it.matches(".* en .*")
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

  override fun hasSender(input: String) = input.matches(".*enviar.*")

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

  override fun clearNote(input: String) = input.replace("nota", "").trim()

  override fun hasAction(input: String): Boolean {
    return (input.startsWith("abierta") || input.startsWith("abierto")
      || input.matches(".*ayuda.*")
      || input.matches(".*ajustar.*") || input.matches(".*modificar.*")
      || input.matches(".*informe.*") ||
      input.matches(".*cambio.*"))
  }

  override fun getAction(input: String) = when {
    input.matches(".*ayuda.*") -> Action.HELP
    input.matches(".*lo chillón.*") || input.matches(".*volumen.*") -> Action.VOLUME
    input.matches(".*ajustes.*") -> Action.SETTINGS
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

  override fun hasEmptyTrash(input: String) = input.matches(".*papelera vacía.*")

  override fun hasDisableReminders(input: String) =
    input.matches(".*deshabilitar el recordatorio.*")

  override fun hasGroup(input: String) = input.matches(".*añadir grupo.*")

  override fun clearGroup(input: String): String {
    val sb = StringBuilder()
    val parts: Array<String> = input.splitByWhitespaces().toTypedArray()
    var st = false
    for (s in parts) {
      if (s.matches(".*(el )?grupo.*")) {
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
    input.matchesOrFalse(".*(la )?hora.*") || input.matchesOrFalse("en punto.*") -> 1
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

  override fun hasAnswer(input: String) = input.let { " $it " }.matches(".* (sí|si|no) .*")

  override fun getAnswer(input: String) = when {
    input.matches(".* ?(no) ?.*") -> Action.NO
    else -> Action.YES
  }

  override fun findFloat(input: String?) = when {
    input.matchesOrFalse("(la )?mitad") || input.matchesOrFalse("(el )?medio?a?") -> 0.5f
    else -> -1f
  }

  override fun clearFloats(input: String?) = when {
    input == null -> null
    input.contains("y media") -> input.replace("y media", "")
    input.contains("y medio") -> input.replace("y medio", "")
    input.contains("media") -> input.replace("media", "")
    input.contains("medio") -> input.replace("medio", "")
    else -> input
  }

  override fun findNumber(input: String?) = when {
    input == null -> -1f
    input.matches("cero") || input.matches("nulo") -> 0f
    input.matches("un([ao])?") || input.matches("primer([ao])?") -> 1f
    input.matches("dos") || input.matches("segund([ao])?") -> 2f
    input.matches("(los )?tres") || input.matches("tercer([ao])?") -> 3f
    input.matches("cuatro") || input.matches("cuart([ao])?") -> 4f
    input.matches("(los )?cinco") || input.matches("quint([ao])?") -> 5f
    input.matches("(los )?seis") || input.matches("sext([ao])?") -> 6f
    input.matches("(los )?siete") || input.matches("séptim([ao])?") -> 7f
    input.matches("(los )?ocho") || input.matches("octav([ao])?") -> 8f
    input.matches("(el )?nueve") || input.matches("noven([ao])?") -> 9f
    input.matches("(los )?diez") || input.matches("décim([ao])?") -> 10f
    input.matches("(el )?once") || input.matches("undécim([ao])?") -> 11f
    input.matches("(los )?doce") || input.matches("duodécim([ao])?") -> 12f
    input.matches("(los )?trece") || input.matches("decimotercer([ao])?") -> 13f
    input.matches("(los )?catorce") || input.matches("decimocuart([ao])?") -> 14f
    input.matches("(los )?quince") || input.matches("decimoquint([ao])?") -> 15f
    input.matches("(el )?dieciséis") || input.matches("decimosext([ao])?") -> 16f
    input.matches("(los )?diecisiete") || input.matches("decimoséptim([ao])?") -> 17f
    input.matches("(los )?dieciocho") || input.matches("decimoctav([ao])?") -> 18f
    input.matches("(el )?diecinueve") || input.matches("decimonoven([ao])?") -> 19f
    input.matches("(los )?veinte") || input.matches("vigésim([ao])?") -> 20f
    input.matches("(los )?treinta") || input.matches("trigésim([ao])?") -> 30f
    input.matches("(los )?cuarenta") || input.matches("cuadragésim([ao])?") -> 40f
    input.matches("(los )?cincuenta") || input.matches("quincuagésim([ao])?") -> 50f
    input.matches("(las )?sesenta") || input.matches("sexagésim([ao])?") -> 60f
    input.matches("(los )?setenta") || input.matches("septuagésim([ao])?") -> 70f
    input.matches("(los )?ochenta") || input.matches("diecioch([ao])?") -> 80f
    input.matches("(la )?noventa") || input.matches("nonagésim([ao])?") -> 90f
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
}