package com.github.naz013.feature.reminder

import com.github.naz013.ui.common.R
import com.github.naz013.ui.reminder.UiAppTarget
import com.github.naz013.ui.reminder.UiCallTarget
import com.github.naz013.ui.reminder.UiEmailTarget
import com.github.naz013.ui.reminder.UiLinkTarget
import com.github.naz013.ui.reminder.UiReminderDueData
import com.github.naz013.ui.reminder.UiReminderStatus
import com.github.naz013.ui.reminder.UiReminderTarget
import com.github.naz013.ui.reminder.UiSmsTarget
import com.github.naz013.feature.reminder.util.IntervalUtil
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.common.TextProvider
import com.github.naz013.common.contacts.ContactsReader
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.icalendar.ICalendarApi
import com.github.naz013.icalendar.TagType
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import org.threeten.bp.LocalDateTime

internal class UiReminderCommonAdapter(
  private val textProvider: TextProvider,
  private val dateTimeManager: DateTimeManager,
  private val contactsReader: ContactsReader,
  private val packageManagerWrapper: PackageManagerWrapper,
  private val iCalendarApi: ICalendarApi,
  private val reminderPreferences: ReminderPreferences,
  private val modelDateTimeFormatter: ModelDateTimeFormatter,
) {
  fun getPriorityTitle(priority: Int): String =
    when (priority) {
      0 -> textProvider.getText(R.string.priority_lowest)
      1 -> textProvider.getText(R.string.priority_low)
      2 -> textProvider.getText(R.string.priority_normal)
      3 -> textProvider.getText(R.string.priority_high)
      4 -> textProvider.getText(R.string.priority_highest)
      else -> textProvider.getText(R.string.priority_normal)
    }

  fun getTargetV2(reminder: ReminderV2): UiReminderTarget? {
    if (!reminder.isActive || reminder.isRemoved) return null
    return when (val action = reminder.action) {
      is ReminderAction.Sms ->
        UiSmsTarget(
          reminder.summary,
          action.target,
          contactsReader.getNameFromNumber(action.target),
        ).takeIf { reminder.summary.isNotEmpty() }

      is ReminderAction.Call ->
        UiCallTarget(
          reminder.summary,
          contactsReader.getNameFromNumber(action.target),
        )

      is ReminderAction.Link -> UiLinkTarget(action.target)

      is ReminderAction.App ->
        UiAppTarget(
          action.target,
          packageManagerWrapper.getApplicationName(action.target),
        )

      is ReminderAction.Email ->
        UiEmailTarget(
          reminder.summary,
          action.target,
          action.subject,
          "",
          contactsReader.getNameFromMail(action.target),
        )

      ReminderAction.Shopping, ReminderAction.None -> null
    }
  }

  fun getDueV2(reminder: ReminderV2): UiReminderDueData {
    val remindBefore = reminder.notification.remindBefore
    val before =
      if (remindBefore == null || remindBefore == 0L) {
        null
      } else {
        IntervalUtil.getBeforeTime(remindBefore) { getBeforePattern(it) }
      }
    val dateTime = reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) }
    val dueMillis = dateTime?.let { dateTimeManager.toMillis(it) } ?: 0L
    val due = dateTime?.let { dateTimeManager.getFullDateTime(it) }
    return UiReminderDueData(
      before = before,
      repeat = getRepeatValueV2(reminder.recurrence),
      dateTime = due,
      remaining = getRemainingV2(dateTime),
      millis = dueMillis,
      localDateTime = dateTime,
      recurRule = getRecurRulesV2(reminder.recurrence),
      formattedTime = dateTime?.let { dateTimeManager.getTime(it.toLocalTime()) },
      formattedDateTime = due,
    )
  }

  private fun getRepeatValueV2(recurrence: RecurrenceRule): String =
    when (recurrence) {
      is RecurrenceRule.Monthly ->
        String.format(textProvider.getText(R.string.xM), recurrence.repeatInterval.toString())

      is RecurrenceRule.RelativeMonthly ->
        String.format(textProvider.getText(R.string.xM), recurrence.repeatInterval.toString())

      is RecurrenceRule.Weekly -> getRepeatString(recurrence.weekdays)
      is RecurrenceRule.Yearly -> textProvider.getText(R.string.yearly)
      is RecurrenceRule.ICalendar -> textProvider.getText(R.string.recur_custom)
      is RecurrenceRule.Daily -> {
        IntervalUtil.getInterval(recurrence.repeatInterval) { getIntervalPattern(it) }
          ?: textProvider.getText(R.string.repeat_once)
      }

      is RecurrenceRule.Countdown -> {
        IntervalUtil.getInterval(recurrence.repeatInterval) { getIntervalPattern(it) }
          ?: textProvider.getText(R.string.repeat_once)
      }

      RecurrenceRule.Once,
      RecurrenceRule.LocationEnter,
      RecurrenceRule.LocationExit,
      -> textProvider.getText(R.string.repeat_once)
    }

  private fun getRecurRulesV2(recurrence: RecurrenceRule): String? =
    (recurrence as? RecurrenceRule.ICalendar)?.let { rule ->
      runCatching { iCalendarApi.parseObject(rule.rrule) }
        .getOrNull()
        ?.map
        ?.values
        ?.firstOrNull { it.tagType == TagType.RRULE }
        ?.buildString()
    }

  private fun getRemainingV2(localEventDateTime: LocalDateTime?): String =
    modelDateTimeFormatter.getRemaining(localEventDateTime, dateTimeManager.getCurrentDateTime())

  fun getReminderStatus(
    isActive: Boolean,
    isRemoved: Boolean,
  ): UiReminderStatus =
    UiReminderStatus(
      title = getReminderStatusTitle(isActive, isRemoved),
      active = isActive,
      removed = isRemoved,
    )

  private fun getReminderStatusTitle(
    isActive: Boolean,
    isRemoved: Boolean,
  ): String =
    when {
      isRemoved -> textProvider.getText(R.string.deleted)
      isActive -> textProvider.getText(R.string.enabled4)
      else -> textProvider.getText(R.string.disabled)
    }

  private fun getIntervalPattern(type: IntervalUtil.PatternType): String =
    when (type) {
      IntervalUtil.PatternType.SECONDS -> ""
      IntervalUtil.PatternType.MINUTES -> textProvider.getText(R.string.x_min)
      IntervalUtil.PatternType.HOURS -> textProvider.getText(R.string.x_hours)
      IntervalUtil.PatternType.DAYS -> textProvider.getText(R.string.xD)
      IntervalUtil.PatternType.WEEKS -> textProvider.getText(R.string.xW)
    }

  private fun getBeforePattern(type: IntervalUtil.PatternType): String =
    when (type) {
      IntervalUtil.PatternType.SECONDS -> textProvider.getText(R.string.x_seconds)
      IntervalUtil.PatternType.MINUTES -> textProvider.getText(R.string.x_minutes)
      IntervalUtil.PatternType.HOURS -> textProvider.getText(R.string.x_hours)
      IntervalUtil.PatternType.DAYS -> textProvider.getText(R.string.x_days)
      IntervalUtil.PatternType.WEEKS -> textProvider.getText(R.string.x_weeks)
    }

  private fun getRepeatString(repCode: List<Int>): String {
    val sb = StringBuilder()
    val first = reminderPreferences.startDay
    if (first == 0 && repCode[0] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.sun))
    }
    if (repCode[1] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.mon))
    }
    if (repCode[2] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.tue))
    }
    if (repCode[3] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.wed))
    }
    if (repCode[4] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.thu))
    }
    if (repCode[5] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.fri))
    }
    if (repCode[6] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.sat))
    }
    if (first == 1 && repCode[0] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.sun))
    }
    return if (isAllChecked(repCode)) {
      textProvider.getText(R.string.everyday)
    } else {
      sb.toString().trim()
    }
  }

  private fun isAllChecked(repCode: List<Int>): Boolean = repCode.none { it == 0 }

  companion object {
    private const val DAY_CHECKED = 1
  }
}
