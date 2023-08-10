package com.elementary.tasks.settings.birthday

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.work.BirthdayDeleteBackupWorker
import com.elementary.tasks.birthdays.work.ScanContactsWorker
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.utils.DispatcherProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import timber.log.Timber

class BirthdaySettingsViewModel(
  private val birthdaysDao: BirthdaysDao,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val notifier: Notifier,
  private val scanContactsWorker: ScanContactsWorker,
  private val textProvider: TextProvider
) : BaseProgressViewModel(dispatcherProvider) {

  private var mJob: Job? = null

  fun startScan() {
    mJob?.cancel()
    mJob = viewModelScope.launch(dispatcherProvider.default()) {
      postInProgress(true)
      val count = runCatching { scanContactsWorker.scanContacts() }.getOrNull() ?: 0
      Timber.d("Found $count birthdays")
      postInProgress(false)
      val message = if (count == 0) {
        textProvider.getText(R.string.no_new_birthdays)
      } else {
        textProvider.getText(R.string.voice_found) + " $count " +
          textProvider.getText(R.string.birthdays)
      }
      postError(StringUtils.capitalize(message.lowercase()))
    }
  }

  fun deleteAllBirthdays() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val list = birthdaysDao.getAll()
      for (birthday in list) {
        birthdaysDao.delete(birthday)
        workerLauncher.startWork(
          BirthdayDeleteBackupWorker::class.java,
          Constants.INTENT_ID,
          birthday.uuId
        )
      }
      notifier.showBirthdayPermanent()
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }
}
