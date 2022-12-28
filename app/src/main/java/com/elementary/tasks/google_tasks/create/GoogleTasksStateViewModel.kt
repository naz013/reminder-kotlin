package com.elementary.tasks.google_tasks.create

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.data.models.GoogleTaskList

class GoogleTasksStateViewModel : ViewModel(), LifecycleObserver {

  var isEdited = false
  var listId: String = ""
  var action: String = ""

  var isLoading = false
  var editedTaskList: GoogleTaskList? = null
}
