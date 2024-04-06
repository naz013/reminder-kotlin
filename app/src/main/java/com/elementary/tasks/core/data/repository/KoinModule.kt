package com.elementary.tasks.core.data.repository

import org.koin.dsl.module

val repositoryModule = module {
  factory { ReminderRepository(get()) }
  factory { BirthdayRepository(get()) }
  factory { NoteRepository(get()) }
  factory { NoteImageRepository(get()) }
  factory { RecurPresetRepository(get()) }
}
