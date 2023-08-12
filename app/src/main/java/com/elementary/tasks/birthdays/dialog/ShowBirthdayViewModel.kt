package com.elementary.tasks.birthdays.dialog

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.work.SingleBackupWorker
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayShowAdapter
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayShow
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.work.WorkerLauncher
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class ShowBirthdayViewModel(
  private val id: String,
  private val birthdaysDao: BirthdaysDao,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val notifier: Notifier,
  private val dateTimeManager: DateTimeManager,
  private val uiBirthdayShowAdapter: UiBirthdayShowAdapter,
  private val updatesHelper: UpdatesHelper
) : BaseProgressViewModel(dispatcherProvider) {

  private val _birthday = mutableLiveDataOf<UiBirthdayShow>()
  val birthday = _birthday.toLiveData()

  var uiBirthdayShow: UiBirthdayShow? = null
    private set
  var isEventShowed = false

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)
    viewModelScope.launch(dispatcherProvider.default()) {
      val birthday = birthdaysDao.getById(id) ?: return@launch
      uiBirthdayShow = uiBirthdayShowAdapter.convert(birthday)
      _birthday.postValue(uiBirthdayShow)
    }
  }

  fun onTestLoad(birthday: Birthday?) {
    if (birthday != null) {
      uiBirthdayShow = uiBirthdayShowAdapter.convert(birthday)
      _birthday.postValue(uiBirthdayShow)
    }
  }

  fun getNumber(): String? {
    return uiBirthdayShow?.number
  }

  fun getId(): String {
    return id
  }

  fun getUniqueId(): Int {
    return uiBirthdayShow?.uniqueId ?: 2123
  }

  fun saveBirthday() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val birthday = birthdaysDao.getById(id)

      if (birthday == null) {
        postCommand(Commands.SAVED)
        return@launch
      }

      birthdaysDao.insert(
        birthday.copy(
          updatedAt = dateTimeManager.getNowGmtDateTime(),
          showedYear = LocalDate.now().year
        )
      )
      notifier.showBirthdayPermanent()
      updatesHelper.updateBirthdaysWidget()
      updatesHelper.updateTasksWidget()
      workerLauncher.startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, birthday.uuId)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }
}
