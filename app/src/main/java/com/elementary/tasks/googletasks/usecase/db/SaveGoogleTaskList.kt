package com.elementary.tasks.googletasks.usecase.db

import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.core.data.models.GoogleTaskList

class SaveGoogleTaskList(
  private val googleTaskListsDao: GoogleTaskListsDao
) {

  operator fun invoke(googleTaskList: GoogleTaskList) {
    googleTaskListsDao.insert(googleTaskList)
  }
}
