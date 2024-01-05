package com.elementary.tasks.reminder.build.reminder

import com.elementary.tasks.core.analytics.Traces
import com.elementary.tasks.core.data.models.BuilderSchemeItem
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.EmptyState
import com.elementary.tasks.reminder.build.ErrorState
import com.elementary.tasks.reminder.build.bi.ProcessedBuilderItems
import com.elementary.tasks.reminder.build.logic.builderstate.BuilderStateCalculator
import com.elementary.tasks.reminder.build.reminder.compose.DateTimeInjector
import com.elementary.tasks.reminder.build.reminder.compose.ReminderCleaner
import com.elementary.tasks.reminder.build.reminder.compose.TypeCalculator
import com.elementary.tasks.reminder.build.reminder.validation.ReminderValidator
import java.util.UUID

class BiToReminderAdapter(
  private val builderStateCalculator: BuilderStateCalculator,
  private val reminderValidator: ReminderValidator,
  private val typeCalculator: TypeCalculator,
  private val dateTimeInjector: DateTimeInjector,
  private val reminderCleaner: ReminderCleaner
) {

  operator fun invoke(
    reminder: Reminder,
    items: List<BuilderItem<*>>,
    newId: Boolean
  ): BuildResult {
    val processedBuilderItems = ProcessedBuilderItems(items)
    val itemsMap = processedBuilderItems.typeMap

    val type = typeCalculator(processedBuilderItems)
    Traces.d(TAG, "invoke: type=$type")

    val builderState = builderStateCalculator(type, itemsMap)
    Traces.d(TAG, "invoke: builderState=$builderState")

    if (builderState is EmptyState || builderState is ErrorState) {
      return BuildResult.Error("State is not valid")
    }

    reminder.type = type

    items.forEach {
      it.modifier.putInto(reminder)
    }

    reminderCleaner(reminder)
    dateTimeInjector(reminder, processedBuilderItems)

    when (val validationResult = reminderValidator(reminder)) {
      is ReminderValidator.ValidationResult.Failed -> {
        Traces.d(TAG, "invoke: reminder not valid cause = ${validationResult.error}")
        return BuildResult.Error("Reminder is not valid")
      }
      else -> {
      }
    }

    if (newId) {
      reminder.uuId = UUID.randomUUID().toString()
    }

    reminder.builderScheme = items.mapIndexed { index, builderItem ->
      BuilderSchemeItem(builderItem.biType, index)
    }
    reminder.version = Reminder.Version.V3

    Traces.d(TAG, "invoke: new reminder=$reminder")

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
