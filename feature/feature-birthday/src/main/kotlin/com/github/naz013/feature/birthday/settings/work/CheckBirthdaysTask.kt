package com.github.naz013.feature.birthday.settings.work

import android.content.Context
import com.github.naz013.logic.birthday.SaveBirthdayUseCase
import com.github.naz013.feature.birthday.settings.usecase.GetContactsWithMetadataUseCase
import com.github.naz013.common.Permissions
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult

class CheckBirthdaysTask(
  private val context: Context,
  private val birthdayRepository: BirthdayRepository,
  private val dateTimeManager: DateTimeManager,
  private val saveBirthdayUseCase: SaveBirthdayUseCase,
  private val getContactsWithMetadataUseCase: GetContactsWithMetadataUseCase,
) : BackgroundTask {
  override suspend fun run(
    input: TaskData,
    progress: TaskProgressReporter,
  ): TaskResult {
    if (!Permissions.checkPermission(context, Permissions.READ_CONTACTS)) {
      Logger.e(TASK_KEY, "No READ_CONTACTS permission!")
      return TaskResult.Success
    }
    scanContacts()
    return TaskResult.Success
  }

  private suspend fun scanContacts() {
    val contacts = getContactsWithMetadataUseCase()
    if (contacts.isEmpty()) {
      Logger.w(TASK_KEY, "No contacts with birthdays found.")
      return
    }

    val birthdays = birthdayRepository.getAll().associateBy { it.contactId }

    var newBirthdaysCount = 0
    contacts.filterNot { birthdays.containsKey(it.id) }.forEach { (id, name, number, birthday) ->
      val birthdayDate = birthday?.let { dateTimeManager.findBirthdayDate(it) } ?: return@forEach
      val key = number?.substring(1) ?: "0"
      val birthdayItem =
        Birthday(
          name = name,
          date = dateTimeManager.formatBirthdayDate(birthdayDate),
          number = number ?: "",
          showedYear = 0,
          contactId = id,
          day = birthdayDate.dayOfMonth,
          month = birthdayDate.monthValue - 1,
          key = "$name|$key",
          updatedAt = dateTimeManager.getNowGmtDateTime(),
          version = 0L,
          syncState = SyncState.WaitingForUpload,
        )
      saveBirthdayUseCase(birthdayItem)
      newBirthdaysCount++
    }

    Logger.i(TASK_KEY, "Scan complete. New birthdays added: $newBirthdaysCount")
  }

  companion object {
    const val TASK_KEY = "check_birthdays"
  }
}
