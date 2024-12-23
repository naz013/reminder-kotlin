package com.elementary.tasks.core.services.action

import com.elementary.tasks.core.services.action.birthday.BirthdayActionProcessor
import com.elementary.tasks.core.services.action.birthday.BirthdayDataProvider
import com.elementary.tasks.core.services.action.birthday.BirthdayHandlerFactory
import com.elementary.tasks.core.services.action.reminder.ReminderActionProcessor
import com.elementary.tasks.core.services.action.reminder.ReminderDataProvider
import com.elementary.tasks.core.services.action.reminder.ReminderHandlerFactory
import com.elementary.tasks.core.services.action.reminder.ReminderRepeatProcessor
import org.koin.dsl.module

val actionModule = module {
  factory { WearNotification(get(), get()) }

  factory { ReminderDataProvider(get(), get()) }
  factory { BirthdayDataProvider(get(), get()) }

  factory { ReminderHandlerFactory(get(), get(), get(), get(), get(), get(), get()) }
  factory {
    BirthdayHandlerFactory(
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
      get()
    )
  }

  factory { ReminderActionProcessor(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
  factory { BirthdayActionProcessor(get(), get(), get(), get(), get(), get(), get(), get(), get()) }

  factory { ReminderRepeatProcessor(get(), get(), get(), get()) }
}
