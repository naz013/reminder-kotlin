package com.elementary.tasks.birthdays.list

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.birthdays.list.filter.BirthdayQueryFilter
import com.elementary.tasks.birthdays.work.BirthdayDeleteBackupWorker
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayListAdapter
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Birthday
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(FlowPreview::class)
class BirthdaysViewModel(
  private val birthdayRepository: BirthdayRepository,
  private val uiBirthdayListAdapter: UiBirthdayListAdapter,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val notifier: Notifier,
  private val appWidgetUpdater: AppWidgetUpdater
) : BaseProgressViewModel(dispatcherProvider) {

  private val _birthdays = mutableLiveDataOf<List<UiBirthdayList>>()
  val birthdays = _birthdays.toLiveData()

  private val _canSearch = mutableLiveDataOf<Boolean>()
  val canSearch = _canSearch.toLiveData()

  private var lastQuery: String = ""
  private var allBirthdays = listOf<Birthday>()

  private val queryFilterFlow = MutableStateFlow("")

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      queryFilterFlow
        .debounce(300)
        .collect {
          lastQuery = it
          filterBirthdays()
        }
    }
  }

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    loadBirthdays()
  }

  fun onSearchUpdate(query: String) {
    Logger.i(TAG, "On search update: ${Logger.private(query)}")
    queryFilterFlow.tryEmit(query)
  }

  fun deleteBirthday(id: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      birthdayRepository.delete(id)
      notifier.showBirthdayPermanent()
      workerLauncher.startWork(BirthdayDeleteBackupWorker::class.java, IntentKeys.INTENT_ID, id)
      loadBirthdays()
      appWidgetUpdater.updateScheduleWidget()
      appWidgetUpdater.updateBirthdaysWidget()
      postInProgress(false)
      postCommand(Commands.DELETED)
    }
  }

  private fun loadBirthdays() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      allBirthdays = birthdayRepository.getAll()
      filterBirthdays()
      postInProgress(false)

      Logger.i(TAG, "Loaded birthdays: ${allBirthdays.size}, can search: ${allBirthdays.isNotEmpty()}")

      withContext(dispatcherProvider.main()) {
        _canSearch.value = allBirthdays.isNotEmpty()
      }
    }
  }

  private suspend fun filterBirthdays() {
    val filtered = filterByQuery(allBirthdays, lastQuery)
    Logger.i(TAG, "Filtering birthdays by query '$lastQuery': ${filtered.size} items left.")

    val uiLists = filtered.map { uiBirthdayListAdapter.convert(it) }
      .sortedBy { it.nextBirthdayDateMillis }
    withContext(dispatcherProvider.main()) {
      _birthdays.value = uiLists
    }
  }

  private fun filterByQuery(birthdays: List<Birthday>, query: String): List<Birthday> {
    if (query.isBlank()) return birthdays
    return birthdays.filter(BirthdayQueryFilter(lastQuery)).also {
      Logger.i(TAG, "Filtered by query: ${it.size} items left, was: ${birthdays.size}. Query: ${Logger.private(query)}")
    }
  }

  companion object {
    private const val TAG = "BirthdaysViewModel"
  }
}
