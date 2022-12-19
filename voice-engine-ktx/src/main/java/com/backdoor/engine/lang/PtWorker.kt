package com.backdoor.engine.lang

import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.Ampm
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import java.util.regex.Pattern

internal class PtWorker(zoneId: ZoneId) : Worker(zoneId) {

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

  override fun clearCalendar(input: String) =
    input.splitByWhitespaces()
      .toMutableList()
      .let {
        it.forEachIndexed { index, s ->
          if (s.matches(".*calendário.*")) {
            it[index] = ""
            if (index > 0 && it[index - 1].matches("([ao])")) {
              it[index - 1] = ""
            }
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
    input.matches(".*cada.*") || input.matches(".*tod([ao])s( as)?.*") || hasEveryDay(input)

  override fun hasEveryDay(input: String) = input.matches(".*diário.*")

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
    input.matches(".*amanh([ãa]).*") || input.matches(".*próximo dia.*")

  override fun clearTomorrow(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (s.matches(".*amanh([ãa]).*") || s.matches(".*próximo dia.*")) {
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
            if (it[index - 1].matches("com")) it[index - 1] = ""
          }
          it[index] = ""
        }
      }
    }.clip()

  override fun getMessageType(input: String) = when {
    input.matches(".*mensagem.*") -> Action.MESSAGE
    input.matches(".*carta.*") -> Action.MAIL
    else -> null
  }

  override fun clearMessageType(input: String) =
    input.splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        if (getMessageType(s) != null) {
          it[index] = ""
          val nextIndex = index + 1
          if (nextIndex < it.size && it[nextIndex].matches("para")) {
            it[nextIndex] = ""
          }
          return@forEachIndexed
        }
      }
    }.clip()

  override fun getAmpm(input: String) = when {
    input.matches(".*(de )?manhã.*") -> Ampm.MORNING
    input.matches(".*tarde.*") -> Ampm.EVENING
    input.matches(".*meio-dia.*") -> Ampm.NOON
    input.matches(".*noite.*") -> Ampm.NIGHT
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
        if (!s.matches("at")) sb.append(" ").append(s.trim())
      }
      sb.toString().trim()
    } ?: ""

  override fun getMonth(input: String?) = when {
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

  override fun hasCall(input: String) = input.matches(".*ligue.*")

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
    it.matches(".*após.*") || it.matches(".* em .*") || it.matches(".*depois( de)?.*")
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

  override fun getDate(input: String, result: (LocalDate?) -> Unit): String? {
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
    }.clip().also {
      result(localDate)
    }
  }

  override fun hasSender(input: String) =
    input.matches(".*mandar.*") || input.matches(".*enviar.*")

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
    return (input.startsWith("abrir") || input.startsWith("aberto")
      || input.matches(".*ajuda.*")
      || input.matches(".*ajustar.*")
      || input.matches(".*relatório.*")
      || input.matches(".*mudança.*"))
  }

  override fun getAction(input: String) = when {
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

  override fun getEvent(input: String) = when {
    input.matches(".*aniversário.*") -> Action.BIRTHDAY
    input.matches(".*(o )?lembrete.*") -> Action.REMINDER
    else -> Action.NO_EVENT
  }

  override fun hasEmptyTrash(input: String) = input.matches(".*lixo vazio.*")

  override fun hasDisableReminders(input: String) = input.matches(".*desativar lembrete.*")

  override fun hasGroup(input: String) = input.matches(".*adicionar grupo.*")

  override fun clearGroup(input: String): String {
    val sb = StringBuilder()
    val parts: Array<String> = input.splitByWhitespaces().toTypedArray()
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
    return sb.toString().trim()
  }

  override fun hasToday(input: String) = input.matches(".*hoje.*")

  override fun hasAfterTomorrow(input: String) = input.matches(".*depois de amanhã.*")

  override val afterTomorrow = "depois de amanhã"

  override fun hasHours(input: String?) = when {
    input.matchesOrFalse(".*hora.*") || input.matchesOrFalse(".*horas.*") -> 1
    else -> -1
  }

  override fun hasMinutes(input: String?) = when {
    input.matchesOrFalse(".*minuto.*") -> 1
    else -> -1
  }

  override fun hasSeconds(input: String?) = input.matchesOrFalse(".*segund([ao])?.*")

  override fun hasDays(input: String?) = input.matchesOrFalse(".*dia.*")

  override fun hasWeeks(input: String?) = input.matchesOrFalse(".*semana.*")

  override fun hasMonth(input: String?) =
    input.matchesOrFalse(".*mês.*") || input.matchesOrFalse(".*meses.*")

  override fun hasAnswer(input: String) = input.let { " $it " }.matches(".* (sim|não) .*")

  override fun getAnswer(input: String) = when {
    input.matches(".* ?sim ?.*") -> Action.YES
    else -> Action.NO
  }

  override fun findFloat(input: String?) = when {
    input.matchesOrFalse("mei([ao])") -> 0.5f
    else -> -1f
  }

  override fun clearFloats(input: String?) = when {
    input == null -> null
    input.contains("e meio") -> input.replace("e meio", "")
    input.contains("e meia") -> input.replace("e meia", "")
    input.contains("meio") -> input.replace("meio", "")
    input.contains("ao meio") -> input.replace("ao meio", "")
    else -> input
  }

  override fun findNumber(input: String?) = when {
    input == null -> -1f
    input.matches("zero") || input.matches("nulo") -> 0f
    input.matches("uma?") || input.matches("primeir([ao])") -> 1f
    input.matches("dois") || input.matches("duas") || input.matches("segund([ao])") -> 2f
    input.matches("(os )?três") || input.matches("terceir([ao])") -> 3f
    input.matches("quatro") || input.matches("quarto") -> 4f
    input.matches("cinco") || input.matches("quint([ao])") -> 5f
    input.matches("(as )?seis") || input.matches("sext([ao])") -> 6f
    input.matches("sete") || input.matches("sétim([ao])") -> 7f
    input.matches("(os )?oito") || input.matches("oitav([ao])") -> 8f
    input.matches("(os )?nove") || input.matches("non([ao])") -> 9f
    input.matches("dez") || input.matches("décim([ao])") -> 10f
    input.matches("(os )?onze") || input.matches("décima primeira") || input.matches("décimo primeiro") -> 11f
    input.matches("(os )?doze") || input.matches("décimo segundo") || input.matches("décima segunda") -> 12f
    input.matches("(os )?treze") || input.matches("décimo terceiro") -> 13f
    input.matches("quatorze") || input.matches("(o )?catorze") || input.matches("décimo quarto") -> 14f
    input.matches("quinze") || input.matches("décimo quinto") -> 15f
    input.matches("(as )?dezesseis") || input.matches("décimo sexto") -> 16f
    input.matches("(os )?dezessete") || input.matches("décimo sétimo") -> 17f
    input.matches("(os )?dezoito") || input.matches("décimo oitavo") -> 18f
    input.matches("(as )?dezenove") || input.matches("décimo nono") -> 19f
    input.matches("vinte") || input.matches("vigésim([ao])") -> 20f
    input.matches("trinta") || input.matches("trigésim([ao])") -> 30f
    input.matches("(os )?quarenta") || input.matches("(o )?quadragésimo") -> 40f
    input.matches("(os )?cinq([üu])enta") || input.matches("quinquagésim([ao])") -> 50f
    input.matches("(os )?sessenta") || input.matches("(o )?sexagésimo") -> 60f
    input.matches("setenta") || input.matches("septuagésim([ao])") -> 70f
    input.matches("(as )?oitenta") || input.matches("octogésim([ao])") -> 80f
    input.matches("(os )?noventa") || input.matches("(o )?nonagésim([ao])") -> 90f
    else -> -1f
  }

  override fun hasShowAction(input: String) =
    input.matches(".*mostre.*") || input.matches(".*mostrar.*")

  override fun getShowAction(input: String) = when {
    input.matches(".*aniversários.*") -> Action.BIRTHDAYS
    input.matches(".*lembretes ativos.*") -> Action.ACTIVE_REMINDERS
    input.matches(".*lembretes.*") -> Action.REMINDERS
    input.matches(".*eventos.*") -> Action.EVENTS
    input.matches(".*notas.*") -> Action.NOTES
    input.matches(".*grupos.*") -> Action.GROUPS
    input.matches(".*listas? de compras.*") -> Action.SHOP_LISTS
    else -> null
  }

  override fun hasNextModifier(input: String) = input.matches(".*(nos|para)? ?próximos.*")
}