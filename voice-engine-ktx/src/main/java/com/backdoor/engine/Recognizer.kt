package com.backdoor.engine

import com.backdoor.engine.lang.Worker
import com.backdoor.engine.lang.WorkerFactory.getWorker
import com.backdoor.engine.lang.clip
import com.backdoor.engine.lang.splitByWhitespaces
import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.ActionType
import com.backdoor.engine.misc.Ampm
import com.backdoor.engine.misc.ContactsInterface
import com.backdoor.engine.misc.Logger
import com.backdoor.engine.misc.Logger.log
import com.backdoor.engine.misc.LongInternal
import com.backdoor.engine.misc.TimeUtil.getGmtFromDateTime
import org.apache.commons.lang3.StringUtils
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import java.util.Locale

class Recognizer private constructor(
  locale: String,
  private val times: List<String>,
  timeZone: String
) {

  var enableLogging: Boolean = true
    set(value) {
      field = value
      Logger.LOG_ENABLED = value
    }
  private var contactsInterface: ContactsInterface? = null
  private var zoneId = ZoneId.of(timeZone)
  private var worker = getWorker(locale, zoneId)

  fun updateLocale(locale: String) {
    worker = getWorker(locale, zoneId)
  }

  fun recognize(input: String): Model? {
    log("parse: input = $input, worker = $worker")
    return input.lowercase(LOCALE).trim()
      .let { worker.replaceNumbers(it) ?: "" }
      .also { log("parse: after numbers replaced = $it") }
      .let { s ->
        val showAction = worker.getShowAction(s)
        val event = getEvent(s)
        when {
          worker.hasShowAction(s) && showAction != null ->
            createAction(worker.clearShowAction(s), showAction)
          worker.hasNote(s) -> getNote(input)
          worker.hasGroup(s) -> getGroup(worker.clearGroup(s))
          worker.hasEvent(s) && event != null -> event
          worker.hasAction(s) -> getAction(s)
          worker.hasEmptyTrash(s) -> emptyTrash
          worker.hasDisableReminders(s) -> disableAction
          worker.hasAnswer(s) -> getAnswer(s)
          else -> parseReminder(s, input)
        }
      }
  }

  private fun parseReminder(input: String, origin: String): Model? {
    log("parseReminder: $input")
    return Proc(input = input)
      .also { proc ->
        if (worker.hasCall(proc.input)) {
          proc.updateInput { worker.clearCall(it) }
          proc.hasAction = true
          proc.action = Action.CALL
        }
      }
      .also { proc ->
        if (worker.hasSender(proc.input)) {
          proc.updateInput { worker.clearSender(it) }
          worker.getMessageType(proc.input)?.also { action ->
            proc.updateInput { worker.clearMessageType(it) }
            proc.hasAction = true
            proc.action = action
          }
        }
      }
      .also { proc ->
        log("parse: has repeat -> $proc")
        if (worker.hasRepeat(proc.input).also { proc.isRepeating = it }) {
          proc.isEveryDay = worker.hasEveryDay(proc.input)
          proc.updateInput { worker.clearRepeat(it) }
          proc.repeatMillis = worker.getDaysRepeat(proc.input)
          if (proc.repeatMillis != 0L) {
            proc.updateInput { worker.clearDaysRepeat(it) }
          }
          if (proc.repeatMillis == 0L && proc.isEveryDay) {
            proc.repeatMillis = Worker.DAY
          }
        }
      }
      .also { proc ->
        log("parse: calendar $proc")
        proc.hasCalendar = worker.hasCalendar(proc.input).also { b ->
          if (b) proc.updateInput { worker.clearCalendar(it) }
        }
      }
      .also { proc ->
        log("parse: today $proc")
        proc.hasToday = worker.hasToday(proc.input).also { b ->
          if (b) proc.updateInput { worker.clearToday(it) }
        }
      }
      .also { proc ->
        log("parse: after tomorrow $proc")
        proc.hasAfterTomorrow = worker.hasAfterTomorrow(proc.input).also { b ->
          if (b) proc.updateInput { worker.clearAfterTomorrow(it) }
        }
      }
      .also { proc ->
        log("parse: tomorrow $proc")
        proc.hasTomorrow = worker.hasTomorrow(proc.input).also { b ->
          if (b) proc.updateInput { worker.clearTomorrow(it) }
        }
      }
      .also { proc ->
        log("parse: timer check $proc")
        proc.hasTimer = worker.hasTimer(proc.input).also { b ->
          if (b) {
            proc.updateInput { worker.cleanTimer(it) }
            worker.getMultiplier(proc.input, proc.afterTime).also {
              proc.input = it
            }
          }
        }
      }
      .also { proc ->
        log("parse: ampm $proc")
        proc.ampm = worker.getAmpm(proc.input)?.also {
          proc.updateInput { worker.clearAmpm(it) }
        }
      }
      .also { proc ->
        log("parse: before weekdays $proc")
        if (!proc.isEveryDay) {
          proc.weekdays = worker.getWeekDays(proc.input)
          proc.hasWeekday = proc.weekdays.any { it == 1 }.also { b ->
            if (b) {
              proc.action = when (proc.action) {
                Action.CALL -> Action.WEEK_CALL
                Action.MESSAGE -> Action.WEEK_SMS
                else -> Action.WEEK
              }
              proc.input = worker.clearWeekDays(proc.input)
            }
          }
        }
      }
      .also { proc ->
        log("parse: date ${proc.input}")
        proc.updateInput { s ->
          worker.getDate(s) { proc.date = it }
        }
      }
      .also { proc ->
        log("parse: time $proc")
        proc.time = worker.getTime(proc.input, proc.ampm, times)?.also {
          proc.updateInput { worker.clearTime(it) }
        }
      }
      .also { proc ->
        if (proc.hasToday) {
          log("parse: today")
          proc.dateTime = getTodayTime(proc.time)
        } else if (proc.hasAfterTomorrow) {
          log("parse: after tomorrow")
          proc.dateTime = getAfterTomorrowTime(proc.time)
        } else if (proc.hasTomorrow) {
          log("parse: tomorrow")
          proc.dateTime = getTomorrowTime(proc.time)
        } else if (proc.hasWeekday && !proc.isRepeating) {
          log("parse: on weekday")
          proc.dateTime = getDayTime(proc.time, proc.weekdays)
        } else if (proc.isRepeating) {
          log("parse: repeating")
          proc.dateTime = getDateTime(proc.date, proc.time)
        } else if (proc.hasTimer) {
          log("parse: timer")
          proc.dateTime = LocalDateTime.now().plusSeconds(proc.afterTime.value / 1000L)
        } else if (proc.date != null || proc.time != null) {
          log("parse: date/time")
          proc.dateTime = getDateTime(proc.date, proc.time)
        } else {
          proc.skipNext = true
        }
      }
      .takeIf { !it.skipNext }
      ?.also { proc ->
        log("parse: message -> $proc")
        if (proc.hasAction && (proc.action == Action.MESSAGE || proc.action == Action.MAIL)) {
          proc.message = worker.getMessage(proc.input)
          proc.updateInput { worker.clearMessage(it) }
          proc.message?.also { message ->
            proc.updateInput { it.replace(message, "") }
          }
        }
      }
      ?.also { proc ->
        if (proc.hasAction) {
          if (proc.action == Action.CALL || proc.action == Action.MESSAGE) {
            contactsInterface?.also { findNumber(proc, it) }
          } else if (proc.action == Action.MAIL) {
            contactsInterface?.also { findEmail(proc, it) }
          }
          proc.skipNext = proc.number == null
        }
      }
      ?.takeIf { !it.skipNext }
      ?.also { proc ->
        if (proc.hasAction) {
          proc.summary = if (proc.action == Action.CALL) {
            StringUtils.capitalize(origin)
          } else {
            StringUtils.capitalize(proc.message)
          }
          if ((proc.action == Action.MESSAGE || proc.action == Action.MAIL) && proc.summary.isEmpty()) {
            proc.skipNext = true
          }
        } else {
          proc.summary = StringUtils.capitalize(StringUtils.normalizeSpace(proc.input))
        }
      }
      ?.takeIf { !it.skipNext }
      ?.let {
        Model(
          type = it.actionType,
          summary = it.summary,
          dateTime = getGmtFromDateTime(it.dateTime),
          weekdays = it.weekdays,
          repeatInterval = it.repeatMillis,
          target = it.number,
          hasCalendar = it.hasCalendar,
          action = it.action,
          afterMillis = it.afterTime.value
        )
      }
      ?.also { log("parse: out = $it") }
  }

  private fun findNumber(proc: Proc, contactsInterface: ContactsInterface) {
    var number: String? = null
    val output = proc.input.trim().splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        val res = contactsInterface.findNumber(s)
        if (res != null) {
          number = res
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()
    if (number == null) {
      proc.skipNext = true
    } else {
      proc.number = number
      proc.input = output
    }
  }

  private fun findEmail(proc: Proc, contactsInterface: ContactsInterface) {
    var email: String? = null
    val output = proc.input.trim().splitByWhitespaces().toMutableList().also {
      it.forEachIndexed { index, s ->
        val res = contactsInterface.findEmail(s)
        if (res != null) {
          email = res
          it[index] = ""
          return@forEachIndexed
        }
      }
    }.clip()
    proc.number = email
    proc.input = output
  }

  private fun createAction(input: String, action: Action): Model {
    val hasNext = worker.hasNextModifier(input)
    val multi = LongInternal()
    val date = worker.getMultiplier(input, multi).let { output ->
      if (hasNext) {
        nowDateTime().plusSeconds(multi.value / 1000L)
      } else {
        var date: LocalDate? = null
        worker.getDate(output) { date = it }
        date?.atTime(12, 0)
      }
    }
    return Model(
      action = action,
      type = ActionType.SHOW,
      dateTime = getGmtFromDateTime(date),
      repeatInterval = multi.value
    )
  }

  private fun getAnswer(input: String?): Model? {
    return input?.let {
      worker.getAnswer(it)
    }?.let {
      Model(
        type = ActionType.ANSWER,
        action = it
      )
    }
  }

  private fun getDateTime(date: LocalDate?, time: LocalTime?): LocalDateTime? {
    val localDate = date ?: LocalDate.now(zoneId)
    val localTime = time ?: LocalTime.now(zoneId)

    return nowDateTime().withYear(localDate.year)
      .withMonth(localDate.monthValue)
      .withDayOfMonth(localDate.dayOfMonth)
      .withHour(localTime.hour)
      .withMinute(localTime.minute)
      .withSecond(0)
  }

  private fun getDayTime(time: LocalTime?, weekdays: List<Int>): LocalDateTime? {
    val localTime = time ?: LocalTime.now(zoneId)

    val dateTime = nowDateTime()
      .withHour(localTime.hour)
      .withMinute(localTime.minute)
      .withSecond(0)

    val count = Worker.getNumberOfSelectedWeekdays(weekdays)

    return if (count == 1) {
      val list = weekdays.toMutableList()
      val sunday = list.removeAt(0)
      list.add(sunday)

      val nowDateTime = nowDateTime()
      var tmpDateTime = dateTime
      while (true) {
        val datOfWeek = tmpDateTime.dayOfWeek.value
        if (weekdays[datOfWeek - 1] == 1 && tmpDateTime.isAfter(nowDateTime)) {
          break
        }
        tmpDateTime = tmpDateTime.plusDays(1L)
      }
      tmpDateTime
    } else {
      dateTime
    }
  }

  private fun getTomorrowTime(time: LocalTime?) = getTime(time, 1)

  private fun getAfterTomorrowTime(time: LocalTime?) = getTime(time, 2)

  private fun getTodayTime(time: LocalTime?) = getTime(time, 0)

  private fun getTime(time: LocalTime?, days: Int): LocalDateTime? {
    if (time == null) return null
    val dateTime = nowDateTime().withHour(time.hour).withMinute(time.minute)
    if (days == 0) return dateTime
    return dateTime.plusDays(days.toLong())
  }

  private fun nowDateTime() = LocalDateTime.now(zoneId)

  private val disableAction = Model(
    type = ActionType.ACTION,
    action = Action.DISABLE
  )

  private val emptyTrash = Model(
    type = ActionType.ACTION,
    action = Action.TRASH
  )

  private fun getGroup(input: String): Model {
    return Model(
      type = ActionType.GROUP,
      summary = input.let { StringUtils.capitalize(it) }
    )
  }

  private fun getEvent(input: String?): Model? {
    return input?.let { worker.getEvent(it) }
      ?.let {
        Model(
          type = ActionType.ACTION,
          action = it
        )
      }
  }

  private fun getAction(input: String?): Model? {
    return input?.let { worker.getAction(it) }
      ?.let {
        Model(
          type = ActionType.ACTION,
          action = it
        )
      }
  }

  private fun getNote(input: String?): Model? {
    return input?.let { StringUtils.capitalize(worker.clearNote(input)) }?.let {
      Model(
        summary = it,
        type = ActionType.NOTE
      )
    }
  }

  fun setContactHelper(contactsInterface: ContactsInterface?) {
    this.contactsInterface = contactsInterface
  }

  class Builder {
    fun setLocale(locale: String): TimeBuilder {
      return TimeBuilder(locale)
    }

    inner class TimeBuilder internal constructor(
      private val locale: String
    ) {
      fun setTimes(times: List<String>): TimeZoneBuilder {
        return TimeZoneBuilder(locale, times)
      }
    }

    inner class TimeZoneBuilder internal constructor(
      private val locale: String,
      private val times: List<String>
    ) {
      fun setTimeZone(timeZone: String): EndBuilder {
        return EndBuilder(locale, times, timeZone)
      }
    }

    inner class EndBuilder internal constructor(
      private val locale: String,
      private val times: List<String>,
      private val timeZone: String
    ) {

      fun build(): Recognizer {
        return Recognizer(locale, times, timeZone)
      }
    }
  }

  private data class Proc(
    var input: String,
    var action: Action = Action.DATE,
    var number: String? = null,
    var message: String? = null,
    var hasAction: Boolean = false,
    var actionType: ActionType = ActionType.REMINDER,
    var skipNext: Boolean = false,
    var summary: String = "",
    var isRepeating: Boolean = false,
    var isEveryDay: Boolean = false,
    var repeatMillis: Long = 0,
    var weekdays: List<Int> = listOf(),
    var hasCalendar: Boolean = false,
    var hasToday: Boolean = false,
    var hasAfterTomorrow: Boolean = false,
    var hasTomorrow: Boolean = false,
    var ampm: Ampm? = null,
    var hasWeekday: Boolean = false,
    var hasTimer: Boolean = false,
    var afterTime: LongInternal = LongInternal(),
    var date: LocalDate? = null,
    var time: LocalTime? = null,
    var dateTime: LocalDateTime? = null
  ) {
    fun updateInput(f: (String) -> String?) {
      input = f.invoke(input) ?: ""
    }
  }

  internal companion object {
    var LOCALE: Locale = Locale.getDefault()
  }
}
