package com.elementary.tasks.core.data.repository

import org.koin.dsl.module

val repositoryModule = module {
  single { ReminderRepository(get()) }
  single { BirthdayRepository(get()) }
  single { NoteRepository(get()) }
  single { NoteImageRepository(get()) }
  single { MissedCallRepository(get()) }
}
