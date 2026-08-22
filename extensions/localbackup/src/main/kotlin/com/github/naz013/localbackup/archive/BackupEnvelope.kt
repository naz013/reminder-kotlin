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

/**
 * Notes are intentionally excluded from v1 - they bundle image attachments through a dedicated
 * sync pre/post-processing pipeline that a simple local archive doesn't replicate. See the Local
 * Backup feature plan.
 */
data class BackupEnvelope(
  val reminders: List<ReminderV2> = emptyList(),
  val groups: List<GroupV2> = emptyList(),
  val birthdays: List<Birthday> = emptyList(),
  val places: List<Place> = emptyList(),
  val presets: List<RecurPreset> = emptyList(),
  val tags: List<Tag> = emptyList(),
  val tagAssignments: List<TagAssignment> = emptyList(),
  val routines: List<Routine> = emptyList(),
  val routineExecutions: List<RoutineExecutionRecord> = emptyList()
) {
  fun isEmpty(): Boolean =
    reminders.isEmpty() && groups.isEmpty() && birthdays.isEmpty() && places.isEmpty() && presets.isEmpty() &&
      tags.isEmpty() && tagAssignments.isEmpty() && routines.isEmpty() && routineExecutions.isEmpty()
}
