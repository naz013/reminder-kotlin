package com.elementary.tasks.core.services.action

import com.elementary.tasks.core.services.action.birthday.BirthdayActionProcessor
import com.elementary.tasks.core.services.action.birthday.BirthdayDataProvider
import com.elementary.tasks.core.services.action.birthday.BirthdayHandlerFactory
import com.elementary.tasks.core.services.action.missedcall.MissedCallActionProcessor
import com.elementary.tasks.core.services.action.missedcall.MissedCallDataProvider
import com.elementary.tasks.core.services.action.missedcall.MissedCallHandlerFactory
import com.elementary.tasks.core.services.action.reminder.ReminderActionProcessor
import com.elementary.tasks.core.services.action.reminder.ReminderDataProvider
import com.elementary.tasks.core.services.action.reminder.ReminderHandlerFactory
import org.koin.dsl.module

val actionModule = module {
  single { WearNotification(get(), get()) }

  single { ReminderDataProvider(get(), get(), get()) }
  single { BirthdayDataProvider(get(), get(), get()) }
  single { MissedCallDataProvider(get(), get(), get()) }

  single { ReminderHandlerFactory(get(), get(), get(), get(), get(), get(), get()) }
  single { MissedCallHandlerFactory(get(), get(), get(), get(), get(), get(), get(), get()) }
  single {
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
      get()
    )
  }

  single { ReminderActionProcessor(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
  single { BirthdayActionProcessor(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
  single { MissedCallActionProcessor(get(), get(), get(), get(), get(), get(), get(), get()) }
}
