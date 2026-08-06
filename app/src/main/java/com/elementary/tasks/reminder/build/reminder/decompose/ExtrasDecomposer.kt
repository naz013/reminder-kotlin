package com.elementary.tasks.reminder.build.reminder.decompose

import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.reminder.build.AttachmentsBuilderItem
import com.elementary.tasks.reminder.build.BeforeTimeBuilderItem
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.BypassDndBuilderItem
import com.elementary.tasks.reminder.build.CategoryBuilderItem
import com.elementary.tasks.reminder.build.DelayMinutesBuilderItem
import com.elementary.tasks.reminder.build.DescriptionBuilderItem
import com.elementary.tasks.reminder.build.EmailSubjectBuilderItem
import com.elementary.tasks.reminder.build.GoogleCalendarBuilderItem
import com.elementary.tasks.reminder.build.GoogleCalendarDurationBuilderItem
import com.elementary.tasks.reminder.build.GoogleTaskListBuilderItem
import com.elementary.tasks.reminder.build.LedColorBuilderItem
import com.elementary.tasks.reminder.build.LockScreenVisibilityBuilderItem
import com.elementary.tasks.reminder.build.OtherParamsBuilderItem
import com.elementary.tasks.reminder.build.PriorityBuilderItem
import com.elementary.tasks.reminder.build.RepeatLimitBuilderItem
import com.elementary.tasks.reminder.build.SummaryBuilderItem
import com.elementary.tasks.reminder.build.VibrationPatternBuilderItem
import com.elementary.tasks.reminder.build.WakeScreenBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.elementary.tasks.reminder.build.bi.CalendarDuration
import com.elementary.tasks.reminder.build.bi.OtherParams
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.repeatLimitOrDefault
import com.github.naz013.repository.GoogleTaskListRepository

class ExtrasDecomposer(
  private val biFactory: BiFactory,
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val googleCalendarUtils: GoogleCalendarUtils,
) {
  suspend operator fun invoke(reminder: ReminderV2): List<BuilderItem<*>> {
    val notification = reminder.notification
    val calendarExport = reminder.calendarExport

    val summary =
      reminder.summary
        .takeIf { it.isNotBlank() }
        ?.let { biFactory.createWithValue(BiType.SUMMARY, it, SummaryBuilderItem::class.java) }

    val description =
      reminder.description
        ?.takeIf { it.isNotBlank() }
        ?.let {
          biFactory.createWithValue(
            BiType.DESCRIPTION,
            it,
            DescriptionBuilderItem::class.java,
          )
        }

    val beforeTime =
      notification.remindBefore
        ?.takeIf { it > 0 }
        ?.let { biFactory.createWithValue(BiType.BEFORE_TIME, it, BeforeTimeBuilderItem::class.java) }

    val repeatLimit =
      reminder.recurrence
        .repeatLimitOrDefault()
        .takeIf { it > 0 }
        ?.let {
          biFactory.createWithValue(BiType.REPEAT_LIMIT, it, RepeatLimitBuilderItem::class.java)
        }

    val priority =
      (notification.priority ?: ReminderPriority.NORMAL).ordinal.let {
        biFactory.createWithValue(BiType.PRIORITY, it, PriorityBuilderItem::class.java)
      }

    val ledColor =
      notification.color?.takeIf { BuildParams.isPro }?.let {
        biFactory.createWithValue(BiType.LED_COLOR, it, LedColorBuilderItem::class.java)
      }

    val attachments =
      reminder.attachmentFiles
        .takeIf { it.isNotEmpty() }
        ?.let {
          biFactory.createWithValue(BiType.ATTACHMENTS, it, AttachmentsBuilderItem::class.java)
        }

    val googleTaskList =
      reminder.taskExport?.taskListId
        ?.takeIf { it.isNotEmpty() }
        ?.let { googleTaskListRepository.getById(it) }
        ?.let {
          biFactory.createWithValue(
            BiType.GOOGLE_TASK_LIST,
            it,
            GoogleTaskListBuilderItem::class.java,
          )
        }

    val googleCalendar =
      calendarExport?.calendarId
        ?.takeIf { it > 0 }
        ?.let { calendarId ->
          googleCalendarUtils.getCalendarsList().firstOrNull { it.id == calendarId }
        }?.let {
          biFactory.createWithValue(BiType.GOOGLE_CALENDAR, it, GoogleCalendarBuilderItem::class.java)
        }

    val googleCalendarDuration =
      calendarExport
        ?.takeIf { it.duration > 0 || it.allDay }
        ?.let { CalendarDuration(it.allDay, it.duration) }
        ?.let {
          biFactory.createWithValue(
            BiType.GOOGLE_CALENDAR_DURATION,
            it,
            GoogleCalendarDurationBuilderItem::class.java,
          )
        }

    val emailSubject =
      reminder.action
        .subjectOrEmpty()
        .takeIf { it.isNotEmpty() }
        ?.let {
          biFactory.createWithValue(
            BiType.EMAIL_SUBJECT,
            it,
            EmailSubjectBuilderItem::class.java,
          )
        }

    val category =
      notification.category?.let {
        biFactory.createWithValue(BiType.CATEGORY, it.ordinal, CategoryBuilderItem::class.java)
      }

    val lockScreenVisibility =
      notification.lockScreenVisibility?.let {
        biFactory.createWithValue(
          BiType.LOCK_SCREEN_VISIBILITY,
          it.ordinal,
          LockScreenVisibilityBuilderItem::class.java,
        )
      }

    val bypassDnd =
      notification.bypassDoNotDisturb?.let {
        biFactory.createWithValue(BiType.BYPASS_DND, it, BypassDndBuilderItem::class.java)
      }

    val wakeScreen =
      notification.wakeScreen?.let {
        biFactory.createWithValue(BiType.WAKE_SCREEN, it, WakeScreenBuilderItem::class.java)
      }

    val vibrationPattern =
      notification.vibrationPattern
        ?.takeIf { it.isNotEmpty() }
        ?.let {
          biFactory.createWithValue(BiType.VIBRATION_PATTERN, it, VibrationPatternBuilderItem::class.java)
        }

    val delayMinutes =
      notification.delayMinutes
        ?.takeIf { it > 0 }
        ?.let {
          biFactory.createWithValue(BiType.DELAY_MINUTES, it, DelayMinutesBuilderItem::class.java)
        }

    val otherParams =
      reminder
        .takeIf { notification.vibrate == true || notification.repeatNotification == true }
        ?.let {
          OtherParams(
            useGlobal = false,
            notifyByVoice = false,
            vibrate = notification.vibrate == true,
            repeatNotification = notification.repeatNotification == true,
          )
        }?.let {
          biFactory.createWithValue(
            BiType.OTHER_PARAMS,
            it,
            OtherParamsBuilderItem::class.java,
          )
        }

    return listOfNotNull(
      summary,
      description,
      beforeTime,
      repeatLimit,
      priority,
      ledColor,
      category,
      lockScreenVisibility,
      bypassDnd,
      wakeScreen,
      vibrationPattern,
      delayMinutes,
      attachments,
      googleTaskList,
      googleCalendar,
      googleCalendarDuration,
      emailSubject,
      otherParams,
    )
  }

  private fun ReminderAction.subjectOrEmpty(): String = when (this) {
    is ReminderAction.Sms -> subject
    is ReminderAction.Email -> subject
    else -> ""
  }
}
