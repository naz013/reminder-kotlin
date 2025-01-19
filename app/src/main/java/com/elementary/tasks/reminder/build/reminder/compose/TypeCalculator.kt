package com.elementary.tasks.reminder.build.reminder.compose

import com.elementary.tasks.reminder.build.bi.BiGroup
import com.elementary.tasks.reminder.build.bi.ProcessedBuilderItems
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.logging.Logger

class TypeCalculator(
  private val actionCalculator: ActionCalculator
) {

  operator fun invoke(processedBuilderItems: ProcessedBuilderItems): Int {
    val itemsMap = processedBuilderItems.typeMap

    val hasDateTime = itemsMap[BiType.DATE] != null && itemsMap[BiType.TIME] != null
    val hasTimer = itemsMap[BiType.COUNTDOWN_TIMER] != null
    val hasWeekdays = itemsMap[BiType.DAYS_OF_WEEK] != null
    val hasMonthDay = itemsMap[BiType.DAY_OF_MONTH] != null
    val hasDayOfYear = itemsMap[BiType.DAY_OF_YEAR] != null

    val isOnlyOneActive = isOnlyOneActive(
      hasDateTime,
      hasTimer,
      hasWeekdays,
      hasMonthDay,
      hasDayOfYear
    )
    val mainType = if (isOnlyOneActive) {
      when {
        hasDateTime -> Reminder.BY_DATE + actionCalculator(itemsMap)
        hasTimer -> Reminder.BY_TIME + actionCalculator(itemsMap)
        hasWeekdays -> Reminder.BY_WEEK + actionCalculator(itemsMap)
        hasMonthDay -> Reminder.BY_MONTH + actionCalculator(itemsMap)
        hasDayOfYear -> Reminder.BY_DAY_OF_YEAR + actionCalculator(itemsMap)
        else -> 0
      }
    } else if (isAllFalse(hasDateTime, hasTimer, hasWeekdays, hasMonthDay, hasDayOfYear)) {
      val hasShop = itemsMap[BiType.SUB_TASKS] != null
      val hasLocationIn = itemsMap[BiType.ARRIVING_COORDINATES] != null
      val hasLocationOut = itemsMap[BiType.LEAVING_COORDINATES] != null
      val hasICalRecur = processedBuilderItems.groupMap.containsKey(BiGroup.ICAL)
      val hasDate = itemsMap[BiType.DATE] != null
      val hasTime = itemsMap[BiType.TIME] != null
      val hasLocationDelay = itemsMap[BiType.LOCATION_DELAY_DATE] != null ||
        itemsMap[BiType.LOCATION_DELAY_TIME] != null

      when {
        hasLocationIn -> Reminder.BY_LOCATION + actionCalculator(itemsMap)
        hasLocationOut -> Reminder.BY_OUT + actionCalculator(itemsMap)
        hasLocationDelay -> Reminder.BY_LOCATION + actionCalculator(itemsMap)
        hasICalRecur -> Reminder.BY_RECUR + actionCalculator(itemsMap)
        hasDate -> Reminder.BY_DATE + actionCalculator(itemsMap)
        hasTime -> Reminder.BY_DATE + actionCalculator(itemsMap)
        hasShop -> Reminder.BY_DATE_SHOP
        else -> {
          0
        }
      }
    } else {
      0
    }

    Logger.i(TAG, "Calculated type = $mainType")
    return mainType
  }

  private fun isOnlyOneActive(vararg booleans: Boolean): Boolean {
    return booleans.filter { it }.size == 1
  }

  private fun isAllFalse(vararg booleans: Boolean): Boolean {
    return booleans.none { it }
  }

  companion object {
    private const val TAG = "TypeCalculator"
  }
}
