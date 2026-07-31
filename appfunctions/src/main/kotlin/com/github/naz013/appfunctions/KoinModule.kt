package com.github.naz013.appfunctions

import com.github.naz013.appfunctions.birthday.CreateSimpleBirthdayUseCase
import com.github.naz013.appfunctions.birthday.ListUpcomingBirthdaysUseCase
import com.github.naz013.appfunctions.googletask.CompleteGoogleTaskUseCase
import com.github.naz013.appfunctions.googletask.CreateGoogleTaskUseCase
import com.github.naz013.appfunctions.googletask.ListGoogleTasksUseCase
import com.github.naz013.appfunctions.note.CreateSimpleNoteUseCase
import com.github.naz013.appfunctions.reminder.CompleteReminderUseCase
import com.github.naz013.appfunctions.reminder.CreateSimpleReminderUseCase
import com.github.naz013.appfunctions.reminder.DeleteReminderUseCase
import com.github.naz013.appfunctions.reminder.ListUpcomingRemindersUseCase
import org.koin.dsl.module

val appFunctionsModule =
  module {
    factory { CreateSimpleReminderUseCase(get(), get()) }
    factory { ListUpcomingRemindersUseCase(get(), get()) }
    factory { CompleteReminderUseCase(get()) }
    factory { DeleteReminderUseCase(get()) }

    factory { CreateSimpleNoteUseCase(get(), get()) }

    factory { CreateSimpleBirthdayUseCase(get(), get()) }
    factory { ListUpcomingBirthdaysUseCase(get(), get(), get()) }

    factory { CreateGoogleTaskUseCase(get(), get(), get()) }
    factory { ListGoogleTasksUseCase(get()) }
    factory { CompleteGoogleTaskUseCase(get(), get()) }
  }
