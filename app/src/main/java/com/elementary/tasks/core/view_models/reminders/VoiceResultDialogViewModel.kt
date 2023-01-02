package com.elementary.tasks.core.view_models.reminders

import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.adapter.UiReminderListAdapter
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.utils.DispatcherProvider
import kotlinx.coroutines.launch

class VoiceResultDialogViewModel(
  id: String,
  private val reminderDao: ReminderDao,
  private val uiReminderListAdapter: UiReminderListAdapter,
  dispatcherProvider: DispatcherProvider
) : BaseProgressViewModel(dispatcherProvider) {

  val reminder = Transformations.map(reminderDao.loadById(id)) {
    uiReminderListAdapter.create(it)
  }
  var hasSameInDb: Boolean = false

  fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderDao.getById(id)
      hasSameInDb = reminder != null
    }
  }
}
