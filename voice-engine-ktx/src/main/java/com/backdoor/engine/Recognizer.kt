package com.backdoor.engine

import com.backdoor.engine.lang.Worker
import com.backdoor.engine.lang.WorkerFactory.getWorker
import com.backdoor.engine.lang.WorkerInterface
import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.ActionType
import com.backdoor.engine.misc.Ampm
import com.backdoor.engine.misc.ContactOutput
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

  fun parse(string: String): Model? {
    var keyStr: String? = string.toLowerCase().trim { it <= ' ' }
    keyStr = worker!!.replaceNumbers(keyStr)
    println("parse: $keyStr, worker $worker")
    if (worker!!.hasShowAction(keyStr!!)) {
      val local = keyStr + ""
      val action = worker!!.getShowAction(local)
      if (action != null) {
        val hasNext = worker!!.hasNextModifier(local)
        val date: Long
        val multi = LongInternal()
        worker!!.getMultiplier(local, multi)
        if (hasNext) {
          date = System.currentTimeMillis() + multi.get()
        } else {
          val dt = LongInternal()
          worker!!.getDate(local, dt)
          date = dt.get()
        }
        val model = Model()
        model.setAction(action)
        model.setType(ActionType.SHOW)
        model.setDateTime(getGmtFromDateTime(date))
        return model
      }
    }
    if (worker.hasNote(keyStr)) {
      return getNote(keyStr)
    }
    if (worker.hasGroup(keyStr)) {
      keyStr = worker.clearGroup(keyStr)
      return getGroup(keyStr)
    }
    if (worker.hasEvent(keyStr)) {
      val model = getEvent(keyStr)
      if (model != null) {
        return model
      }
    }
    if (worker.hasAction(keyStr)) {
      return getAction(keyStr)
    }
    if (worker.hasEmptyTrash(keyStr)) {
      return emptyTrash
    }
    if (worker.hasDisableReminders(keyStr)) {
      return disableAction
    }
    if (worker.hasAnswer(keyStr)) {
      return getAnswer(keyStr)
    }
    var type = Action.DATE
    var number: String? = null
    var hasAction = false
    if (keyStr.hasCall()) {
      keyStr = worker!!.clearCall(keyStr)
      hasAction = true
      type = Action.CALL
    }
    if (keyStr.hasSender()) {
      keyStr = worker!!.clearSender(keyStr!!)
      val actionType: Action = worker.getMessageType(keyStr)
      if (actionType != null) {
        hasAction = true
        keyStr = worker!!.clearMessageType(keyStr!!)
        type = actionType
      }
    }
    var repeating: Boolean
    var isEveryDay = false
    var hasWeekday = false
    var weekdays: List<Int> = ArrayList()
    var repeat: Long = 0
    if (keyStr.hasRepeat().also { repeating = it }) {
      isEveryDay = keyStr.hasEveryDay()
      keyStr = worker!!.clearRepeat(keyStr!!)
      println("parse: has repeat -> $keyStr, isEvery $isEveryDay")
      repeat = worker!!.getDaysRepeat(keyStr!!)
      if (repeat != 0L) {
        keyStr = worker!!.clearDaysRepeat(keyStr)
      }
      if (isEveryDay) {
        weekdays.clear()
        for (i in 0..6) {
          weekdays.add(1)
        }
        type = if (type === Action.CALL) {
          Action.WEEK_CALL
        } else if (type === Action.MESSAGE) {
          Action.WEEK_SMS
        } else {
          Action.WEEK
        }
      }
    }
    var isCalendar: Boolean
    if (worker!!.hasCalendar(keyStr!!).also { isCalendar = it }) {
      keyStr = worker!!.clearCalendar(keyStr)
    }
    var today: Boolean
    if (worker!!.hasToday(keyStr!!).also { today = it }) {
      keyStr = worker!!.clearToday(keyStr)
    }
    var afterTomorrow: Boolean
    if (worker!!.hasAfterTomorrow(keyStr).also { afterTomorrow = it }) {
      keyStr = worker!!.clearAfterTomorrow(keyStr)
    }
    var tomorrow: Boolean
    if (worker!!.hasTomorrow(keyStr).also { tomorrow = it }) {
      keyStr = worker!!.clearTomorrow(keyStr)
    }
    val ampm: Ampm = worker.getAmpm(keyStr)
    if (ampm != null) {
      keyStr = worker!!.clearAmpm(keyStr!!)
    }
    if (!isEveryDay) {
      weekdays = worker!!.getWeekDays(keyStr!!)
      for (day in weekdays) {
        if (day == 1) {
          hasWeekday = true
          break
        }
      }
      keyStr = worker!!.clearWeekDays(keyStr)
      if (hasWeekday) {
        type = if (type === Action.CALL) {
          Action.WEEK_CALL
        } else if (type === Action.MESSAGE) {
          Action.WEEK_SMS
        } else {
          Action.WEEK
        }
      }
    }
    var hasTimer: Boolean
    val afterTime = LongInternal()
    if (keyStr.isTimer().also { hasTimer = it }) {
      keyStr = worker!!.cleanTimer(keyStr!!)
      keyStr = worker!!.getMultiplier(keyStr!!, afterTime)
    }
    System.out.println("parse: " + afterTime.get().toString() + ", input " + keyStr)
    val date = LongInternal()
    keyStr = worker!!.getDate(keyStr!!, date)
    println("parse: after date $keyStr")
    var time = worker!!.getTime(keyStr!!, ampm, times)
    if (time != 0L) {
      keyStr = worker!!.clearTime(keyStr)
    }
    println("parse: $keyStr, time $time, date $date")
    if (today) {
      println("parse: today")
      time = getTodayTime(time)
    } else if (afterTomorrow) {
      println("parse: after tomorrow")
      time = getAfterTomorrowTime(time)
    } else if (tomorrow) {
      println("parse: tomorrow")
      time = getTomorrowTime(time)
    } else if (isEveryDay) {
      println("parse: everyday")
      time = getDayTime(time, weekdays)
    } else if (hasWeekday && !repeating) {
      println("parse: on weekday")
      time = getDayTime(time, weekdays)
    } else if (repeating) {
      println("parse: repeating")
      time = getRepeatingTime(time, hasWeekday)
    } else if (hasTimer) {
      println("parse: timer")
      time = System.currentTimeMillis() + afterTime.get()
    } else if (date.get() !== 0 || time != 0L) {
      println("parse: date/time")
      time = getDateTime(date.get(), time)
    } else {
      return null
    }
    var message: String? = null
    if (hasAction && (type === Action.MESSAGE || type === Action.MAIL)) {
      message = worker!!.getMessage(keyStr)
      keyStr = worker!!.clearMessage(keyStr)
      if (message != null) {
        keyStr = keyStr!!.replace(message, "")
      }
    }
    if (hasAction && contactsInterface != null) {
      var output: ContactOutput? = contactsInterface!!.findNumber(keyStr) ?: return null
      keyStr = output.output
      number = output.number
      if (type === Action.MAIL) {
        output = contactsInterface!!.findEmail(keyStr)
        number = output!!.number
        keyStr = output.output
      }
      if (number == null) {
        return null
      }
    }
    var task = StringUtils.capitalize(StringUtils.normalizeSpace(keyStr))
    if (hasAction) {
      task = StringUtils.capitalize(message)
      if ((type === Action.MESSAGE || type === Action.MAIL) && task == null) {
        return null
      }
    }
    return Model(
      type = ActionType.REMINDER,
      summary = task,
      dateTime = getGmtFromDateTime(LongInternal(time)),
      weekdays = weekdays,
      repeatInterval = repeat,
      target = number,
      isHasCalendar = isCalendar,
      action = type
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
    var date = date
    var time = time
    if (date == 0L) {
      date = System.currentTimeMillis()
    }
    if (time == 0L) {
      time = date
    }
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = time
    val hour = calendar[Calendar.HOUR_OF_DAY]
    val minute = calendar[Calendar.MINUTE]
    calendar.timeInMillis = date
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
    var time = time
    val calendar = Calendar.getInstance()
    if (time == 0L) {
      time = System.currentTimeMillis()
    }
    calendar.timeInMillis = time
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
    return calendar.timeInMillis
  }

  private fun getDayTime(time: Long, weekdays: List<Int>): Long {
    var time = time
    val calendar = Calendar.getInstance()
    if (time == 0L) {
      time = System.currentTimeMillis()
    }
    calendar.timeInMillis = time
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
    return calendar.timeInMillis
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
    return input?.let {
      StringUtils.capitalize(worker.clearNote(input))
    }?.let {
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

  internal companion object {
    var LOCALE = Locale.getDefault()
  }
}