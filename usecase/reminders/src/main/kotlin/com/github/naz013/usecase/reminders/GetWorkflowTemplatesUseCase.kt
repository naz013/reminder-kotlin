package com.github.naz013.usecase.reminders

import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.repository.WorkflowTemplateRepository

/** Reads the full template gallery (built-in + user-saved) for a browsing screen. */
class GetWorkflowTemplatesUseCase(
  private val workflowTemplateRepository: WorkflowTemplateRepository
) {

  suspend operator fun invoke(): List<WorkflowTemplate> = workflowTemplateRepository.getAll()
}
