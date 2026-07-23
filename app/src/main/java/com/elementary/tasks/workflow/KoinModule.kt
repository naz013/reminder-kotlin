package com.elementary.tasks.workflow

import com.github.naz013.workapi.BackgroundTask
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val workflowModule = module {
  factoryOf(::WorkflowRulesUtil)
  factory<BackgroundTask>(named(RunWorkflowRulesTask.TASK_KEY)) { RunWorkflowRulesTask(get()) }

  viewModelOf(::WorkflowGalleryViewModel)
  viewModel { (groupId: String) ->
    WorkflowRulesForGroupViewModel(groupId, get(), get(), get(), get(), get(), get(), get())
  }
}
