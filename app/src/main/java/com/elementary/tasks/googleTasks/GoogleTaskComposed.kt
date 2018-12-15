package com.elementary.tasks.googleTasks

import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList

data class GoogleTaskComposed(
        var googleTasks: List<GoogleTask> = listOf(),
        var googleList: GoogleTaskList? = null,
        var listId: String = ""
)