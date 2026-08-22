package com.github.naz013.logic.notificationaction

import com.github.naz013.logic.notificationaction.birthday.BirthdayActionProcessor
import com.github.naz013.logic.notificationaction.birthday.BirthdayCancelActionFactory
import com.github.naz013.logic.notificationaction.reminder.ReminderActionProcessor
import com.github.naz013.logic.notificationaction.reminder.ReminderCompleteSnoozeFactory
import com.github.naz013.logic.notificationaction.reminder.ReminderRepeatProcessor
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val logicNotificationActionModule = module {
  single { InAppAlertBus() }
  single { ForegroundStateTracker() }

  factoryOf(::DoNotDisturbManager)
  factoryOf(::WearNotification)

  factoryOf(::ReminderCompleteSnoozeFactory)
  factory {
    ReminderActionProcessor(
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
    )
  }
  factoryOf(::ReminderRepeatProcessor)

  factoryOf(::BirthdayCancelActionFactory)
  factory {
    BirthdayActionProcessor(
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
    )
  }
}
