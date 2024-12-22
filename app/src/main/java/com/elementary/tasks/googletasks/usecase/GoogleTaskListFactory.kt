package com.elementary.tasks.googletasks.usecase

import com.github.naz013.domain.GoogleTaskList

class GoogleTaskListFactory {

  fun update(googleTaskList: GoogleTaskList, newGoogleTaskList: GoogleTaskList): GoogleTaskList {
    return googleTaskList.copy(
      title = newGoogleTaskList.title,
      listId = newGoogleTaskList.listId,
      eTag = newGoogleTaskList.eTag,
      kind = newGoogleTaskList.kind,
      selfLink = newGoogleTaskList.selfLink,
      updated = newGoogleTaskList.updated,
      uploaded = true
    )
  }
}
