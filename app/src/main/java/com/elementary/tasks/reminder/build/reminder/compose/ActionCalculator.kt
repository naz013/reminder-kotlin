package com.elementary.tasks.reminder.build.reminder.compose

import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.reminder.build.bi.BiType
import com.elementary.tasks.reminder.build.BuilderItem

class ActionCalculator {

  operator fun invoke(itemsMap: Map<BiType, BuilderItem<*>>): Int {
    return when {
      itemsMap[BiType.SMS]?.modifier?.isCorrect() == true -> Reminder.Action.SMS
      itemsMap[BiType.PHONE_CALL]?.modifier?.isCorrect() == true -> Reminder.Action.CALL
      itemsMap[BiType.EMAIL]?.modifier?.isCorrect() == true -> Reminder.Action.EMAIL
      itemsMap[BiType.LINK]?.modifier?.isCorrect() == true -> Reminder.Action.LINK
      itemsMap[BiType.APPLICATION]?.modifier?.isCorrect() == true -> Reminder.Action.APP
      itemsMap[BiType.SUB_TASKS]?.modifier?.isCorrect() == true -> Reminder.Action.SHOP
      else -> Reminder.Action.NONE
    }
  }
}
