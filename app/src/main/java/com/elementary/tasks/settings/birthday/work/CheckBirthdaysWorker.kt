package com.elementary.tasks.settings.birthday.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.elementary.tasks.birthdays.usecase.SaveBirthdayUseCase
import com.elementary.tasks.settings.birthday.usecase.GetContactsWithMetadataUseCase
import com.github.naz013.common.Permissions
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import kotlinx.coroutines.withContext

class CheckBirthdaysWorker(
  context: Context,
  workerParams: WorkerParameters,
  private val birthdayRepository: BirthdayRepository,
  private val dateTimeManager: DateTimeManager,
  private val dispatcherProvider: DispatcherProvider,
  private val saveBirthdayUseCase: SaveBirthdayUseCase,
  private val getContactsWithMetadataUseCase: GetContactsWithMetadataUseCase,
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    if (!Permissions.checkPermission(applicationContext, Permissions.READ_CONTACTS)) {
      Logger.e(TAG, "No READ_CONTACTS permission!")
      return Result.success()
    }
    withContext(dispatcherProvider.io()) {
      scanContacts()
    }
    return Result.success()
  }

  private suspend fun scanContacts() {
    val contacts = getContactsWithMetadataUseCase()
    if (contacts.isEmpty()) {
      Logger.w(TAG, "No contacts with birthdays found.")
      return
    }

    val birthdays = birthdayRepository.getAll().associateBy { it.contactId }

    var newBirthdaysCount = 0
    contacts.filterNot { birthdays.containsKey(it.id) }.forEach { (id, name, number, birthday) ->
      val birthdayDate = birthday?.let { dateTimeManager.findBirthdayDate(it) } ?: return@forEach
      val key = number?.substring(1) ?: "0"
      val birthdayItem = Birthday(
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
        syncState = SyncState.WaitingForUpload
      )
      saveBirthdayUseCase(birthdayItem)
      newBirthdaysCount++
    }

    Logger.i(TAG, "Scan complete. New birthdays added: $newBirthdaysCount")
  }

  companion object {
    private const val TAG = "CheckBirthdaysWorker"

    fun scheduleOnTime(context: Context) {
      val work = OneTimeWorkRequest.Builder(CheckBirthdaysWorker::class.java)
      WorkManager.getInstance(context).enqueue(work.build())
      Logger.i(TAG, "CheckBirthdaysWorker scheduled.")
    }
  }
}
