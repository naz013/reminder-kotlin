package com.github.naz013.usecase.reminders

import org.koin.dsl.module

val remindersUseCaseModule = module {
  factory { GetActiveRemindersUseCase(get()) }
  factory { GetActiveRemindersWithoutGpsUseCase(get()) }

  factory { GetReminderByIdUseCase(get()) }

  factory { GetActiveRemindersV2UseCase(get()) }
  factory { GetReminderV2ByIdUseCase(get()) }
  factory { GetRemindersV2ByGroupIdUseCase(get()) }
  factory { GetRemindersV2InRangeUseCase(get()) }
  factory { ResolveReminderV2NotificationSettingsUseCase(get(), get()) }
  factory { WorkflowEngine(get(), get(), get()) }
}
