package com.github.naz013.appfunctions

import com.github.naz013.appfunctions.birthday.CreateSimpleBirthdayUseCase
import com.github.naz013.appfunctions.birthday.DeleteBirthdayUseCase
import com.github.naz013.appfunctions.birthday.ListUpcomingBirthdaysUseCase
import com.github.naz013.appfunctions.birthday.SearchBirthdaysUseCase
import com.github.naz013.appfunctions.birthday.UpdateBirthdayUseCase
import com.github.naz013.appfunctions.googletask.CompleteGoogleTaskUseCase
import com.github.naz013.appfunctions.googletask.CreateGoogleTaskUseCase
import com.github.naz013.appfunctions.googletask.DeleteGoogleTaskUseCase
import com.github.naz013.appfunctions.googletask.ListGoogleTasksUseCase
import com.github.naz013.appfunctions.googletask.SearchGoogleTasksUseCase
import com.github.naz013.appfunctions.googletask.UpdateGoogleTaskUseCase
import com.github.naz013.appfunctions.note.CreateSimpleNoteUseCase
import com.github.naz013.appfunctions.note.DeleteNoteUseCase
import com.github.naz013.appfunctions.note.UpdateNoteUseCase
import com.github.naz013.appfunctions.reminder.CompleteReminderUseCase
import com.github.naz013.appfunctions.reminder.CreateSimpleReminderUseCase
import com.github.naz013.appfunctions.reminder.DeleteReminderUseCase
import com.github.naz013.appfunctions.reminder.ListUpcomingRemindersUseCase
import com.github.naz013.appfunctions.reminder.SearchRemindersUseCase
import com.github.naz013.appfunctions.reminder.UpdateReminderUseCase
import org.koin.dsl.module

val appFunctionsModule =
  module {
    factory { CreateSimpleReminderUseCase(get(), get()) }
    factory { ListUpcomingRemindersUseCase(get(), get()) }
    factory { CompleteReminderUseCase(get()) }
    factory { DeleteReminderUseCase(get()) }
    factory { UpdateReminderUseCase(get(), get()) }
    factory { SearchRemindersUseCase(get()) }

    factory { CreateSimpleNoteUseCase(get(), get()) }
    factory { UpdateNoteUseCase(get()) }
    factory { DeleteNoteUseCase(get()) }

    factory { CreateSimpleBirthdayUseCase(get(), get()) }
    factory { ListUpcomingBirthdaysUseCase(get(), get(), get()) }
    factory { UpdateBirthdayUseCase(get(), get()) }
    factory { DeleteBirthdayUseCase(get()) }
    factory { SearchBirthdaysUseCase(get()) }

    factory { CreateGoogleTaskUseCase(get(), get(), get()) }
    factory { ListGoogleTasksUseCase(get()) }
    factory { CompleteGoogleTaskUseCase(get(), get()) }
    factory { UpdateGoogleTaskUseCase(get(), get(), get()) }
    factory { DeleteGoogleTaskUseCase(get(), get()) }
    factory { SearchGoogleTasksUseCase(get()) }
  }
