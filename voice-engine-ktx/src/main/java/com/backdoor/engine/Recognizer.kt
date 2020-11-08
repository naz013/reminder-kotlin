package com.backdoor.engine

import com.backdoor.engine.lang.Worker
import com.backdoor.engine.lang.WorkerFactory.getWorker
import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.ActionType
import com.backdoor.engine.misc.Ampm
import com.backdoor.engine.misc.ContactsInterface
import com.backdoor.engine.misc.LongInternal
import com.backdoor.engine.misc.TimeUtil.getGmtFromDateTime
import org.apache.commons.lang3.StringUtils
import java.util.*

class Recognizer private constructor(
  locale: String,
  private val times: List<String>,
  private var contactsInterface: ContactsInterface?
) {
  private var worker = getWorker(locale)

  fun updateLocale(locale: String) {
    worker = getWorker(locale)
  }

  fun setLocale(locale: Locale) {
    LOCALE = locale
  }

  fun parse(input: String): Model? {
    return input.toLowerCase(LOCALE)
      .let { s -> s.trim { it <= ' ' } }
      .let { worker.replaceNumbers(it) ?: "" }
      .also { println("parse: $it, worker $worker") }
      .let { s ->
        val action = worker.getAction(s)
        val event = getEvent(s)
        when {
          worker.hasShowAction(s) && action != null -> createAction(s, action)
          worker.hasNote(s) -> getNote(s)
          worker.hasGroup(s) -> getGroup(worker.clearGroup(s))
          worker.hasEvent(s) && event != null -> event
          worker.hasAction(s) -> getAction(s)
          worker.hasEmptyTrash(s) -> emptyTrash
          worker.hasDisableReminders(s) -> disableAction
          worker.hasAnswer(s) -> getAnswer(s)
          else -> parseReminder(s)
        }
      }
  }

  private fun parseReminder(input: String): Model? {
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
        if (worker.hasRepeat(proc.input).also { proc.isRepeating = it }) {
          proc.isEveryDay = worker.hasEveryDay(proc.input)
          proc.updateInput { worker.clearRepeat(it) }
          println("parse: has repeat -> $proc")
          proc.repeatMillis = worker.getDaysRepeat(proc.input)
          if (proc.repeatMillis != 0L) {
            proc.updateInput { worker.clearDaysRepeat(it) }
          }
          if (proc.isEveryDay) {
            proc.weekdays = listOf(1, 1, 1, 1, 1, 1, 1)
            proc.action = when (proc.action) {
              Action.CALL -> Action.WEEK_CALL
              Action.MESSAGE -> Action.WEEK_SMS
              else -> Action.WEEK
            }
          }
        }
      }
      .also { proc ->
        proc.hasCalendar = worker.hasCalendar(proc.input).also { b ->
          if (b) proc.updateInput { worker.clearCalendar(it) }
        }
      }
      .also { proc ->
        proc.hasToday = worker.hasToday(proc.input).also { b ->
          if (b) proc.updateInput { worker.clearToday(it) }
        }
      }
      .also { proc ->
        proc.hasAfterTomorrow = worker.hasAfterTomorrow(proc.input).also { b ->
          if (b) proc.updateInput { worker.clearAfterTomorrow(it) }
        }
      }
      .also { proc ->
        proc.hasTomorrow = worker.hasTomorrow(proc.input).also { b ->
          if (b) proc.updateInput { worker.clearTomorrow(it) }
        }
      }
      .also { proc ->
        proc.ampm = worker.getAmpm(proc.input)?.also {
          proc.updateInput { worker.clearAmpm(it) }
        }
      }
      .also { proc ->
        if (!proc.isEveryDay) {
          proc.weekdays = worker.getWeekDays(proc.input)
          proc.hasWeekday = proc.weekdays.any { it == 1 }.also { b ->
            if (b) {
              proc.action = when (proc.action) {
                Action.CALL -> Action.WEEK_CALL
                Action.MESSAGE -> Action.WEEK_SMS
                else -> Action.WEEK
              }
            }
          }
          proc.updateInput { worker.clearWeekDays(it) }
        }
      }
      .also { proc ->
        proc.hasTimer = worker.hasTimer(proc.input).also { b ->
          if (b) {
            proc.updateInput { worker.cleanTimer(it) }
          }
        }
        worker.getMultiplier(proc.input, proc.afterTime)
        println("parse: ${proc.afterTime}, input: ${proc.input}")
      }
      .also { proc ->
        proc.updateInput { worker.getDate(it, proc.date) }
        println("parse: date ${proc.input}")
      }
      .also { proc ->
        proc.time = worker.getTime(proc.input, proc.ampm, times).also { l ->
          if (l != 0L) proc.updateInput { worker.clearTime(it) }
        }
        println("parse: ${proc.input}, time ${proc.time}, date ${proc.date}")
      }
      .also { proc ->
        if (proc.hasToday) {
          println("parse: today")
          proc.time = getTodayTime(proc.time)
        } else if (proc.hasAfterTomorrow) {
          println("parse: after tomorrow")
          proc.time = getAfterTomorrowTime(proc.time)
        } else if (proc.hasTomorrow) {
          println("parse: tomorrow")
          proc.time = getTomorrowTime(proc.time)
        } else if (proc.isEveryDay) {
          println("parse: everyday")
          proc.time = getDayTime(proc.time, proc.weekdays)
        } else if (proc.hasWeekday && !proc.isRepeating) {
          println("parse: on weekday")
          proc.time = getDayTime(proc.time, proc.weekdays)
        } else if (proc.isRepeating) {
          println("parse: repeating")
          proc.time = getRepeatingTime(proc.time, proc.hasWeekday)
        } else if (proc.hasTimer) {
          println("parse: timer")
          proc.time = System.currentTimeMillis() + proc.afterTime.value
        } else if (proc.date.value != 0L || proc.time != 0L) {
          println("parse: date/time")
          proc.time = getDateTime(proc.date.value, proc.time)
        } else {
          proc.skipNext = true
        }
      }
      .takeIf { !it.skipNext }
      ?.also { proc ->
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
          contactsInterface?.findNumber(proc.input).also { output ->
            if (output == null) {
              proc.skipNext = true
            } else {
              proc.updateInput { output.output }
              proc.number = output.number
            }
          }
          if (proc.action == Action.MAIL) {
            contactsInterface?.findEmail(proc.input).also { output ->
              proc.number = output?.number
              proc.updateInput { output?.output }
            }
          }
          proc.skipNext = proc.number == null
        }
      }
      ?.takeIf { !it.skipNext }
      ?.also { proc ->
        if (proc.hasAction) {
          proc.summary = StringUtils.capitalize(proc.message)
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
          dateTime = getGmtFromDateTime(it.time),
          weekdays = it.weekdays,
          repeatInterval = it.repeatMillis,
          target = it.number,
          hasCalendar = it.hasCalendar,
          action = it.action
        )
      }
  }

  private fun createAction(input: String, action: Action): Model? {
    val hasNext = worker.hasNextModifier(input)
    val multi = LongInternal()
    val date = worker.getMultiplier(input, multi).let {
      if (hasNext) {
        System.currentTimeMillis() + multi.value
      } else {
        val dt = LongInternal()
        worker.getDate(it, dt)
        dt.value
      }
    }
    return Model(
      action = action,
      type = ActionType.SHOW,
      dateTime = getGmtFromDateTime(date)
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

  private fun getDateTime(date: Long, time: Long): Long {
    val dateMillis = date.takeIf { it != 0L } ?: System.currentTimeMillis()
    val timeMillis = time.takeIf { it != 0L } ?: dateMillis
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timeMillis
    val hour = calendar[Calendar.HOUR_OF_DAY]
    val minute = calendar[Calendar.MINUTE]
    calendar.timeInMillis = dateMillis
    calendar[Calendar.HOUR_OF_DAY] = hour
    calendar[Calendar.MINUTE] = minute
    calendar[Calendar.SECOND] = 0
    calendar[Calendar.MILLISECOND] = 0
    if (calendar.timeInMillis < System.currentTimeMillis()) {
      calendar.timeInMillis = calendar.timeInMillis + Worker.DAY
    }
    return calendar.timeInMillis
  }

  private fun getRepeatingTime(time: Long, hasWeekday: Boolean): Long {
    return (time.takeIf { it != 0L } ?: System.currentTimeMillis()).let {
      val calendar = Calendar.getInstance()
      calendar.timeInMillis = it
      val hour = calendar[Calendar.HOUR_OF_DAY]
      val minute = calendar[Calendar.MINUTE]
      calendar.timeInMillis = System.currentTimeMillis()
      calendar[Calendar.HOUR_OF_DAY] = hour
      calendar[Calendar.MINUTE] = minute
      calendar[Calendar.SECOND] = 0
      calendar[Calendar.MILLISECOND] = 0
      if (!hasWeekday) {
        if (calendar.timeInMillis < System.currentTimeMillis()) {
          calendar.timeInMillis = calendar.timeInMillis + Worker.DAY
        }
      }
      calendar.timeInMillis
    }
  }

  private fun getDayTime(time: Long, weekdays: List<Int>): Long {
    return (time.takeIf { it != 0L } ?: System.currentTimeMillis()).let {
      val calendar = Calendar.getInstance()
      calendar.timeInMillis = it
      val hour = calendar[Calendar.HOUR_OF_DAY]
      val minute = calendar[Calendar.MINUTE]
      calendar.timeInMillis = System.currentTimeMillis()
      calendar[Calendar.HOUR_OF_DAY] = hour
      calendar[Calendar.MINUTE] = minute
      calendar[Calendar.SECOND] = 0
      calendar[Calendar.MILLISECOND] = 0
      val count = Worker.getNumberOfSelectedWeekdays(weekdays)
      if (count == 1) {
        while (true) {
          val mDay = calendar[Calendar.DAY_OF_WEEK]
          if (weekdays[mDay - 1] == 1 && calendar.timeInMillis > System.currentTimeMillis()) {
            break
          }
          calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
      }
      calendar.timeInMillis
    }
  }

  private fun getTomorrowTime(time: Long) = getTime(time, 1)

  private fun getAfterTomorrowTime(time: Long) = getTime(time, 2)

  private fun getTodayTime(time: Long) = getTime(time, 0)

  private fun getTime(time: Long, days: Int): Long {
    return (time.takeIf { it != 0L } ?: System.currentTimeMillis()).let {
      val calendar = Calendar.getInstance()
      calendar.timeInMillis = it
      val hour = calendar[Calendar.HOUR_OF_DAY]
      val minute = calendar[Calendar.MINUTE]
      calendar.timeInMillis = System.currentTimeMillis() + Worker.DAY * days
      calendar[Calendar.HOUR_OF_DAY] = hour
      calendar[Calendar.MINUTE] = minute
      calendar[Calendar.SECOND] = 0
      calendar[Calendar.MILLISECOND] = 0
      calendar.timeInMillis
    }
  }

  private val disableAction = Model(
    type = ActionType.ACTION,
    action = Action.DISABLE
  )

  private val emptyTrash = Model(
    type = ActionType.ACTION,
    action = Action.TRASH
  )

  private fun getGroup(input: String): Model? {
    return input.let {
      StringUtils.capitalize(worker.clearGroup(it))
    }.let {
      Model(
        type = ActionType.GROUP,
        summary = it
      )
    }
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
      fun setTimes(times: List<String>): ExtraBuilder {
        return ExtraBuilder(locale, times)
      }
    }

    inner class ExtraBuilder internal constructor(
      private val locale: String,
      private val times: List<String>
    ) {
      private var contactsInterface: ContactsInterface? = null
      fun setContactsInterface(contactsInterface: ContactsInterface?): ExtraBuilder {
        this.contactsInterface = contactsInterface
        return this
      }

      fun build(): Recognizer {
        return Recognizer(locale, times, contactsInterface)
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
    var date: LongInternal = LongInternal(),
    var time: Long = 0
  ) {
    fun updateInput(f: (String) -> String?) {
      input = f.invoke(input) ?: ""
    }
  }

  internal companion object {
    var LOCALE: Locale = Locale.getDefault()
  }
}