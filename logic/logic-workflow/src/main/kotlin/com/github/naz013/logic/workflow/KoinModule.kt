package com.github.naz013.logic.workflow

import com.github.naz013.logic.reminder.ReminderWorkflowTrigger
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val logicWorkflowModule = module {
  factoryOf(::SaveWorkflowRuleUseCase)
  factory { WorkflowEngine(get(), get(), get(), get(), get()) }
  factoryOf(::WorkflowActionDispatcher)
  factoryOf(::WorkflowTriggerRunner)
  factory<ReminderWorkflowTrigger> { get<WorkflowTriggerRunner>() }
  factoryOf(::DeleteWorkflowRuleUseCase)
  factoryOf(::SaveWorkflowTemplateUseCase)
  factory { ApplyWorkflowTemplateUseCase(get(), get()) }
  factory { SaveWorkflowRuleAsTemplateUseCase(get(), get()) }
  factory { CreateWorkflowRuleUseCase(get()) }
  factory { GetWorkflowRulesForReminderUseCase(get()) }
  factory { GetWorkflowRulesForGroupUseCase(get()) }
  factory { GetGlobalWorkflowRulesUseCase(get()) }
  factory { GetWorkflowTemplatesUseCase(get()) }
  factory { WorkflowConfigImpl(get()) as WorkflowConfig }
}
