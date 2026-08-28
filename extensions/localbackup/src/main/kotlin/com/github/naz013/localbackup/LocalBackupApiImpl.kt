package com.github.naz013.localbackup

import com.github.naz013.localbackup.archive.BackupArchiveReader
import com.github.naz013.localbackup.archive.BackupArchiveWriter
import com.github.naz013.localbackup.archive.BackupEnvelope
import com.github.naz013.localbackup.crypto.BackupCipher
import com.github.naz013.localbackup.crypto.PassphraseKeyDerivation
import com.github.naz013.logging.Logger
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
import java.io.InputStream
import java.io.OutputStream
import java.util.Arrays
import javax.crypto.AEADBadTagException

internal class LocalBackupApiImpl(
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
  private val workflowTemplateRepository: WorkflowTemplateRepository,
  private val archiveWriter: BackupArchiveWriter,
  private val archiveReader: BackupArchiveReader
) : LocalBackupApi {

  override suspend fun export(output: OutputStream, passphrase: CharArray): Result<Unit> {
    val result = runCatching {
      val envelope = BackupEnvelope(
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

      val salt = PassphraseKeyDerivation.generateSalt()
      val iv = BackupCipher.generateIv()
      val key = PassphraseKeyDerivation.deriveKey(passphrase, salt)

      BackupFileHeader(salt, iv, PassphraseKeyDerivation.ITERATIONS).writeTo(output)
      BackupCipher.encryptingStream(output, key, iv).use { cipherOutput ->
        archiveWriter.write(cipherOutput, envelope)
      }
      Logger.i(TAG, "Exported local backup: ${envelope.summary()}")
    }
    Arrays.fill(passphrase, '0')
    return result.onFailure { Logger.e(TAG, "Failed to export local backup", it) }
  }

  override suspend fun import(input: InputStream, passphrase: CharArray): Result<ImportSummary> {
    val result = runCatching {
      val header = BackupFileHeader.readFrom(input)
      val key = PassphraseKeyDerivation.deriveKey(passphrase, header.salt, header.iterations)

      val envelope = BackupCipher.decryptingStream(input, key, header.iv).use { cipherInput ->
        archiveReader.read(cipherInput)
      }

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

      Logger.i(TAG, "Imported local backup: ${envelope.summary()}")
      ImportSummary(
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
    Arrays.fill(passphrase, '0')
    return result.fold(
      onSuccess = { Result.success(it) },
      onFailure = { e ->
        Logger.e(TAG, "Failed to import local backup", e)
        Result.failure(if (e.isWrongPassphrase()) WrongPassphraseException() else e)
      }
    )
  }

  private fun BackupEnvelope.summary(): String =
    "reminders=${reminders.size}, groups=${groups.size}, birthdays=${birthdays.size}, " +
      "places=${places.size}, presets=${presets.size}, tags=${tags.size}, " +
      "tagAssignments=${tagAssignments.size}, routines=${routines.size}, " +
      "routineExecutions=${routineExecutions.size}, workflowRules=${workflowRules.size}, " +
      "workflowTemplates=${workflowTemplates.size}"

  private fun Throwable.isWrongPassphrase(): Boolean {
    var current: Throwable? = this
    while (current != null) {
      if (current is AEADBadTagException) return true
      current = current.cause
    }
    return false
  }

  companion object {
    private const val TAG = "LocalBackupApi"
  }
}
