package com.elementary.tasks.birthdays.dialog

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.work.SingleBackupWorker
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayShowAdapter
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayShow
import com.github.naz013.common.intent.IntentKeys
import com.elementary.tasks.core.utils.Notifier
import com.github.naz013.common.datetime.DateTimeManager
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.github.naz013.domain.Birthday
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.repository.BirthdayRepository
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class ShowBirthdayViewModel(
  private val id: String,
  private val birthdayRepository: BirthdayRepository,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val notifier: Notifier,
  private val dateTimeManager: DateTimeManager,
  private val uiBirthdayShowAdapter: UiBirthdayShowAdapter,
  private val appWidgetUpdater: AppWidgetUpdater
) : BaseProgressViewModel(dispatcherProvider) {

  private val _birthday = mutableLiveDataOf<UiBirthdayShow>()
  val birthday = _birthday.toLiveData()

  var uiBirthdayShow: UiBirthdayShow? = null
    private set
  var isEventShowed = false

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)
    viewModelScope.launch(dispatcherProvider.default()) {
      val birthday = birthdayRepository.getById(id) ?: return@launch
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
      val birthday = birthdayRepository.getById(id)

      if (birthday == null) {
        postCommand(Commands.SAVED)
        return@launch
      }

      birthdayRepository.save(
        birthday.copy(
          updatedAt = dateTimeManager.getNowGmtDateTime(),
          showedYear = LocalDate.now().year
        )
      )
      notifier.showBirthdayPermanent()
      appWidgetUpdater.updateBirthdaysWidget()
      appWidgetUpdater.updateScheduleWidget()
      workerLauncher.startWork(SingleBackupWorker::class.java, IntentKeys.INTENT_ID, birthday.uuId)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }
}
