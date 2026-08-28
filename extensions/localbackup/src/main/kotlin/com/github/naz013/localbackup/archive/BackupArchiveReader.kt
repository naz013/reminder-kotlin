package com.github.naz013.localbackup.archive

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.RecurPreset
import com.github.naz013.domain.Tag
import com.github.naz013.domain.TagAssignment
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.routine.RoutineExecutionRecord
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.files.DataConverter
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.InputStream

internal class BackupArchiveReader(
  private val dataConverter: DataConverter
) {
  suspend fun read(input: InputStream): BackupEnvelope {
    val dataInput = DataInputStream(input)
    val formatVersion = dataInput.readInt()
    if (formatVersion != BackupArchiveWriter.FORMAT_VERSION) {
      throw UnsupportedBackupFormatException(formatVersion)
    }

    val reminders = mutableListOf<ReminderV2>()
    val groups = mutableListOf<GroupV2>()
    val birthdays = mutableListOf<Birthday>()
    val places = mutableListOf<Place>()
    val presets = mutableListOf<RecurPreset>()
    val tags = mutableListOf<Tag>()
    val tagAssignments = mutableListOf<TagAssignment>()
    val routines = mutableListOf<Routine>()
    val routineExecutions = mutableListOf<RoutineExecutionRecord>()
    val workflowRules = mutableListOf<WorkflowRule>()
    val workflowTemplates = mutableListOf<WorkflowTemplate>()

    val count = dataInput.readInt()
    repeat(count) {
      val size = dataInput.readInt()
      val bytes = ByteArray(size)
      dataInput.readFully(bytes)
      when (val item = dataConverter.toData(ByteArrayInputStream(bytes))) {
        is ReminderV2 -> reminders += item
        is GroupV2 -> groups += item
        is Birthday -> birthdays += item
        is Place -> places += item
        is RecurPreset -> presets += item
        is Tag -> tags += item
        is TagAssignment -> tagAssignments += item
        is Routine -> routines += item
        is RoutineExecutionRecord -> routineExecutions += item
        is WorkflowRule -> workflowRules += item
        is WorkflowTemplate -> workflowTemplates += item
        else -> Unit
      }
    }

    return BackupEnvelope(
      reminders, groups, birthdays, places, presets, tags, tagAssignments, routines, routineExecutions,
      workflowRules, workflowTemplates
    )
  }
}

class UnsupportedBackupFormatException(val formatVersion: Int) :
  IllegalArgumentException("Unsupported backup format version: $formatVersion")
