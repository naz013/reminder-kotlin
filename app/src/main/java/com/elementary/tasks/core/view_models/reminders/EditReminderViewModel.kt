package com.elementary.tasks.core.view_models.reminders

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.view_models.DispatcherProvider
import kotlinx.coroutines.launch

class EditReminderViewModel(
  id: String,
  appDb: AppDb,
  googleCalendarUtils: GoogleCalendarUtils,
  eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  workerLauncher: WorkerLauncher,
  updatesHelper: UpdatesHelper
) : BaseRemindersViewModel(
  googleCalendarUtils,
  eventControlFactory,
  dispatcherProvider,
  workerLauncher,
  updatesHelper,
  appDb.reminderDao(),
  appDb.reminderGroupDao(),
  appDb.placesDao()
) {

  private val _note = mutableLiveDataOf<NoteWithImages>()
  val note = _note.toLiveData()

  private val _googleTask = mutableLiveDataOf<Pair<GoogleTaskList?, GoogleTask?>>()
  val googleTask = _googleTask.toLiveData()

  val reminder = reminderDao.loadById(id)

  val db = appDb
  var hasSameInDb: Boolean = false

  fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderDao.getById(id)
      hasSameInDb = reminder != null
    }
  }
}
