package com.github.naz013.logic.workflow

import org.koin.dsl.module

val logicWorkflowModule = module {
  factory { WorkflowEngine(get(), get(), get(), get()) }
  factory { ApplyWorkflowTemplateUseCase(get(), get()) }
  factory { SaveWorkflowRuleAsTemplateUseCase(get(), get()) }
  factory { CreateWorkflowRuleUseCase(get()) }
  factory { GetWorkflowRulesForReminderUseCase(get()) }
  factory { GetWorkflowRulesForGroupUseCase(get()) }
  factory { GetGlobalWorkflowRulesUseCase(get()) }
  factory { GetWorkflowTemplatesUseCase(get()) }
}
