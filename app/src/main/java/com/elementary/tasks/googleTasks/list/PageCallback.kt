package com.elementary.tasks.googleTasks.list

import com.elementary.tasks.core.data.models.GoogleTaskList

interface PageCallback {
    fun provideGoogleTasksLists(listener: ((List<GoogleTaskList>) -> Unit)?)
}