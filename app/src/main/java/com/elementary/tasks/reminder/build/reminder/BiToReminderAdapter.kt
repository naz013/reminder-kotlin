package com.elementary.tasks.reminder.build.reminder

import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.EmptyState
import com.elementary.tasks.reminder.build.ErrorState
import com.elementary.tasks.reminder.build.bi.ProcessedBuilderItems
import com.elementary.tasks.reminder.build.logic.builderstate.BuilderStateCalculator
import com.elementary.tasks.reminder.build.reminder.compose.DateTimeInjector
import com.elementary.tasks.reminder.build.reminder.compose.EditedReminderDataCleaner
import com.elementary.tasks.reminder.build.reminder.compose.ReminderDateTimeCleaner
import com.elementary.tasks.reminder.build.reminder.compose.TypeCalculator
import com.elementary.tasks.reminder.build.reminder.validation.ReminderValidator
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.BuilderSchemeItem
import com.github.naz013.logging.Logger

class BiToReminderAdapter(
  private val builderStateCalculator: BuilderStateCalculator,
  private val reminderValidator: ReminderValidator,
  private val typeCalculator: TypeCalculator,
  private val dateTimeInjector: DateTimeInjector,
  private val reminderDateTimeCleaner: ReminderDateTimeCleaner,
  private val editedReminderDataCleaner: EditedReminderDataCleaner
) {

  operator fun invoke(
    reminder: Reminder,
    items: List<BuilderItem<*>>,
    isEdited: Boolean
  ): BuildResult {
    val processedBuilderItems = ProcessedBuilderItems(items)

    val type = typeCalculator(processedBuilderItems)
    val builderState = builderStateCalculator(type)
    if (builderState is EmptyState || builderState is ErrorState) {
      return BuildResult.Error("State is not valid")
    }

    if (isEdited) {
      editedReminderDataCleaner(reminder, processedBuilderItems)
    }

    reminder.type = type
    items.forEach {
      it.modifier.putInto(reminder)
    }

    reminderDateTimeCleaner(reminder)
    dateTimeInjector(reminder, processedBuilderItems)

    when (val validationResult = reminderValidator(reminder)) {
      is ReminderValidator.ValidationResult.Failed -> {
        Logger.d("Reminder not valid cause = ${validationResult.error}")
        return BuildResult.Error("Reminder is not valid")
      }
      else -> {
      }
    }

    reminder.builderScheme = items.mapIndexed { index, builderItem ->
      BuilderSchemeItem(builderItem.biType, index)
    }
    reminder.version = Reminder.Version.V3

    Logger.d(TAG, "New reminder = $reminder")
    return BuildResult.Success(reminder)
  }

  sealed class BuildResult {
    data class Success(val reminder: Reminder) : BuildResult()
    data class Error(val error: String) : BuildResult()
  }

  companion object {
    private const val TAG = "BiToReminderAdapter"
  }
}
