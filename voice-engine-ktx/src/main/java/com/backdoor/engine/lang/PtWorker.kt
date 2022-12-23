package com.backdoor.engine.lang

import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import com.backdoor.engine.misc.ContactsInterface
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import java.util.regex.Pattern

internal class PtWorker(zoneId: ZoneId, contactsInterface: ContactsInterface?) :
  Worker(zoneId, contactsInterface) {

  override val weekdays: List<String> = listOf(
    "doming",
    "segunda(-feira|s)",
    "terças?",
    "quarta(-feira|s)",
    "quinta(-feira|s)",
    "sexta( feira|s)",
    "sábados?"
  )

  override fun hasCalendar(input: String): Boolean = input.matches(".*calendário.*")

  override fun clearCalendar(input: String): String {
    return input.splitByWhitespaces()
      .toMutableList()
      .also {
        it.forEachIndexed { index, s ->
          if (s.matches(".*calendário.*")) {
            it[index] = ""
            clearAllBackward(it, index - 1, 3, "(e|adicione|ao)")
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
            clearAllBackward(it, index - 1, 1, "(as|às|e)")
            break
          }
        }
      }
    }.clip().splitByWhitespaces().forEach { s ->
      val part = s.trim()
      if (!part.matches("na") && !part.matches("em")) sb.append(" ").append(part)
    }
    return sb.toString().trim()
  }

  override fun getDaysRepeat(input: String): Long =
    input.splitByWhitespaces().firstOrNull { hasDays(it) }?.toRepeat(1) ?: 0

  override fun clearDaysRepeat(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasDays(" $s")) {
          ignoreAny {
            s.toFloat()
            it[index - 1] = ""
          }
          it[index] = ""
          clearAllBackward(it, index - 1, 1, "os")
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun hasRepeat(input: String): Boolean =
    input.matches(".*cada.*") || input.matches(".*tod(a|o)s( as)?.*") || hasEveryDay(input)

  override fun hasEveryDay(input: String): Boolean = input.matches(".*tod(a|o)s .*dias?.*")

  override fun clearRepeat(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (hasRepeat(s)) {
          it[index] = ""
          clearAllForward(it, index + 1, 1, "(as|os)")
          clearAllBackward(it, index - 1, 2, "(e|repita)")
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun hasTomorrow(input: String): Boolean =
    input.matches(".*amanh([ãa]).*") || input.matches(".*próximo dia.*")

  override fun clearTomorrow(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches(".*amanh([ãa]).*") || s.matches(".*próximo dia.*")) {
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun getAmpm(input: String): Ampm? = when {
    input.matches(".*(de )?manhã.*") -> Ampm.MORNING
    input.matches(".*tarde.*") -> Ampm.EVENING
    input.matches(".*meio-dia.*") -> Ampm.NOON
    input.matches(".*noite.*") -> Ampm.NIGHT
    else -> null
  }

  override fun clearAmpm(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (getAmpm(s) != null) {
          it[index] = ""
          clearAllBackward(it, index - 1, 1, "(à|de|agosto|pela|ao)")
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
          clearAllBackward(it, i - 1, 2, "(às|e|em)")
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
            clearAllBackward(it, i - 1, 1, "(às|e|em)")
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
            clearAllBackward(it, i - 1, 1, "(às|e|em)")
          }
        }
      }
    }?.clip()?.let { s ->
      val matcher = pattern.matcher(s)
      if (matcher.find()) {
        val time = matcher.group().trim()
        s.replace(time, "")
      } else s
    }?.splitByWhitespaces()?.toMutableList()?.let { list ->
      val sb = StringBuilder()
      list.forEach { s ->
        if (!s.matches("at")) sb.append(" ").append(s.trim())
      }
      sb.toString().trim()
    } ?: ""
  }

  override fun getMonth(input: String?): Int = when {
    input == null -> -1
    input.contains("janeiro") -> 1
    input.contains("fevereiro") -> 2
    input.contains("março") || input.contains("marcha") -> 3
    input.contains("abril") -> 4
    input.contains("maio") -> 5
    input.contains("junho") -> 6
    input.contains("julho") -> 7
    input.contains("agosto") -> 8
    input.contains("setembro") -> 9
    input.contains("outubro") -> 10
    input.contains("novembro") -> 11
    input.contains("dezembro") -> 12
    else -> -1
  }

  override fun hasCall(input: String): Boolean = input.matches(".*ligue.*")

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

  override fun hasTimer(input: String): Boolean = input.let { " $it " }.let {
    it.matches(".*após.*") || it.matches(".*depois( d(e|o))?.*")
  }

  override fun getMessage(input: String): String {
    val sb = StringBuilder()
    var isStart = false
    input.splitByWhitespaces().forEach {
      if (isStart) sb.append(" ").append(it)
      if (it.matches("text(o|es)")) isStart = true
    }
    if (sb.isEmpty()) {
      // read backwards
      sb.append(clearMessage(input))
    }
    return sb.toString().trim()
  }

  override fun clearMessage(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches("text(o|es)")) {
          it[index] = ""
          clearAllBackward(it, index - 1, 1, "(com|de)")
        }
      }
    }.clip()
  }

  override fun getMessageType(input: String): Action? = when {
    input.matches(".*mensagem.*") -> Action.MESSAGE
    input.matches(".*carta.*") || input.matches(".*e-mail.*") -> Action.MAIL
    else -> null
  }

  override fun clearMessageType(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (getMessageType(s) != null) {
          it[index] = ""
          clearAllForward(it, index + 1, 1, "para")
          clearAllBackward(it, index - 1, 1, "um")
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
          val dayOfMonth: Int? = crawlBackward(list, index - 1, 2, true, {
            it.toFloat().toInt()
          }) {
            list[it] = ""
            clearAllBackward(list, it - 1, 2, "(em|de|no|dia)")
          }

          if (dayOfMonth != null) {
            val parsedDate = LocalDate.now(zoneId)
              .withDayOfMonth(dayOfMonth)
              .withMonth(month)

            localDate = parsedDate
            list[index] = ""
            clearAllBackward(list, index - 1, 1, "(de|em)")
            return@forEachIndexed
          }
        }
      }
    }.clip().also {
      result(localDate)
    }
  }

  override fun hasSender(input: String): Boolean =
    input.matches(".*mandar.*") || input.matches(".*envi(a|e)r?.*")

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

  override fun hasNote(input: String): Boolean = input.matches(".*notas?.*")

  override fun clearNote(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches("(adicionar|crear|añadir)")) {
          it[index] = ""
          clearAllForward(it, index + 1, 1, "nuevo")
        } else if (s.matches("notas?")) {
          it[index] = ""
          clearAllBackward(it, index - 1, 1, "de")
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun hasAction(input: String): Boolean {
    return input.matches(".*abrir.*") || input.matches(".*aberto.*") ||
      input.matches(".*alterar.*") || input.matches(".*ajuda.*") ||
      input.matches(".*ajustar.*") || input.matches(".*relatório.*") ||
      input.matches(".*mudança.*") || input.matches(".*abra.*")
  }

  override fun getAction(input: String): Action = when {
    input.matches(".*ajuda.*") -> Action.HELP
    input.matches(".*sonoridade.*") || input.matches(".*volume.*") -> Action.VOLUME
    input.matches(".*definições.*") || input.matches(".*configurações.*") -> Action.SETTINGS
    input.matches(".*relatório.*") -> Action.REPORT
    else -> Action.APP
  }

  override fun hasEvent(input: String): Boolean {
    return (input.startsWith("nova") || input.startsWith("novo") ||
      input.startsWith("adicionar") || input.startsWith("crio") ||
      input.startsWith("criar"))
  }

  override fun getEvent(input: String): Action = when {
    input.matches(".*(aniversário|cumpleaños).*") -> Action.BIRTHDAY
    input.matches(".*(o )?(lembrete|recordatorio).*") -> Action.REMINDER
    else -> Action.NO_EVENT
  }

  override fun hasEmptyTrash(input: String): Boolean =
    input.matches(".*(lixo|papelera|limpiar) (la )?(vazio|basura|vacía).*")

  override fun hasDisableReminders(input: String): Boolean =
    input.matches(".*(desactivar|deshabilitar) .*(lembrete|recordatorio).*")

  override fun hasGroup(input: String): Boolean =
    input.matches(".*(adicionar|crear|añadir) .*grupo.*")

  override fun clearGroup(input: String): String {
    return input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches(".*(adicionar|crear|añadir).*")) {
          it[index] = ""
          clearAllForward(it, index + 1, 1, "nuevo")
        } else if (s.matches(".*grupo.*")) {
          it[index] = ""
          clearAllBackward(it, index - 1, 1, "en")
          return@forEachIndexed
        }
      }
    }.clip()
  }

  override fun hasToday(input: String): Boolean = input.matches(".*hoje.*")

  override fun hasAfterTomorrow(input: String): Boolean = input.matches(".*depois de amanhã.*")

  override val afterTomorrow: String = "depois de amanhã"

  override fun hasHours(input: String?): Int = when {
    input.matchesOrFalse(".*hora.*") || input.matchesOrFalse(".*horas.*") -> 1
    else -> -1
  }

  override fun hasMinutes(input: String?): Int = when {
    input.matchesOrFalse(".*minuto.*") -> 1
    else -> -1
  }

  override fun hasSeconds(input: String?): Boolean = input.matchesOrFalse(".*segund(a|o).*")

  override fun hasDays(input: String?): Boolean = input.matchesOrFalse(".* d(i|í)as?.*")

  override fun hasWeeks(input: String?): Boolean = input.matchesOrFalse(".*semana.*")

  override fun hasMonth(input: String?): Boolean =
    input.matchesOrFalse(".*mês.*") || input.matchesOrFalse(".*meses.*")

  override fun hasAnswer(input: String): Boolean =
    input.let { " $it " }.matches(".* (sim|não),? .*")

  override fun getAnswer(input: String): Action = when {
    input.matches(".* ?sim ?.*") -> Action.YES
    else -> Action.NO
  }

  override fun findFloat(input: String?): Float = when {
    input.matchesOrFalse("mei(a|o)") -> 0.5f
    else -> -1f
  }

  override fun clearFloats(input: String?): String? = when {
    input == null -> null
    " $input ".matches(".*e meio .*") -> input.replace("e meio", "")
    " $input ".matches(".*e meia .*") -> input.replace("e meia", "")
    " $input ".matches(".* meio .*") -> input.replace("meio", "")
    " $input ".matches(".*ao meio .*") -> input.replace("ao meio", "")
    else -> input
  }

  override fun findNumber(input: String?): Float = when {
    input == null -> -1f
    input.matches("zero") || input.matches("nulo") -> 0f
    input.matches("uma") || input.matches("primeir(a|o)") -> 1f
    input.matches("dois") || input.matches("duas") || input.matches("segund(a|o)") -> 2f
    input.matches("(os )?três") || input.matches("terceir(a|o)") -> 3f
    input.matches("quatro") || input.matches("quarto") -> 4f
    input.matches("cinco") || input.matches("quint(a|o)") -> 5f
    input.matches("(as )?seis") || input.matches("sext(a|o)") -> 6f
    input.matches("sete") || input.matches("sétim(a|o)") -> 7f
    input.matches("(os )?oito") || input.matches("oitav(a|o)") -> 8f
    input.matches("(os )?nove") || input.matches("non(a|o)") -> 9f
    input.matches("dez") || input.matches("décim(a|o)") -> 10f
    input.matches("(os )?onze") || input.matches("décima primeira") || input.matches("décimo primeiro") -> 11f
    input.matches("(os )?doze") || input.matches("décimo segundo") || input.matches("décima segunda") -> 12f
    input.matches("(os )?treze") || input.matches("décimo terceiro") -> 13f
    input.matches("quatorze") || input.matches("(o )?catorze") || input.matches("décimo quarto") -> 14f
    input.matches("quinze") || input.matches("décimo quinto") -> 15f
    input.matches("(as )?dezesseis") || input.matches("décimo sexto") -> 16f
    input.matches("(os )?dezessete") || input.matches("décimo sétimo") -> 17f
    input.matches("(os )?dezoito") || input.matches("décimo oitavo") -> 18f
    input.matches("(as )?dezenove") || input.matches("décimo nono") -> 19f
    input.matches("vinte") || input.matches("vigésim(a|o)") -> 20f
    input.matches("trinta") || input.matches("trigésim(a|o)") -> 30f
    input.matches("(os )?quarenta") || input.matches("(o )?quadragésimo") -> 40f
    input.matches("(os )?cinq([üu])enta") || input.matches("quinquagésim(a|o)") -> 50f
    input.matches("(os )?sessenta") || input.matches("(o )?sexagésimo") -> 60f
    input.matches("setenta") || input.matches("septuagésim(a|o)") -> 70f
    input.matches("(as )?oitenta") || input.matches("octogésim(a|o)") -> 80f
    input.matches("(os )?noventa") || input.matches("(o )?nonagésim(a|o)") -> 90f
    else -> -1f
  }

  override fun hasShowAction(input: String): Boolean = input.matches(".*mostr(e|ar).*")

  override fun getShowAction(input: String): Action? = when {
    input.matches(".*aniversários.*") || input.matches(".*cumpleaños.*") -> Action.BIRTHDAYS
    hasReminders(input) && input.matches(".*ac?tivos?.*") -> Action.ACTIVE_REMINDERS
    hasReminders(input) -> Action.REMINDERS
    input.matches(".*eventos.*") -> Action.EVENTS
    input.matches(".*notas.*") -> Action.NOTES
    input.matches(".*grupos.*") -> Action.GROUPS
    input.matches(".*listas? (de )?compras?.*") -> Action.SHOP_LISTS
    else -> null
  }

  private fun hasReminders(input: String): Boolean {
    return input.matches(".*recordatorios?.*") || input.matches(".*lembretes?.*")
  }

  override fun hasNextModifier(input: String): Boolean =
    input.matches(".*((nos|para|los) )?próximos.*")

  override fun hasConnectSpecialWord(input: String): Boolean {
    return input.matches("(de|às)")
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
        var integer = 1f
        var hourSuccess = false
        ignoreAny {
          integer = parts[i - hoursIndex].toFloat()
          hourSuccess = true
          parts[i - hoursIndex] = ""
        }
        if (ampm == Ampm.EVENING) {
          integer += 12f
        }
        h = integer
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
    if (reserveHour != -1f && reserveHour != 1f) {
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
}
