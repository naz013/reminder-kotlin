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
 * Upserts a [BackupEnvelope] back into every repository [LocalBackupApi] and the cross-app transfer
 * feature both restore into - split out of [LocalBackupApiImpl] so the two callers share one
 * implementation instead of repeating the same eleven-repository write.
 */
internal class ApplyBackupEnvelopeUseCase(
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
  suspend operator fun invoke(envelope: BackupEnvelope): ImportSummary {
    envelope.reminders.forEach { reminderV2Repository.save(it) }
    envelope.groups.forEach { groupV2Repository.save(it) }
    envelope.birthdays.forEach { birthdayRepository.save(it) }
    envelope.places.forEach { placeRepository.save(it) }
    envelope.presets.forEach { recurPresetRepository.save(it) }
    envelope.tags.forEach { tagRepository.save(it) }
    // A restore is "make local state match this snapshot exactly," same reasoning as the
    // cloud-download apply path - replace, not a per-row merge.
    tagAssignmentRepository.replaceAll(envelope.tagAssignments)
    envelope.routines.forEach { routineRepository.save(it) }
    envelope.routineExecutions.forEach { routineExecutionRepository.save(it) }
    envelope.workflowRules.forEach { workflowRuleRepository.save(it) }
    envelope.workflowTemplates.forEach { workflowTemplateRepository.save(it) }

    return ImportSummary(
      remindersImported = envelope.reminders.size,
      groupsImported = envelope.groups.size,
      birthdaysImported = envelope.birthdays.size,
      placesImported = envelope.places.size,
      presetsImported = envelope.presets.size,
      tagsImported = envelope.tags.size,
      tagAssignmentsImported = envelope.tagAssignments.size,
      routinesImported = envelope.routines.size,
      routineExecutionsImported = envelope.routineExecutions.size,
      workflowRulesImported = envelope.workflowRules.size,
      workflowTemplatesImported = envelope.workflowTemplates.size
    )
  }
}
