package com.github.naz013.feature.home.scheduleview

import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.ReminderV2Repository

/** True when the user has created anything at all; false means a first-run (brand new) account. */
internal class HasAnyUserContentUseCase(
  private val reminderV2Repository: ReminderV2Repository,
  private val birthdayRepository: BirthdayRepository,
  private val noteRepository: NoteRepository,
  private val googleTaskRepository: GoogleTaskRepository,
) {
  suspend operator fun invoke(): Boolean =
    reminderV2Repository.count(active = true, removed = false) > 0 ||
      reminderV2Repository.count(active = false, removed = false) > 0 ||
      birthdayRepository.countAll() > 0 ||
      noteRepository.countAll(isArchived = false) > 0 ||
      googleTaskRepository.countAll() > 0
}
