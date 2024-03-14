package com.elementary.tasks.core.data.adapter

import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.reminder.UiAppTarget
import com.elementary.tasks.core.data.ui.reminder.UiCallTarget
import com.elementary.tasks.core.data.ui.reminder.UiEmailTarget
import com.elementary.tasks.core.data.ui.reminder.UiLinkTarget
import com.elementary.tasks.core.data.ui.reminder.UiReminderDueData
import com.elementary.tasks.core.data.ui.reminder.UiReminderStatus
import com.elementary.tasks.core.data.ui.reminder.UiReminderTarget
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.data.ui.reminder.UiSmsTarget
import com.elementary.tasks.core.os.PackageManagerWrapper
import com.elementary.tasks.core.os.contacts.ContactsReader
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.datetime.IntervalUtil
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceManager
import com.elementary.tasks.core.utils.datetime.recurrence.TagType
import com.elementary.tasks.core.utils.params.Prefs

class UiReminderCommonAdapter(
  private val textProvider: TextProvider,
  private val prefs: Prefs,
  private val dateTimeManager: DateTimeManager,
  private val contactsReader: ContactsReader,
  private val packageManagerWrapper: PackageManagerWrapper,
  private val recurrenceManager: RecurrenceManager
) {

  fun getPriorityTitle(priority: Int): String {
    return when (priority) {
      0 -> textProvider.getText(R.string.priority_lowest)
      1 -> textProvider.getText(R.string.priority_low)
      2 -> textProvider.getText(R.string.priority_normal)
      3 -> textProvider.getText(R.string.priority_high)
      4 -> textProvider.getText(R.string.priority_highest)
      else -> textProvider.getText(R.string.priority_normal)
    }
  }

  fun getTarget(reminder: Reminder, type: UiReminderType): UiReminderTarget? {
    val actionTarget: UiReminderTarget? =
      if (reminder.isActive && !reminder.isRemoved && reminder.target.isNotEmpty()) {
        when {
          type.isSms() -> UiSmsTarget(
            reminder.summary,
            reminder.target,
            contactsReader.getNameFromNumber(reminder.target)
          ).takeIf { reminder.summary.isNotEmpty() }

          type.isCall() -> UiCallTarget(
            reminder.summary,
            contactsReader.getNameFromNumber(reminder.target)
          )

          type.isApp() -> UiAppTarget(
            reminder.target,
            packageManagerWrapper.getApplicationName(reminder.target)
          )
          type.isLink() -> UiLinkTarget(reminder.target)
          type.isEmail() -> UiEmailTarget(
            reminder.summary,
            reminder.target,
            reminder.subject,
            reminder.attachmentFile,
            contactsReader.getNameFromMail(reminder.target)
          )

          else -> null
        }
      } else {
        null
      }
    return actionTarget
  }

  fun getDue(data: Reminder, type: UiReminderType): UiReminderDueData {
    val before = if (data.remindBefore == 0L) {
      null
    } else {
      IntervalUtil.getBeforeTime(data.remindBefore) { getBeforePattern(it) }
    }
    val dateTime = dateTimeManager.fromGmtToLocal(data.eventTime)
    val dueMillis = dateTimeManager.toMillis(data.eventTime)
    val due = dateTime?.let { dateTimeManager.getFullDateTime(it) }
    val repeatValue = when {
      type.isBase(UiReminderType.Base.MONTHLY) ->
        String.format(textProvider.getText(R.string.xM), data.repeatInterval.toString())

      type.isBase(UiReminderType.Base.WEEKDAY) -> dateTimeManager.getRepeatString(data.weekdays)
      type.isBase(UiReminderType.Base.YEARLY) -> textProvider.getText(R.string.yearly)
      type.isBase(UiReminderType.Base.RECUR) -> textProvider.getText(R.string.recur_custom)
      else -> IntervalUtil.getInterval(data.repeatInterval) { getIntervalPattern(it) }
    }
    return UiReminderDueData(
      before = before,
      repeat = repeatValue,
      dateTime = due,
      remaining = getRemaining(data),
      millis = dueMillis,
      localDateTime = dateTime,
      recurRule = getRecurRules(data, type),
      formattedTime = dateTime?.let { dateTimeManager.getTime(it.toLocalTime()) },
      formattedDateTime = dateTime?.let { dateTimeManager.getFullDateTime(it) }
    )
  }

  private fun getRecurRules(reminder: Reminder, type: UiReminderType): String? {
    return if (type.isRecur()) {
      runCatching { recurrenceManager.parseObject(reminder.recurDataObject) }
        .getOrNull()
        ?.map?.values?.firstOrNull { it.tagType == TagType.RRULE }
        ?.buildString()
    } else {
      null
    }
  }

  private fun getRemaining(reminder: Reminder): String {
    return dateTimeManager.getRemaining(reminder.eventTime, reminder.delay)
  }

  fun getTypeString(type: UiReminderType): String {
    return when {
      type.isCall() -> textProvider.getText(R.string.make_call)
      type.isSms() -> textProvider.getText(R.string.message)
      type.isApp() -> textProvider.getText(R.string.application)
      type.isLink() -> textProvider.getText(R.string.open_link)
      type.isSubTasks() -> textProvider.getText(R.string.builder_sub_tasks)
      type.isEmail() -> textProvider.getText(R.string.e_mail)
      else -> getType(type)
    }
  }

  private fun getType(type: UiReminderType): String {
    return when {
      type.isBase(UiReminderType.Base.MONTHLY) -> textProvider.getText(R.string.day_of_month)
      type.isBase(UiReminderType.Base.WEEKDAY) -> textProvider.getText(R.string.alarm)
      type.isBase(UiReminderType.Base.LOCATION_IN) -> textProvider.getText(R.string.entering_place)
      type.isBase(UiReminderType.Base.LOCATION_OUT) -> textProvider.getText(R.string.leaving_place)
      type.isBase(UiReminderType.Base.TIMER) -> textProvider.getText(R.string.timer)
      type.isBase(UiReminderType.Base.PLACE) -> textProvider.getText(R.string.places)
      type.isBase(UiReminderType.Base.YEARLY) -> textProvider.getText(R.string.yearly)
      type.isBase(UiReminderType.Base.RECUR) -> textProvider.getText(R.string.recur_custom)
      else -> textProvider.getText(R.string.by_date)
    }
  }

  fun getReminderStatus(isActive: Boolean, isRemoved: Boolean): UiReminderStatus {
    return UiReminderStatus(
      title = getReminderStatusTitle(isActive, isRemoved),
      active = isActive,
      removed = isRemoved
    )
  }

  private fun getReminderStatusTitle(isActive: Boolean, isRemoved: Boolean): String {
    return when {
      isRemoved -> textProvider.getText(R.string.deleted)
      isActive -> textProvider.getText(R.string.enabled4)
      else -> textProvider.getText(R.string.disabled)
    }
  }

  private fun getIntervalPattern(type: IntervalUtil.PatternType): String {
    return when (type) {
      IntervalUtil.PatternType.SECONDS -> "0"
      IntervalUtil.PatternType.MINUTES -> textProvider.getText(R.string.x_min)
      IntervalUtil.PatternType.HOURS -> textProvider.getText(R.string.x_hours)
      IntervalUtil.PatternType.DAYS -> textProvider.getText(R.string.xD)
      IntervalUtil.PatternType.WEEKS -> textProvider.getText(R.string.xW)
    }
  }

  private fun getBeforePattern(type: IntervalUtil.PatternType): String {
    return when (type) {
      IntervalUtil.PatternType.SECONDS -> textProvider.getText(R.string.x_seconds)
      IntervalUtil.PatternType.MINUTES -> textProvider.getText(R.string.x_minutes)
      IntervalUtil.PatternType.HOURS -> textProvider.getText(R.string.x_hours)
      IntervalUtil.PatternType.DAYS -> textProvider.getText(R.string.x_days)
      IntervalUtil.PatternType.WEEKS -> textProvider.getText(R.string.x_weeks)
    }
  }
}
