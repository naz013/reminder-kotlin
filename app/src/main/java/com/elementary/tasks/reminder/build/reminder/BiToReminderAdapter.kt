package com.elementary.tasks.reminder.build.reminder

import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.EmptyState
import com.elementary.tasks.reminder.build.ErrorState
import com.elementary.tasks.reminder.build.bi.ProcessedBuilderItems
import com.elementary.tasks.reminder.build.logic.builderstate.BuilderStateCalculator
import com.elementary.tasks.reminder.build.reminder.compose.CalendarExportCalculator
import com.elementary.tasks.reminder.build.reminder.compose.ReminderActionCalculator
import com.elementary.tasks.reminder.build.reminder.compose.RecurrenceRuleCalculator
import com.elementary.tasks.reminder.build.reminder.validation.ReminderValidator
import com.github.naz013.domain.reminder.v2.BuilderSchemeItemV2
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger

class BiToReminderAdapter(
  private val builderStateCalculator: BuilderStateCalculator,
  private val reminderValidator: ReminderValidator,
  private val recurrenceRuleCalculator: RecurrenceRuleCalculator,
  private val reminderActionCalculator: ReminderActionCalculator,
  private val calendarExportCalculator: CalendarExportCalculator,
) {
  operator fun invoke(
    reminder: ReminderV2,
    items: List<BuilderItem<*>>,
    isEdited: Boolean,
  ): BuildResult {
    val processedBuilderItems = ProcessedBuilderItems(items)

    val recurrence = recurrenceRuleCalculator(processedBuilderItems)
    val builderState = builderStateCalculator(recurrence)
    if (builderState is EmptyState || builderState is ErrorState) {
      return BuildResult.Error("State is not valid")
    }
    if (recurrence == null) {
      return BuildResult.Error("State is not valid")
    }

    var updated =
      resetToBlank(reminder).copy(
        recurrence = recurrence.rule,
        schedule = recurrence.schedule,
        places = recurrence.places,
        location = recurrence.location,
        action = reminderActionCalculator(processedBuilderItems.typeMap),
        calendarExport = calendarExportCalculator(processedBuilderItems.typeMap),
      )

    updated = items.fold(updated) { acc, item -> item.modifier.putInto(acc) }

    when (val validationResult = reminderValidator(updated)) {
      is ReminderValidator.ValidationResult.Failed -> {
        Logger.d(TAG, "Reminder not valid cause = ${validationResult.error}")
        return BuildResult.Error("Reminder is not valid")
      }
      else -> {
      }
    }

    updated =
      updated.copy(
        builderScheme = items.mapIndexed { index, builderItem -> BuilderSchemeItemV2(builderItem.biType.ordinal, index) },
      )

    Logger.d(TAG, "New reminder = $updated")
    return BuildResult.Success(updated)
  }

  private fun resetToBlank(reminder: ReminderV2): ReminderV2 =
    reminder.copy(
      summary = "",
      description = null,
      noteId = "",
      groupId = null,
      notification = NotificationSettingsOverride(),
      calendarExport = null,
      taskExport = null,
      location = null,
      attachmentFiles = emptyList(),
      places = emptyList(),
      shoppingItems = emptyList(),
    )

  sealed class BuildResult {
    data class Success(
      val reminderV2: ReminderV2,
    ) : BuildResult()

    data class Error(
      val error: String,
    ) : BuildResult()
  }

  companion object {
    private const val TAG = "BiToReminderAdapter"
  }
}
