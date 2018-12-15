package com.elementary.tasks.googleTasks.list

import com.elementary.tasks.googleTasks.GoogleTaskComposed

interface PageCallback {
    fun find(listId: String, listener: ((String, GoogleTaskComposed) -> Unit)?)
}