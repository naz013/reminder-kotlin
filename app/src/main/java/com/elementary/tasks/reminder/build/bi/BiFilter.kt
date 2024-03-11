package com.elementary.tasks.reminder.build.bi

import android.content.Context
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.reminder.build.BuilderItem

class BiFilter(
  private val locationFilter: LocationFilter,
  private val creatorConfigFilter: CreatorConfigFilter
) {

  operator fun invoke(item: BuilderItem<*>): Boolean {
    return item.isEnabled && (!item.isForPro || (item.isForPro && Module.isPro)) &&
      Module.currentSdk in item.minSdk..item.maxSdk && locationFilter(item) &&
      creatorConfigFilter(item)
  }
}

class CreatorConfigFilter(
  private val prefs: Prefs
) {

  operator fun invoke(item: BuilderItem<*>): Boolean {
    return !getDisabledTypes().contains(item.biType)
  }

  private fun getDisabledTypes(): List<BiType> {
    val config = prefs.reminderCreatorParams
    return (
      listOfNotNull(
        BiType.BEFORE_TIME.takeIf { !config.isBeforePickerEnabled() },
        BiType.REPEAT_TIME.takeIf { !config.isRepeatPickerEnabled() },
        BiType.REPEAT_LIMIT.takeIf { !config.isRepeatLimitPickerEnabled() },
        BiType.PRIORITY.takeIf { !config.isPriorityPickerEnabled() },
        BiType.MELODY.takeIf { !config.isMelodyPickerEnabled() },
        BiType.GOOGLE_CALENDAR.takeIf { !config.isCalendarPickerEnabled() },
        BiType.GOOGLE_CALENDAR_DURATION.takeIf { !config.isCalendarPickerEnabled() },
        BiType.GOOGLE_TASK_LIST.takeIf { !config.isGoogleTasksPickerEnabled() },
        BiType.OTHER_PARAMS.takeIf { !config.isTuneExtraPickerEnabled() },
        BiType.ATTACHMENTS.takeIf { !config.isAttachmentPickerEnabled() },
        BiType.LED_COLOR.takeIf { !config.isLedPickerEnabled() },
        BiType.WINDOW_TYPE.takeIf { !config.isWindowTypePickerEnabled() },
        BiType.PHONE_CALL.takeIf { !config.isPhoneCallEnabled() },
        BiType.SMS.takeIf { !config.isSendSmsEnabled() },
        BiType.EMAIL.takeIf { !config.isSendEmailEnabled() },
        BiType.EMAIL_SUBJECT.takeIf { !config.isSendEmailEnabled() },
        BiType.APPLICATION.takeIf { !config.isOpenAppEnabled() },
        BiType.LINK.takeIf { !config.isOpenLinkEnabled() }
      ) + (BiGroup.ICAL.types.takeIf { !config.isICalendarEnabled() } ?: emptyList())
      )
  }
}

class LocationFilter(context: Context) {

  private val hasLocation = Module.hasLocation(context)

  operator fun invoke(item: BuilderItem<*>): Boolean {
    return if (LOCATION_TYPES.contains(item.biType)) {
      hasLocation
    } else {
      true
    }
  }

  companion object {
    private val LOCATION_TYPES = listOf(
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES,
      BiType.LOCATION_DELAY_DATE,
      BiType.LOCATION_DELAY_TIME
    )
  }
}
