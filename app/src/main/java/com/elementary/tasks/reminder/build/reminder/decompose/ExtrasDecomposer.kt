package com.elementary.tasks.reminder.build.reminder.decompose

import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.reminder.build.AttachmentsBuilderItem
import com.elementary.tasks.reminder.build.BeforeTimeBuilderItem
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.DescriptionBuilderItem
import com.elementary.tasks.reminder.build.EmailSubjectBuilderItem
import com.elementary.tasks.reminder.build.GoogleCalendarBuilderItem
import com.elementary.tasks.reminder.build.GoogleCalendarDurationBuilderItem
import com.elementary.tasks.reminder.build.GoogleTaskListBuilderItem
import com.elementary.tasks.reminder.build.LedColorBuilderItem
import com.elementary.tasks.reminder.build.OtherParamsBuilderItem
import com.elementary.tasks.reminder.build.PriorityBuilderItem
import com.elementary.tasks.reminder.build.RepeatLimitBuilderItem
import com.elementary.tasks.reminder.build.SummaryBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.elementary.tasks.reminder.build.bi.BiType
import com.elementary.tasks.reminder.build.bi.CalendarDuration
import com.elementary.tasks.reminder.build.bi.OtherParams

class ExtrasDecomposer(
  private val biFactory: BiFactory,
  private val googleTaskListsDao: GoogleTaskListsDao,
  private val googleCalendarUtils: GoogleCalendarUtils
) {

  operator fun invoke(reminder: Reminder): List<BuilderItem<*>> {
    val summary = reminder.summary.takeIf { it.isNotBlank() }
      ?.let { biFactory.createWithValue(BiType.SUMMARY, it, SummaryBuilderItem::class.java) }

    val description = reminder.description?.takeIf { it.isNotBlank() }
      ?.let {
        biFactory.createWithValue(
          BiType.DESCRIPTION,
          it,
          DescriptionBuilderItem::class.java
        )
      }

    val beforeTime = reminder.remindBefore.takeIf { it > 0 }
      ?.let { biFactory.createWithValue(BiType.BEFORE_TIME, it, BeforeTimeBuilderItem::class.java) }

    val repeatLimit = reminder.repeatLimit.takeIf { it > 0 }
      ?.let {
        biFactory.createWithValue(BiType.REPEAT_LIMIT, it, RepeatLimitBuilderItem::class.java)
      }

    val priority = reminder.priority.let {
      biFactory.createWithValue(BiType.PRIORITY, it, PriorityBuilderItem::class.java)
    }

    val ledColor = reminder.color.takeIf { Module.isPro }?.let {
      biFactory.createWithValue(BiType.LED_COLOR, it, LedColorBuilderItem::class.java)
    }

    val attachments = reminder.attachmentFiles.ifEmpty {
      reminder.attachmentFile.takeIf { it.isNotEmpty() }?.let { listOf(it) }
    }?.takeIf { it.isNotEmpty() }?.let {
      biFactory.createWithValue(BiType.ATTACHMENTS, it, AttachmentsBuilderItem::class.java)
    }

    val googleTaskList = reminder.taskListId.takeIf { !it.isNullOrEmpty() }
      ?.let { googleTaskListsDao.getById(it) }
      ?.let {
        biFactory.createWithValue(
          BiType.GOOGLE_TASK_LIST,
          it,
          GoogleTaskListBuilderItem::class.java
        )
      }

    val googleCalendar = reminder.calendarId.takeIf { it > 0 }?.let { calendarId ->
      googleCalendarUtils.getCalendarsList().firstOrNull { it.id == calendarId }
    }?.let {
      biFactory.createWithValue(BiType.GOOGLE_CALENDAR, it, GoogleCalendarBuilderItem::class.java)
    }

    val googleCalendarDuration = reminder.takeIf { it.duration > 0 || it.allDay }
      ?.let { CalendarDuration(it.allDay, it.duration) }
      ?.let {
        biFactory.createWithValue(
          BiType.GOOGLE_CALENDAR_DURATION,
          it,
          GoogleCalendarDurationBuilderItem::class.java
        )
      }

    val emailSubject = reminder.subject.takeIf { it.isNotEmpty() }
      ?.let {
        biFactory.createWithValue(
          BiType.EMAIL_SUBJECT,
          it,
          EmailSubjectBuilderItem::class.java
        )
      }

    val otherParams = reminder.takeIf {
      it.vibrate || it.notifyByVoice || it.repeatNotification || it.unlock
    }?.let {
      OtherParams(
        useGlobal = false,
        notifyByVoice = it.notifyByVoice,
        vibrate = it.vibrate,
        repeatNotification = it.repeatNotification
      )
    }?.let {
      biFactory.createWithValue(
        BiType.OTHER_PARAMS,
        it,
        OtherParamsBuilderItem::class.java
      )
    }

    return listOfNotNull(
      summary,
      description,
      beforeTime,
      repeatLimit,
      priority,
      ledColor,
      attachments,
      googleTaskList,
      googleCalendar,
      googleCalendarDuration,
      emailSubject,
      otherParams
    )
  }
}
