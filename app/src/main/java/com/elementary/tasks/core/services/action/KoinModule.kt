package com.elementary.tasks.core.services.action

import com.elementary.tasks.core.services.action.birthday.BirthdayDataProvider
import com.elementary.tasks.core.services.action.reminder.ReminderDataProvider
import org.koin.dsl.module

val actionModule = module {
  factory { ReminderDataProvider(get(), get(), get()) }
  factory { BirthdayDataProvider(get(), get(), get()) }
}
