package com.github.naz013.localbackup

import com.github.naz013.localbackup.archive.BackupEnvelope
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.RecurPresetRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.RoutineExecutionRepository
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.repository.WorkflowTemplateRepository

/**
 * Reads every repository [LocalBackupApi] and the cross-app transfer feature both snapshot into a
 * [BackupEnvelope] - split out of [LocalBackupApiImpl] so the two callers share one implementation
 * instead of repeating the same eleven-repository read.
 */
internal class BuildBackupEnvelopeUseCase(
  private val reminderV2Repository: ReminderV2Repository,
  private val groupV2Repository: GroupV2Repository,
  private val birthdayRepository: BirthdayRepository,
  private val placeRepository: PlaceRepository,
  private val recurPresetRepository: RecurPresetRepository,
  private val tagRepository: TagRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val routineRepository: RoutineRepository,
  private val routineExecutionRepository: RoutineExecutionRepository,
  private val workflowRuleRepository: WorkflowRuleRepository,
  private val workflowTemplateRepository: WorkflowTemplateRepository
) {
  suspend operator fun invoke(): BackupEnvelope = BackupEnvelope(
    reminders = reminderV2Repository.getAll().filterNot { it.offlineOnly },
    groups = groupV2Repository.getAll(),
    birthdays = birthdayRepository.getAll(),
    places = placeRepository.getAll(),
    presets = recurPresetRepository.getAll(),
    tags = tagRepository.getAll(),
    tagAssignments = tagAssignmentRepository.getAll(),
    routines = routineRepository.getAll(),
    routineExecutions = routineExecutionRepository.getAll(),
    workflowRules = workflowRuleRepository.getAll(),
    workflowTemplates = workflowTemplateRepository.getAll()
  )
}
