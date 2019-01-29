package com.elementary.tasks.google_tasks.list

import com.elementary.tasks.core.data.models.GoogleTaskList

interface PageCallback {
    fun provideGoogleTasksLists(listener: ((List<GoogleTaskList>) -> Unit)?)
}