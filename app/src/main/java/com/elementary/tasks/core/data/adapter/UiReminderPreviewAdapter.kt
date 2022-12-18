package com.elementary.tasks.core.data.adapter

import android.media.RingtoneManager
import androidx.annotation.DrawableRes
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.UiAppTarget
import com.elementary.tasks.core.data.ui.UiCallTarget
import com.elementary.tasks.core.data.ui.UiEmailTarget
import com.elementary.tasks.core.data.ui.UiGroup
import com.elementary.tasks.core.data.ui.UiLinkTarget
import com.elementary.tasks.core.data.ui.UiReminderDueData
import com.elementary.tasks.core.data.ui.UiReminderIllustration
import com.elementary.tasks.core.data.ui.UiReminderPreview
import com.elementary.tasks.core.data.ui.UiReminderStatus
import com.elementary.tasks.core.data.ui.UiReminderTarget
import com.elementary.tasks.core.data.ui.UiReminderType
import com.elementary.tasks.core.data.ui.UiSmsTarget
import com.elementary.tasks.core.utils.IntervalUtil
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.utils.Sound
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.TimeUtil
import java.io.File

class UiReminderPreviewAdapter(
  private val textProvider: TextProvider,
  private val prefs: Prefs,
  private val uiReminderPlaceAdapter: UiReminderPlaceAdapter
) : UiAdapter<Reminder, UiReminderPreview> {

  override fun create(data: Reminder): UiReminderPreview {
    val type = UiReminderType(data.type)
    val actionTarget: UiReminderTarget? =
      if (data.isActive && !data.isRemoved && data.target.isNotEmpty()) {
        when {
          type.isSms() -> UiSmsTarget(
            data.summary,
            data.target
          ).takeIf { data.summary.isNotEmpty() }
          type.isCall() -> UiCallTarget(data.summary)
          type.isApp() -> UiAppTarget(data.target)
          type.isLink() -> UiLinkTarget(data.target)
          type.isEmail() -> UiEmailTarget(
            data.summary,
            data.target,
            data.subject,
            data.attachmentFile
          )
          else -> null
        }
      } else {
        null
      }
    return UiReminderPreview(
      id = data.uuId,
      group = UiGroup(data.groupUuId, data.groupColor, data.groupTitle),
      noteId = data.noteId,
      type = type,
      actionTarget = actionTarget,
      summary = data.summary,
      isRunning = data.isActive && !data.isRemoved,
      attachmentFile = data.attachmentFile.takeIf { it.isNotEmpty() },
      windowType = getWindowType(data.windowType),
      status = getReminderStatus(data.isActive, data.isRemoved),
      illustration = UiReminderIllustration(
        title = getTypeString(type),
        icon = getReminderIllustration(type)
      ),
      melodyName = getMelodyName(data.melodyPath),
      due = getDue(data, type),
      shopList = data.shoppings,
      places = data.places.map { uiReminderPlaceAdapter.create(it) }
    )
  }

  private fun getDue(data: Reminder, type: UiReminderType): UiReminderDueData {
    val before = if (data.remindBefore == 0L) {
      null
    } else {
      IntervalUtil.getBeforeTime(data.remindBefore) { getBeforePattern(it) }
    }
    val due = TimeUtil.getDateTimeFromGmt(data.eventTime).takeIf { it > 0L }?.let {
      TimeUtil.getFullDateTime(it, prefs.is24HourFormat, prefs.appLanguage)
    }
    val repeatValue = when {
      type.isBase(UiReminderType.Base.MONTHLY) ->
        String.format(textProvider.getText(R.string.xM), data.repeatInterval.toString())
      type.isBase(UiReminderType.Base.WEEKDAY) -> getRepeatString(data.weekdays)
      type.isBase(UiReminderType.Base.YEARLY) -> textProvider.getText(R.string.yearly)
      else -> IntervalUtil.getInterval(data.repeatInterval) { getIntervalPattern(it) }
    }
    return UiReminderDueData(
      before = before,
      repeat = repeatValue,
      dateTime = due
    )
  }

  private fun getMelodyName(melodyPath: String): String? {
    var file: File? = null
    if (melodyPath.isNotEmpty()) {
      file = File(melodyPath)
    } else {
      val path = prefs.melodyFile
      if (path != "" && !Sound.isDefaultMelody(path)) {
        file = File(path)
      } else {
        val soundPath = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)?.path
        if (soundPath != null) {
          file = File(soundPath)
        }
      }
    }
    return file?.name
  }

  @DrawableRes
  private fun getReminderIllustration(type: UiReminderType): Int {
    return when {
      type.isCall() -> R.drawable.ic_phone_call
      type.isSms() -> R.drawable.ic_chat
      type.isLink() -> R.drawable.ic_browser
      type.isApp() -> R.drawable.ic_gamepad
      type.isEmail() -> R.drawable.ic_email_illustration
      type.isShopping() -> R.drawable.ic_shopping_cart
      type.isBase(UiReminderType.Base.LOCATION_IN) -> R.drawable.ic_location_illustration
      type.isBase(UiReminderType.Base.LOCATION_OUT) -> R.drawable.ic_radar
      type.isBase(UiReminderType.Base.PLACE) -> R.drawable.ic_placeholder
      type.isBase(UiReminderType.Base.DATE) -> R.drawable.ic_calendar_illustration
      type.isBase(UiReminderType.Base.WEEKDAY) -> R.drawable.ic_alarm_clock
      type.isBase(UiReminderType.Base.MONTHLY) -> R.drawable.ic_seventeen
      type.isBase(UiReminderType.Base.TIMER) -> R.drawable.ic_stopwatch
      type.isBase(UiReminderType.Base.YEARLY) -> R.drawable.ic_balloons
      else -> R.drawable.ic_bell_illustration
    }
  }

  private fun getTypeString(type: UiReminderType): String {
    return when {
      type.isCall() -> textProvider.getText(R.string.make_call)
      type.isSms() -> textProvider.getText(R.string.message)
      type.isApp() -> textProvider.getText(R.string.application)
      type.isLink() -> textProvider.getText(R.string.open_link)
      type.isShopping() -> textProvider.getText(R.string.shopping_list)
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
      else -> textProvider.getText(R.string.by_date)
    }
  }

  private fun getReminderStatus(isActive: Boolean, isRemoved: Boolean): UiReminderStatus {
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

  private fun getWindowType(reminderWindowType: Int): String {
    if (Module.is10) return textProvider.getText(R.string.simple)
    var windowType = prefs.reminderType
    val ignore = prefs.isIgnoreWindowType
    if (!ignore) {
      windowType = reminderWindowType
    }
    return if (windowType == 0) {
      textProvider.getText(R.string.full_screen)
    } else {
      textProvider.getText(R.string.simple)
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

  private fun getRepeatString(repCode: List<Int>): String {
    val sb = StringBuilder()
    val first = prefs.startDay
    if (first == 0 && repCode[0] == ReminderUtils.DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.sun))
    }
    if (repCode[1] == ReminderUtils.DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.mon))
    }
    if (repCode[2] == ReminderUtils.DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.tue))
    }
    if (repCode[3] == ReminderUtils.DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.wed))
    }
    if (repCode[4] == ReminderUtils.DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.thu))
    }
    if (repCode[5] == ReminderUtils.DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.fri))
    }
    if (repCode[6] == ReminderUtils.DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.sat))
    }
    if (first == 1 && repCode[0] == ReminderUtils.DAY_CHECKED) {
      sb.append(" ")
      sb.append(textProvider.getText(R.string.sun))
    }
    return if (ReminderUtils.isAllChecked(repCode)) {
      textProvider.getText(R.string.everyday)
    } else {
      sb.toString().trim()
    }
  }
}