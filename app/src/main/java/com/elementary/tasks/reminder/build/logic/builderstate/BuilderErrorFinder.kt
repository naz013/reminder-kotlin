package com.elementary.tasks.reminder.build.logic.builderstate

import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.EmptyState
import com.elementary.tasks.reminder.build.ErrorState
import com.elementary.tasks.reminder.build.bi.BiGroup
import com.elementary.tasks.reminder.build.bi.ProcessedBuilderItems
import com.elementary.tasks.reminder.build.reminder.compose.ReminderActionCalculator
import com.elementary.tasks.reminder.build.reminder.compose.RecurrenceRuleCalculator
import com.elementary.tasks.reminder.build.reminder.validation.ReminderValidator
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger

class BuilderErrorFinder(
  private val builderStateCalculator: BuilderStateCalculator,
  private val reminderValidator: ReminderValidator,
  private val recurrenceRuleCalculator: RecurrenceRuleCalculator,
  private val reminderActionCalculator: ReminderActionCalculator,
) {
  operator fun invoke(
    reminder: ReminderV2,
    items: List<BuilderItem<*>>,
  ): BuilderError {
    val processedBuilderItems = ProcessedBuilderItems(items)

    val recurrence = recurrenceRuleCalculator(processedBuilderItems)

    val builderState = builderStateCalculator(recurrence)
    if (builderState is EmptyState || builderState is ErrorState || recurrence == null) {
      Logger.e(TAG, "Builder state is not valid")
      return BuilderError.InvalidState
    }

    var updated =
      reminder.copy(
        recurrence = recurrence.rule,
        schedule = recurrence.schedule,
        places = recurrence.places,
        location = recurrence.location,
        action = reminderActionCalculator(processedBuilderItems.typeMap),
      )
    updated = items.fold(updated) { acc, item -> item.modifier.putInto(acc) }

    when (val validationResult = reminderValidator(updated)) {
      is ReminderValidator.ValidationResult.Failed -> {
        return findError(validationResult.error, processedBuilderItems)
      }

      else -> {
      }
    }

    Logger.i(TAG, "Error is Unknown")
    return BuilderError.Unknown
  }

  /** Routes on the specific reason [reminderValidator] failed for, rather than re-guessing from
   *  which [BiType]s happen to be present: TARGET and SUB_TASKS failures used to fall through the
   *  EVENT_TIME-oriented checks below (or worse, accidentally match one of them, e.g. reporting a
   *  missing Date/Time for what was actually an empty shopping list). */
  private fun findError(
    validationError: ReminderValidator.ValidationError,
    processedBuilderItems: ProcessedBuilderItems,
  ): BuilderError {
    val biTypeMap = BiTypeMap(processedBuilderItems.typeMap)
    return when (validationError) {
      ReminderValidator.ValidationError.TARGET -> findErrorTarget(biTypeMap)
      ReminderValidator.ValidationError.SUB_TASKS -> {
        BuilderError.RequiresBiType(BuilderError.BiTypeCollection.Single(BiType.SUB_TASKS))
      }
      ReminderValidator.ValidationError.EVENT_TIME -> findErrorEventTime(biTypeMap, processedBuilderItems)
    }
  }

  private fun findErrorEventTime(
    biTypeMap: BiTypeMap,
    processedBuilderItems: ProcessedBuilderItems,
  ): BuilderError =
    when {
      biTypeMap.containsAny(BiType.DAYS_OF_WEEK) -> {
        findErrorDayOfWeek(biTypeMap)
      }

      biTypeMap.containsAny(BiType.DAY_OF_MONTH) -> {
        findErrorDayOfMonth(biTypeMap)
      }

      biTypeMap.containsAny(BiType.DAY_OF_YEAR) -> {
        findErrorDayOfYear(biTypeMap)
      }

      biTypeMap.containsAny(BiType.DATE) -> {
        findErrorDate(biTypeMap)
      }

      biTypeMap.containsAny(BiType.TIME) -> {
        findErrorTime()
      }

      processedBuilderItems.groupMap.containsKey(BiGroup.ICAL) -> {
        findErrorICalendar(biTypeMap)
      }

      biTypeMap.containsAny(BiType.LOCATION_DELAY_DATE, BiType.LOCATION_DELAY_TIME) -> {
        findErrorLocation(biTypeMap)
      }

      biTypeMap.containsAny(BiType.SUMMARY) -> {
        findErrorWhenSummary()
      }

      else -> BuilderError.Unknown
    }

  private fun findErrorTarget(biTypeMap: BiTypeMap): BuilderError =
    when {
      biTypeMap.containsAny(BiType.EMAIL) -> {
        BuilderError.RequiresBiType(BuilderError.BiTypeCollection.Single(BiType.EMAIL))
      }

      biTypeMap.containsAny(BiType.LINK) -> {
        BuilderError.RequiresBiType(BuilderError.BiTypeCollection.Single(BiType.LINK))
      }

      biTypeMap.containsAny(BiType.PHONE_CALL) -> {
        BuilderError.RequiresBiType(BuilderError.BiTypeCollection.Single(BiType.PHONE_CALL))
      }

      biTypeMap.containsAny(BiType.SMS) -> {
        BuilderError.RequiresBiType(BuilderError.BiTypeCollection.Single(BiType.SMS))
      }

      biTypeMap.containsAny(BiType.APPLICATION) -> {
        BuilderError.RequiresBiType(BuilderError.BiTypeCollection.Single(BiType.APPLICATION))
      }

      else -> BuilderError.Unknown
    }

  private fun findErrorLocation(biTypeMap: BiTypeMap): BuilderError =
    when {
      biTypeMap.containsAll(BiType.LOCATION_DELAY_DATE, BiType.LOCATION_DELAY_TIME) -> {
        BuilderError.RequiresBiType(
          BuilderError.BiTypeCollection.Multiple.Or(
            BiType.LEAVING_COORDINATES,
            BiType.ARRIVING_COORDINATES,
          ),
        )
      }

      biTypeMap.containsAny(BiType.LOCATION_DELAY_DATE) -> {
        BuilderError.RequiresBiType(
          BuilderError.BiTypeCollection.Single(BiType.LOCATION_DELAY_TIME),
        )
      }

      biTypeMap.containsAny(BiType.LOCATION_DELAY_TIME) -> {
        BuilderError.RequiresBiType(
          BuilderError.BiTypeCollection.Single(BiType.LOCATION_DELAY_DATE),
        )
      }

      else -> {
        BuilderError.Unknown
      }
    }

  private fun findErrorWhenSummary(): BuilderError =
    BuilderError.RequiresBiType(
      BuilderError.BiTypeCollection.Multiple.And(
        BiType.DATE,
        BiType.TIME,
      ),
    )

  private fun findErrorICalendar(biTypeMap: BiTypeMap): BuilderError {
    Logger.d(TAG, "Find ICalendar error for $biTypeMap")
    return when {
      biTypeMap.containsAll(BiType.ICAL_START_DATE, BiType.ICAL_START_TIME) -> {
        Logger.d(TAG, "ICalendar contains start date and time")
        createAndErrorForMissingICalTypes(
          biTypeMap,
          BiType.ICAL_FREQ,
          BiType.ICAL_COUNT,
        )
      }

      biTypeMap.containsAny(BiType.ICAL_START_DATE) -> {
        Logger.d(TAG, "ICalendar contains start date")
        createAndErrorForMissingICalTypes(
          biTypeMap,
          BiType.ICAL_START_TIME,
          BiType.ICAL_FREQ,
          BiType.ICAL_COUNT,
        )
      }

      biTypeMap.containsAny(BiType.ICAL_START_TIME) -> {
        Logger.d(TAG, "ICalendar contains start time")
        createAndErrorForMissingICalTypes(
          biTypeMap,
          BiType.ICAL_START_DATE,
          BiType.ICAL_FREQ,
          BiType.ICAL_COUNT,
        )
      }

      biTypeMap.containsAny(BiType.ICAL_FREQ) -> {
        Logger.d(TAG, "ICalendar contains freq")
        createAndErrorForMissingICalTypes(
          biTypeMap,
          BiType.ICAL_START_TIME,
          BiType.ICAL_START_DATE,
          BiType.ICAL_COUNT,
        )
      }

      biTypeMap.containsAny(BiType.ICAL_COUNT) -> {
        Logger.d(TAG, "ICalendar contains count")
        createAndErrorForMissingICalTypes(
          biTypeMap,
          BiType.ICAL_START_TIME,
          BiType.ICAL_START_DATE,
          BiType.ICAL_FREQ,
        )
      }

      biTypeMap.containsAny(BiType.ICAL_UNTIL_DATE, BiType.ICAL_UNTIL_TIME) &&
        !biTypeMap.containsAll(BiType.ICAL_UNTIL_DATE, BiType.ICAL_UNTIL_TIME) -> {
        Logger.d(TAG, "ICalendar contains until date and time")
        createAndErrorForMissingICalTypes(
          biTypeMap,
          BiType.ICAL_UNTIL_DATE,
          BiType.ICAL_UNTIL_TIME,
        )
      }

      else -> {
        Logger.d(TAG, "ICalendar error is unknown")
        BuilderError.Unknown
      }
    }
  }

  private fun createAndErrorForMissingICalTypes(
    biTypeMap: BiTypeMap,
    vararg biType: BiType,
  ): BuilderError {
    val requires = biType.filter { biTypeMap.containsNone(it) }
    return if (requires.isEmpty()) {
      BuilderError.Unknown
    } else {
      BuilderError.RequiresBiType(
        BuilderError.BiTypeCollection.Multiple.And(requires),
      )
    }
  }

  private fun findErrorTime(): BuilderError =
    BuilderError.RequiresBiType(
      BuilderError.BiTypeCollection.Multiple.Or(
        BiType.DATE,
        BiType.DAYS_OF_WEEK,
        BiType.DAY_OF_MONTH,
        BiType.DAY_OF_YEAR,
      ),
    )

  private fun findErrorDate(biTypeMap: BiTypeMap): BuilderError =
    if (biTypeMap.containsAny(BiType.TIME)) {
      BuilderError.Unknown
    } else {
      BuilderError.RequiresBiType(
        BuilderError.BiTypeCollection.Single(BiType.TIME),
      )
    }

  private fun findErrorDayOfWeek(biTypeMap: BiTypeMap): BuilderError =
    if (biTypeMap.containsAny(BiType.TIME)) {
      BuilderError.Unknown
    } else {
      BuilderError.RequiresBiType(
        BuilderError.BiTypeCollection.Single(BiType.TIME),
      )
    }

  private fun findErrorDayOfMonth(biTypeMap: BiTypeMap): BuilderError =
    if (biTypeMap.containsAny(BiType.TIME)) {
      BuilderError.Unknown
    } else {
      BuilderError.RequiresBiType(
        BuilderError.BiTypeCollection.Single(BiType.TIME),
      )
    }

  private fun findErrorDayOfYear(biTypeMap: BiTypeMap): BuilderError =
    if (biTypeMap.containsAny(BiType.TIME)) {
      BuilderError.Unknown
    } else {
      BuilderError.RequiresBiType(
        BuilderError.BiTypeCollection.Single(BiType.TIME),
      )
    }

  internal class BiTypeMap(
    private val typeMap: Map<BiType, BuilderItem<*>>,
  ) {
    fun containsAny(vararg biTypes: BiType): Boolean = typeMap.keys.any { it in biTypes }

    fun containsAll(vararg biTypes: BiType): Boolean = biTypes.all { typeMap.containsKey(it) }

    fun containsNone(vararg biTypes: BiType): Boolean = typeMap.keys.none { it in biTypes }
  }

  companion object {
    private const val TAG = "BuilderErrorFinder"
  }
}
