package com.elementary.tasks.core.cloud

import androidx.core.content.edit
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.logic.reminder.usecase.ActivateReminderUseCase
import com.github.naz013.domain.TagAssignment
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.sync.DataPostProcessor
import com.github.naz013.files.DataType
import com.github.naz013.files.model.SettingsModel
import com.github.naz013.files.model.TagAssignmentsSnapshotJson

class DataPostProcessorImpl(
  private val groupV2Repository: GroupV2Repository,
  private val prefs: Prefs,
  private val activateReminderUseCase: ActivateReminderUseCase,
  private val tagAssignmentRepository: TagAssignmentRepository,
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

      is TagAssignmentsSnapshotJson -> {
        postProcessTagAssignments(any)
      }

      else -> {
        // No op
      }
    }
  }

  private suspend fun postProcessTagAssignments(snapshot: TagAssignmentsSnapshotJson) {
    try {
      // A restore of the full tag<->item graph, not a merge - a row's absence is meaningful
      // (it means "not tagged"), so replacing wholesale is required for a detach on one device
      // to ever propagate to another.
      tagAssignmentRepository.replaceAll(
        snapshot.assignments.map {
          TagAssignment(tagId = it.tagId, itemId = it.itemId, itemType = TaggedItemType.valueOf(it.itemType))
        }
      )
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to post process tag assignments: $e")
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

    val withGroup = if (reminder.groupId != null && !groups.containsKey(reminder.groupId)) {
      reminder.copy(groupId = null)
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
