package com.elementary.tasks.core.cloud

import androidx.core.content.edit
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.reminder.scheduling.usecase.ActivateReminderUseCase
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.sync.DataPostProcessor
import com.github.naz013.files.DataType
import com.github.naz013.files.model.SettingsModel

class DataPostProcessorImpl(
  private val groupV2Repository: GroupV2Repository,
  private val prefs: Prefs,
  private val activateReminderUseCase: ActivateReminderUseCase,
) : DataPostProcessor {
  override suspend fun process(
    dataType: DataType,
    any: Any,
  ) {
    when (any) {
      is ReminderV2 -> {
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

  private suspend fun postProcessReminder(reminder: ReminderV2) {
    val groups = groupV2Repository.getAll().associateBy { it.uuId }
    val defGroup = groupV2Repository.defaultGroup() ?: groups.values.first()

    val withGroup = if (reminder.groupId == null || !groups.containsKey(reminder.groupId)) {
      reminder.copy(groupId = defGroup.uuId)
    } else {
      reminder
    }
    val fixed = if (withGroup.isRemoved) withGroup.copy(isActive = false) else withGroup
    activateReminderUseCase(fixed)
    Logger.i(TAG, "Post processed reminder with id = ${fixed.uuId}")
  }

  companion object {
    private const val TAG = "DataPostProcessorImpl"
  }
}
