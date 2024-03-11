package com.elementary.tasks.voice

import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.adapter.UiReminderListAdapter
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.mapNullable

class VoiceResultDialogViewModel(
  id: String,
  private val reminderDao: ReminderDao,
  private val uiReminderListAdapter: UiReminderListAdapter,
  dispatcherProvider: DispatcherProvider
) : BaseProgressViewModel(dispatcherProvider) {

  val reminder = reminderDao.loadById(id).mapNullable {
    uiReminderListAdapter.create(it)
  }
  var hasSameInDb: Boolean = false
}
