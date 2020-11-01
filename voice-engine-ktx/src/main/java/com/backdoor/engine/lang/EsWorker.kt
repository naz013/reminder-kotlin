package com.backdoor.engine.lang

import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import java.util.*
import java.util.regex.Pattern

internal class EsWorker : Worker() {

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

  override fun clearCalendar(input: String): String? {
    val parts: Array<String?> = input.split(WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val string = parts[i]
      if (string!!.matches(".*calendario.*")) {
        parts[i] = ""
        if (i > 0 && parts[i - 1]!!.toLowerCase().equals("al", ignoreCase = true)) {
          parts[i - 1] = ""
        }
        break
      }
    }
    return clipStrings(parts)
  }

  override fun getWeekDays(input: String): List<Int> {
    val array = intArrayOf(0, 0, 0, 0, 0, 0, 0)
    val parts: Array<String> = input.split(WHITESPACES).toTypedArray()
    val weekDays = weekdays
    for (part in parts) {
      for (i in weekDays.indices) {
        val day = weekDays[i]
        if (part.matches(".*$day.*")) {
          array[i] = 1
          break
        }
      }
    }
    val list: MutableList<Int> = ArrayList()
    for (anArray in array) list.add(anArray)
    return list
  }

  override fun clearWeekDays(input: String): String {
    var parts: Array<String?> = input.split(WHITESPACES).toTypedArray()
    val weekDays = weekdays
    for (i in parts.indices) {
      val part = parts[i]
      for (day in weekDays) {
        if (part!!.matches(".*$day.*")) {
          parts[i] = ""
          break
        }
      }
    }
    parts = clipStrings(parts).split(WHITESPACES).toTypedArray()
    val sb = StringBuilder()
    for (i in parts.indices) {
      val part = parts[i]!!.trim { it <= ' ' }
      if (!part.matches("los")) sb.append(" ").append(part)
    }
    return sb.toString().trim { it <= ' ' }
  }

  override fun getDaysRepeat(input: String): Long {
    val parts: Array<String> = input.split(WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      if (hasDays(part)) {
        val integer: Int
        integer = try {
          parts[i - 1].toInt()
        } catch (e: NumberFormatException) {
          1
        } catch (e: ArrayIndexOutOfBoundsException) {
          0
        }
        return integer * DAY
      }
    }
    return 0
  }

  override fun clearDaysRepeat(input: String): String? {
    val parts: Array<String?> = input.split(WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      if (hasDays(part)) {
        try {
          parts[i - 1]!!.toInt()
          parts[i - 1] = ""
        } catch (ignored: NumberFormatException) {
        }
        parts[i] = ""
        break
      }
    }
    return clipStrings(parts)
  }

  override fun hasRepeat(input: String?): Boolean {
    return input!!.matches(".*cada.*") || input.matches(".*todos.*") || hasEveryDay(input)
  }

  override fun hasEveryDay(input: String?): Boolean {
    return input!!.matches(".*diario.*")
  }

  override fun clearRepeat(input: String): String? {
    val parts: Array<String?> = input.split(WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      if (hasRepeat(part)) {
        parts[i] = ""
        break
      }
    }
    return clipStrings(parts)
  }

  override fun hasTomorrow(input: String): Boolean {
    return input.matches(".*mañana.*") || input.matches(".*(día )?siguiente.*")
  }

  override fun clearTomorrow(input: String): String? {
    val parts: Array<String?> = input.split(WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      if (part!!.matches(".*mañana.*") || part.matches(".*(día )?siguiente.*")) {
        parts[i] = ""
        break
      }
    }
    return clipStrings(parts)
  }

  override fun getMessage(input: String): String {
    val parts: Array<String> = input.split(WHITESPACES).toTypedArray()
    val sb = StringBuilder()
    var isStart = false
    for (part in parts) {
      if (isStart) sb.append(" ").append(part)
      if (part.matches("texto")) isStart = true
    }
    return sb.toString().trim { it <= ' ' }
  }

  override fun clearMessage(input: String): String? {
    var input = input
    val parts: Array<String?> = input.split(WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      if (part!!.matches("texto")) {
        try {
          if (parts[i - 1]!!.matches("con( el)?")) {
            parts[i - 1] = ""
          }
        } catch (ignored: IndexOutOfBoundsException) {
        }
        input = input.replace(part, "")
        parts[i] = ""
      }
    }
    return clipStrings(parts)
  }

  override fun getMessageType(input: String?): Action? {
    if (input!!.matches(".*(un )?mensaje.*")) return Action.MESSAGE else if (input.matches(".*carta.*")) return Action.MAIL
    return null
  }

  override fun clearMessageType(input: String): String? {
    val parts: Array<String?> = input.split(WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      val type = getMessageType(part)
      if (type != null) {
        parts[i] = ""
        val nextIndex = i + 1
        if (nextIndex < parts.size && parts[nextIndex]!!.matches("al")) {
          parts[nextIndex] = ""
        }
        break
      }
    }
    return clipStrings(parts)
  }

