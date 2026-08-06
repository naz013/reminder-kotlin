package com.github.naz013.usecase.reminders

import org.koin.dsl.module

val remindersUseCaseModule = module {
  factory { GetActiveRemindersV2UseCase(get()) }
  factory { GetActiveRemindersV2ByGroupIdUseCase(get()) }
  factory { CountActiveRemindersV2ByGroupIdUseCase(get()) }
  factory { GetReminderV2ByIdUseCase(get()) }
  factory { GetRemindersV2ByGroupIdUseCase(get()) }
  factory { GetRemindersV2InRangeUseCase(get()) }
  factory { GetRemindersV2ByRemovedStatusUseCase(get()) }
  factory { GetOccurrencesByDateRangeUseCase(get()) }
  factory { ResolveReminderV2NotificationSettingsUseCase(get(), get()) }
  factory { WorkflowEngine(get(), get(), get(), get()) }
  factory { ApplyWorkflowTemplateUseCase(get(), get()) }
  factory { SaveWorkflowRuleAsTemplateUseCase(get(), get()) }
  factory { CreateWorkflowRuleUseCase(get()) }
  factory { GetWorkflowRulesForReminderUseCase(get()) }
  factory { GetWorkflowRulesForGroupUseCase(get()) }
  factory { GetGlobalWorkflowRulesUseCase(get()) }
  factory { GetWorkflowTemplatesUseCase(get()) }
}
