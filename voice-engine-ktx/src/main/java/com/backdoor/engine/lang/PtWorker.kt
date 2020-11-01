package com.backdoor.engine.lang

import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import java.util.*
import java.util.regex.Pattern

internal class PtWorker : Worker() {

  override val weekdays = listOf(
    "doming",
    "segunda-feira",
    "terça",
    "quarta-feira",
    "quinta-feira",
    "sexta feira",
    "sábado"
  )

  override fun hasCalendar(input: String) = input.matches(".*calendário.*")

  override fun clearCalendar(input: String): String? {
    val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val string = parts[i]
      if (string!!.matches(".*calendário.*")) {
        parts[i] = ""
        if (i > 0 && parts[i - 1]!!.matches("([ao])")) {
          parts[i - 1] = ""
        }
        break
      }
    }
    return clipStrings(parts)
  }

  override fun getWeekDays(input: String): List<Int> {
    val array = intArrayOf(0, 0, 0, 0, 0, 0, 0)
    val parts: Array<String> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
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
    var parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
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
    parts = clipStrings(parts).split(Worker.Companion.WHITESPACES).toTypedArray()
    val sb = StringBuilder()
    for (part1 in parts) {
      val part = part1!!.trim { it <= ' ' }
      if (!part.matches("na") && !part.matches("em")) sb.append(" ").append(part)
    }
    return sb.toString().trim { it <= ' ' }
  }

  override fun getDaysRepeat(input: String): Long {
    val parts: Array<String> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
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
        return integer * Worker.Companion.DAY
      }
    }
    return 0
  }

  override fun clearDaysRepeat(input: String): String? {
    val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
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
    return input!!.matches(".*cada.*") || input.matches(".*tod([ao])s( as)?.*") || hasEveryDay(input)
  }

  override fun hasEveryDay(input: String?): Boolean {
    return input!!.matches(".*diário.*")
  }

  override fun clearRepeat(input: String): String? {
    val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
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
    return input.matches(".*amanh([ãa]).*") || input.matches(".*próximo dia.*")
  }

  override fun clearTomorrow(input: String): String? {
    val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      if (part!!.matches(".*amanh([ãa]).*") || part.matches(".*próximo dia.*")) {
        parts[i] = ""
        break
      }
    }
    return clipStrings(parts)
  }

  override fun getMessage(input: String): String {
    val parts: Array<String> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
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
    val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      if (part!!.matches("texto")) {
        try {
          if (parts[i - 1]!!.matches("com")) {
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
    if (input!!.matches(".*mensagem.*")) return Action.MESSAGE else if (input.matches(".*carta.*")) return Action.MAIL
    return null
  }

  override fun clearMessageType(input: String): String? {
    val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      val type = getMessageType(part)
      if (type != null) {
        parts[i] = ""
        val nextIndex = i + 1
        if (nextIndex < parts.size && parts[nextIndex]!!.matches("para")) {
          parts[nextIndex] = ""
        }
        break
      }
    }
    return clipStrings(parts)
  }

  override fun getAmpm(input: String?): Ampm? {
    if (input!!.matches(".*(de )?manhã.*")) return Ampm.MORNING else if (input.matches(".*tarde.*")) return Ampm.EVENING else if (input.matches(".*meio-dia.*")) return Ampm.NOON else if (input.matches(".*noite.*")) return Ampm.NIGHT
    return null
  }

  override fun clearAmpm(input: String): String? {
    val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
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
    val pattern = Pattern.compile("([01]?[0-9]|2[0-3])([ :])[0-5][0-9]")
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
    var parts: Array<String?> = input!!.split(Worker.Companion.WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      if (hasHours(part) != -1) {
        val index = hasHours(part)
        parts[i] = ""
        try {
          parts[i - index]!!.toInt()
          parts[i - index] = ""
        } catch (ignored: Exception) {
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
    val pattern = Pattern.compile("([01]?[0-9]|2[0-3])([ :])[0-5][0-9]")
    input = clipStrings(parts)
    val matcher = pattern.matcher(input)
    if (matcher.find()) {
      val time = matcher.group().trim { it <= ' ' }
      input = input.replace(time, "")
    }
    parts = input.split(Worker.Companion.WHITESPACES).toTypedArray()
    val sb = StringBuilder()
    for (part1 in parts) {
      val part = part1!!.trim { it <= ' ' }
      if (!part.matches("at")) sb.append(" ").append(part)
    }
    return sb.toString().trim { it <= ' ' }
  }

  override fun getMonth(input: String?): Int {
    var res = -1
    if (input!!.contains("janeiro")) res = 0 else if (input.contains("fevereiro")) res = 1 else if (input.contains("março") || input.contains("marcha")) res = 2 else if (input.contains("abril")) res = 3 else if (input.contains("maio")) res = 4 else if (input.contains("junho")) res = 5 else if (input.contains("julho")) res = 6 else if (input.contains("agosto")) res = 7 else if (input.contains("setembro")) res = 8 else if (input.contains("outubro")) res = 9 else if (input.contains("novembro")) res = 10 else if (input.contains("dezembro")) res = 11
    return res
  }

  override fun hasCall(input: String?): Boolean {
    return input!!.matches(".*ligue.*")
  }

  override fun clearCall(input: String): String? {
    val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
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
    return input.matches(".*após.*") || input.matches(".* em .*") || input.matches(".*depois( de)?.*")
  }

  override fun cleanTimer(input: String): String? {
    val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val string = parts[i]
      if (isTimer(string)) {
        parts[i] = ""
        break
      }
    }
    return clipStrings(parts)
  }

  override fun getDate(input: String, res: com.backdoor.engine.misc.LongInternal): String? {
    var mills: Long = 0
    val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
    for (i in parts.indices) {
      val part = parts[i]
      val month = getMonth(part)
      if (month != -1) {
        var integer: Int
        try {
          integer = parts[i + 1]!!.toInt()
          parts[i + 1] = ""
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
    return input!!.matches(".*mandar.*") || input.matches(".*enviar.*")
  }

  override fun clearSender(input: String): String? {
    val parts: Array<String?> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
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
    return (input.startsWith("abrir") || input.startsWith("aberto")
      || input.matches(".*ajuda.*")
      || input.matches(".*ajustar.*")
      || input.matches(".*relatório.*")
      || input.matches(".*mudança.*"))
  }

  override fun getAction(input: String): Action? {
    return if (input.matches(".*ajuda.*")) {
      Action.HELP
    } else if (input.matches(".*sonoridade.*") || input.matches(".*volume.*")) {
      Action.VOLUME
    } else if (input.matches(".*definições.*") || input.matches(".*configurações.*")) {
      Action.SETTINGS
    } else if (input.matches(".*relatório.*")) {
      Action.REPORT
    } else {
      Action.APP
    }
  }

  override fun hasEvent(input: String): Boolean {
    return (input.startsWith("nova") || input.startsWith("novo") || input.startsWith("adicionar")
      || input.startsWith("crio") || input.startsWith("criar"))
  }

  override fun getEvent(input: String): Action? {
    return if (input.matches(".*aniversário.*")) {
      Action.BIRTHDAY
    } else if (input.matches(".*(o )?lembrete.*")) {
      Action.REMINDER
    } else Action.NO_EVENT
  }

  override fun hasEmptyTrash(input: String): Boolean {
    return input.matches(".*lixo vazio.*")
  }

  override fun hasDisableReminders(input: String): Boolean {
    return input.matches(".*desativar lembrete.*")
  }

  override fun hasGroup(input: String): Boolean {
    return input.matches(".*adicionar grupo.*")
  }

  override fun clearGroup(input: String): String {
    val sb = StringBuilder()
    val parts: Array<String> = input.split(Worker.Companion.WHITESPACES).toTypedArray()
    var st = false
    for (s in parts) {
      if (s.matches(".*(o )?grupo.*")) {
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
    return input.matches(".*hoje.*")
  }

  override fun hasAfterTomorrow(input: String): Boolean {
    return input.matches(".*depois de amanhã.*")
  }

  protected override val afterTomorrow: String
    protected get() = "depois de amanhã"

  override fun hasHours(input: String?): Int {
    return if (input!!.matches(".*hora.*") || input.matches(".*horas.*")) 1 else -1
  }

  override fun hasMinutes(input: String?): Int {
    return if (input!!.matches(".*minuto.*")) 1 else -1
  }

  override fun hasSeconds(input: String?): Boolean {
    return input!!.matches(".*segund([ao])?.*")
  }

  override fun hasDays(input: String?): Boolean {
    return input!!.matches(".*dia.*")
  }

  override fun hasWeeks(input: String?): Boolean {
    return input!!.matches(".*semana.*")
  }

  override fun hasMonth(input: String?): Boolean {
    return input!!.matches(".*mês.*") || input.matches(".*meses.*")
  }

  override fun hasAnswer(input: String): Boolean {
    var input = input
    input = " $input "
    return input.matches(".* (sim|não) .*")
  }

  override fun getAnswer(input: String): Action? {
    return if (input.matches(".* ?sim ?.*")) {
      Action.YES
    } else Action.NO
  }

  override fun findFloat(input: String?): Float {
    return if (input!!.matches("mei([ao])")) {
      0.5f
    } else {
      (-1).toFloat()
    }
  }

  override fun clearFloats(input: String?): String? {
    if (input!!.contains("e meio")) {
      return input.replace("e meio", "")
    }
    if (input.contains("e meia")) {
      return input.replace("e meia", "")
    }
    if (input.contains("meio")) {
      return input.replace("meio", "")
    }
    return if (input.contains("ao meio")) {
      input.replace("ao meio", "")
    } else input
  }

  override fun findNumber(input: String?): Float {
    var number = -1f
    if (input!!.matches("zero") || input.matches("nulo")) number = 0f
    if (input.matches("uma?") || input.matches("primeir([ao])")) number = 1f
    if (input.matches("dois") || input.matches("duas") || input.matches("segund([ao])")) number = 2f
    if (input.matches("(os )?três") || input.matches("terceir([ao])")) number = 3f
    if (input.matches("quatro") || input.matches("quarto")) number = 4f
    if (input.matches("cinco") || input.matches("quint([ao])")) number = 5f
    if (input.matches("(as )?seis") || input.matches("sext([ao])")) number = 6f
    if (input.matches("sete") || input.matches("sétim([ao])")) number = 7f
    if (input.matches("(os )?oito") || input.matches("oitav([ao])")) number = 8f
    if (input.matches("(os )?nove") || input.matches("non([ao])")) number = 9f
    if (input.matches("dez") || input.matches("décim([ao])")) number = 10f
    if (input.matches("(os )?onze") || input.matches("décima primeira")
      || input.matches("décimo primeiro")) number = 11f
    if (input.matches("(os )?doze") || input.matches("décimo segundo")
      || input.matches("décima segunda")) number = 12f
    if (input.matches("(os )?treze") || input.matches("décimo terceiro")) number = 13f
    if (input.matches("quatorze") || input.matches("(o )?catorze")
      || input.matches("décimo quarto")) number = 14f
    if (input.matches("quinze") || input.matches("décimo quinto")) number = 15f
    if (input.matches("(as )?dezesseis") || input.matches("décimo sexto")) number = 16f
    if (input.matches("(os )?dezessete") || input.matches("décimo sétimo")) number = 17f
    if (input.matches("(os )?dezoito") || input.matches("décimo oitavo")) number = 18f
    if (input.matches("(as )?dezenove") || input.matches("décimo nono")) number = 19f
    if (input.matches("vinte") || input.matches("vigésim([ao])")) number = 20f
    if (input.matches("trinta") || input.matches("trigésim([ao])")) number = 30f
    if (input.matches("(os )?quarenta") || input.matches("(o )?quadragésimo")) number = 40f
    if (input.matches("(os )?cinq([üu])enta") || input.matches("quinquagésim([ao])")) number = 50f
    if (input.matches("(os )?sessenta") || input.matches("(o )?sexagésimo")) number = 60f
    if (input.matches("setenta") || input.matches("septuagésim([ao])")) number = 70f
    if (input.matches("(as )?oitenta") || input.matches("octogésim([ao])")) number = 80f
    if (input.matches("(os )?noventa") || input.matches("(o )?nonagésim([ao])")) number = 90f
    return number
  }

  override fun hasShowAction(input: String): Boolean {
    return input.matches(".*mostre.*") || input.matches(".*mostrar.*")
  }

  override fun getShowAction(input: String): Action? {
    if (input.matches(".*aniversários.*")) {
      return Action.BIRTHDAYS
    } else if (input.matches(".*lembretes ativos.*")) {
      return Action.ACTIVE_REMINDERS
    } else if (input.matches(".*lembretes.*")) {
      return Action.REMINDERS
    } else if (input.matches(".*eventos.*")) {
      return Action.EVENTS
    } else if (input.matches(".*notas.*")) {
      return Action.NOTES
    } else if (input.matches(".*grupos.*")) {
      return Action.GROUPS
    } else if (input.matches(".*listas? de compras.*")) {
      return Action.SHOP_LISTS
    }
    return null
  }

  override fun hasNextModifier(input: String): Boolean {
    return input.matches(".*(nos|para)? ?próximos.*")
  }
}