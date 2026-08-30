package com.github.naz013.feature.workflow

import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.feature.workflow.builder.WorkflowRuleBuilderViewModel
import com.github.naz013.logic.workflow.BroadcastIntentSender
import com.github.naz013.workapi.BackgroundTask
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val workflowModule = module {
  factoryOf(::WorkflowRulesUtil)
  factory<BroadcastIntentSender> { BroadcastIntentSenderImpl(get()) }
  factory<BackgroundTask>(named(RunWorkflowRulesTask.TASK_KEY)) { RunWorkflowRulesTask(get()) }
  factory<BackgroundTask>(named(RunWorkflowUnacknowledgedRulesTask.TASK_KEY)) {
    RunWorkflowUnacknowledgedRulesTask(get())
  }
  factory<BackgroundTask>(named(WeeklySummaryTask.TASK_KEY)) { WeeklySummaryTask(get(), get(), get()) }

  viewModelOf(::WorkflowGalleryViewModel)
  viewModel { (groupId: String) ->
    WorkflowRulesForGroupViewModel(groupId, get(), get(), get(), get(), get(), get(), get())
  }
  viewModel { (reminderId: String) ->
    WorkflowRulesForReminderViewModel(reminderId, get(), get(), get(), get(), get(), get(), get())
  }
  viewModel { (scopeType: WorkflowScopeType, scopeId: String?, editingRuleId: String?) ->
    WorkflowRuleBuilderViewModel(scopeType, scopeId, editingRuleId, get(), get(), get(), get(), get(), get(), get())
  }
}
