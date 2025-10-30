package com.elementary.tasks.core.cloud

import androidx.core.content.edit
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.groups.GroupsUtil
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.sync.DataPostProcessor
import com.github.naz013.sync.DataType
import com.github.naz013.sync.settings.SettingsModel

class DataPostProcessorImpl(
  private val reminderGroupRepository: ReminderGroupRepository,
  private val reminderRepository: ReminderRepository,
  private val eventControlFactory: EventControlFactory,
  private val groupsUtil: GroupsUtil,
  private val dateTimeManager: DateTimeManager,
  private val prefs: Prefs
) : DataPostProcessor {

  override suspend fun process(dataType: DataType, any: Any) {
    when (any) {
      is Reminder -> {
        postProcessReminder(any)
      }

      is SettingsModel -> {
        postProcessSettings(any)
      }

      else -> {
        // No op
      }
    }
  }

  private fun postProcessSettings(settingsModel: SettingsModel) {
    try {
      prefs.sharedPrefs().edit {
        val entries = settingsModel.data
        for ((key, v) in entries) {
          when (v) {
            is Boolean -> putBoolean(key, v)
            is Float -> putFloat(key, v)
            is Int -> putInt(key, v)
            is Long -> putLong(key, v)
            is String -> putString(key, v)
          }
        }
      }
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to post process settings: $e")
    }
  }

  private suspend fun postProcessReminder(reminder: Reminder) {
    val groups = groupsUtil.mapAll()
    val defGroup = reminderGroupRepository.defaultGroup() ?: groups.values.first()

    if (!groups.containsKey(reminder.groupUuId)) {
      reminder.apply {
        this.groupTitle = defGroup.groupTitle
        this.groupUuId = defGroup.groupUuId
        this.groupColor = defGroup.groupColor
      }
    }
    if (!reminder.isActive || reminder.isRemoved) {
      reminder.isActive = false
    }
    if (!Reminder.isGpsType(reminder.type) && !dateTimeManager.isCurrent(reminder.eventTime)) {
      if (!Reminder.isSame(reminder.type, Reminder.BY_DATE_SHOP) || reminder.hasReminder) {
        reminder.isActive = false
      }
    }
    reminderRepository.save(reminder)
    if (reminder.isActive && !reminder.isRemoved) {
      val control = eventControlFactory.getController(reminder)
      if (control.canSkip()) {
        control.next()
      } else {
        control.enable()
      }
    }

    Logger.i(TAG, "Post processed reminder with id = ${reminder.uuId}")
  }

  companion object {
    private const val TAG = "DataPostProcessorImpl"
  }
}