  override fun getAmpm(input: String?): Ampm? {
    if (input!!.matches(".*mañana.*") || input.matches(".*madrugada.*")
      || input.matches(".*matutino.*") || input.matches(".*mañanero.*")) return Ampm.MORNING else if (input.matches(".*vespertino.*")) return Ampm.EVENING else if (input.matches(".*(de )?mediodía.*")) return Ampm.NOON else if (input.matches(".*(de )?noche.*")) return Ampm.NIGHT
    return null
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
      if (!part.matches("(a )?las")) sb.append(" ").append(part)
    }
    return sb.toString().trim { it <= ' ' }
  }

  override fun getMonth(input: String?): Int {
    var res = -1
    if (input!!.contains("enero")) res = 0 else if (input.contains("febrero")) res = 1 else if (input.contains("marzo") || input.contains("marcha")) res = 2 else if (input.contains("abril")) res = 3 else if (input.contains("mayo")) res = 4 else if (input.contains("junio")) res = 5 else if (input.contains("julio")) res = 6 else if (input.contains("agosto")) res = 7 else if (input.contains("septiembre") || input.contains("setiembre")) res = 8 else if (input.contains("octubre")) res = 9 else if (input.contains("noviembre")) res = 10 else if (input.contains("diciembre")) res = 11
    return res
  }

  override fun hasCall(input: String?): Boolean {
    return input!!.matches(".*llamada.*")
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
    return input.matches(".*después.*") || input.matches(".* en .*")
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
    return clipStrings(parts)
  }

  override fun hasSender(input: String?): Boolean {
    return input!!.matches(".*enviar.*")
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
    return input.contains("nota")
  }

  override fun clearNote(input: String): String {
    var input = input
    input = input.replace("nota", "")
    return input.trim { it <= ' ' }
  }

  override fun hasAction(input: String): Boolean {
    return (input.startsWith("abierta") || input.startsWith("abierto")
      || input.matches(".*ayuda.*")
      || input.matches(".*ajustar.*") || input.matches(".*modificar.*")
      || input.matches(".*informe.*") ||
      input.matches(".*cambio.*"))
  }

  override fun getAction(input: String): Action? {
    return if (input.matches(".*ayuda.*")) {
      Action.HELP
    } else if (input.matches(".*lo chillón.*") || input.matches(".*volumen.*")) {
      Action.VOLUME
    } else if (input.matches(".*ajustes.*")) {
      Action.SETTINGS
    } else if (input.matches(".*informe.*")) {
      Action.REPORT
    } else {
      Action.APP
    }
  }

  override fun hasEvent(input: String): Boolean {
    return (input.startsWith("nueva") || input.startsWith("nuevo") || input.startsWith("añadir")
      || input.startsWith("crear"))
  }

  override fun getEvent(input: String): Action? {
    return if (input.matches(".*(el )?cumpleaños.*")) {
      Action.BIRTHDAY
    } else if (input.matches(".*(el )?recordatorio.*")) {
      Action.REMINDER
    } else Action.NO_EVENT
  }

  override fun hasEmptyTrash(input: String): Boolean {
    return input.matches(".*papelera vacía.*")
  }

  override fun hasDisableReminders(input: String): Boolean {
    return input.matches(".*deshabilitar el recordatorio.*")
  }

  override fun hasGroup(input: String): Boolean {
    return input.matches(".*añadir grupo.*")
  }

  override fun clearGroup(input: String): String {
    val sb = StringBuilder()
    val parts: Array<String> = input.split(WHITESPACES).toTypedArray()
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
    return sb.toString().trim { it <= ' ' }
  }

  override fun hasToday(input: String): Boolean {
    return input.matches(".*(los )?hoy.*")
  }

  override fun hasAfterTomorrow(input: String): Boolean {
    return input.matches(".*pasado mañana.*") || input.matches(".*después de mañana.*")
  }

  override fun clearAfterTomorrow(input: String): String {
    return if (input.matches(".*pasado mañana.*")) {
      input.replace("pasado mañana", "")
    } else if (input.matches(".*después de mañana.*")) {
      input.replace("después de mañana", "")
    } else {
      input
    }
  }

  protected override val afterTomorrow: String
    protected get() = "pasado mañana"

  override fun hasHours(input: String?): Int {
    return if (input!!.matches(".*(la )?hora.*") || input.matches("en punto.*")) 1 else -1
  }

  override fun hasMinutes(input: String?): Int {
    return if (input!!.matches(".*(el )?minutos?.*")) 1 else -1
  }

  override fun hasSeconds(input: String?): Boolean {
    return input!!.matches(".*(el |la )?segundos?.*")
  }

  override fun hasDays(input: String?): Boolean {
    return input!!.matches(".*(el )?día.*") || input.matches(".*(el )?dia.*")
  }

  override fun hasWeeks(input: String?): Boolean {
    return input!!.matches(".*(la )?semanas?.*")
  }

  override fun hasMonth(input: String?): Boolean {
    return input!!.matches(".*(el )?mes(es)?.*")
  }

  override fun hasAnswer(input: String): Boolean {
    var input = input
    input = " $input "
    return input.matches(".* (sí|si|no) .*")
  }

  override fun getAnswer(input: String): Action? {
    return if (input.matches(".* ?(no) ?.*")) {
      Action.NO
    } else Action.YES
  }

  override fun findFloat(input: String?): Float {
    return if (input!!.matches("(la )?mitad") || input.matches("(el )?medio?a?")) {
      0.5f
    } else {
      (-1).toFloat()
    }
  }

  override fun clearFloats(input: String?): String? {
    if (input!!.contains("y media")) {
      return input.replace("y media", "")
    }
    if (input.contains("y medio")) {
      return input.replace("y medio", "")
    }
    if (input.contains("media")) {
      return input.replace("media", "")
    }
    return if (input.contains("medio")) {
      input.replace("medio", "")
    } else input
  }

  override fun findNumber(input: String?): Float {
    var number = -1f
    if (input!!.matches("cero") || input.matches("nulo")) number = 0f else if (input.matches("un([ao])?") || input.matches("primer([ao])?")) number = 1f else if (input.matches("dos") || input.matches("segund([ao])?")) number = 2f else if (input.matches("(los )?tres") || input.matches("tercer([ao])?")) number = 3f else if (input.matches("cuatro") || input.matches("cuart([ao])?")) number = 4f else if (input.matches("(los )?cinco") || input.matches("quint([ao])?")) number = 5f else if (input.matches("(los )?seis") || input.matches("sext([ao])?")) number = 6f else if (input.matches("(los )?siete") || input.matches("séptim([ao])?")) number = 7f else if (input.matches("(los )?ocho") || input.matches("octav([ao])?")) number = 8f else if (input.matches("(el )?nueve") || input.matches("noven([ao])?")) number = 9f else if (input.matches("(los )?diez") || input.matches("décim([ao])?")) number = 10f else if (input.matches("(el )?once") || input.matches("undécim([ao])?")) number = 11f else if (input.matches("(los )?doce") || input.matches("duodécim([ao])?")) number = 12f else if (input.matches("(los )?trece") || input.matches("decimotercer([ao])?")) number = 13f else if (input.matches("(los )?catorce") || input.matches("decimocuart([ao])?")) number = 14f else if (input.matches("(los )?quince") || input.matches("decimoquint([ao])?")) number = 15f else if (input.matches("(el )?dieciséis") || input.matches("decimosext([ao])?")) number = 16f else if (input.matches("(los )?diecisiete") || input.matches("decimoséptim([ao])?")) number = 17f else if (input.matches("(los )?dieciocho") || input.matches("decimoctav([ao])?")) number = 18f else if (input.matches("(el )?diecinueve") || input.matches("decimonoven([ao])?")) number = 19f else if (input.matches("(los )?veinte") || input.matches("vigésim([ao])?")) number = 20f else if (input.matches("(los )?treinta") || input.matches("trigésim([ao])?")) number = 30f else if (input.matches("(los )?cuarenta") || input.matches("cuadragésim([ao])?")) number = 40f else if (input.matches("(los )?cincuenta") || input.matches("quincuagésim([ao])?")) number = 50f else if (input.matches("(las )?sesenta") || input.matches("sexagésim([ao])?")) number = 60f else if (input.matches("(los )?setenta") || input.matches("septuagésim([ao])?")) number = 70f else if (input.matches("(los )?ochenta") || input.matches("diecioch([ao])?")) number = 80f else if (input.matches("(la )?noventa") || input.matches("nonagésim([ao])?")) number = 90f
    return number
  }

  override fun hasShowAction(input: String): Boolean {
    return input.matches(".*mostrar.*")
  }

  override fun getShowAction(input: String): Action? {
    if (input.matches(".*cumpleaños.*")) {
      return Action.BIRTHDAYS
    } else if (input.matches(".*recordatorios activos.*")) {
      return Action.ACTIVE_REMINDERS
    } else if (input.matches(".*(el )?recordatorios.*")) {
      return Action.REMINDERS
    } else if (input.matches(".*eventos.*")) {
      return Action.EVENTS
    } else if (input.matches(".*(las )?notas.*")) {
      return Action.NOTES
    } else if (input.matches(".*grupos.*")) {
      return Action.GROUPS
    } else if (input.matches(".*lista de (la )?compra.*") || input.matches(".*listas de compras.*")) {
      return Action.SHOP_LISTS
    }
    return null
  }

  override fun hasNextModifier(input: String): Boolean {
    return input.matches(".*siguiente.*") || input.matches(".*próximo.*")
  }
}