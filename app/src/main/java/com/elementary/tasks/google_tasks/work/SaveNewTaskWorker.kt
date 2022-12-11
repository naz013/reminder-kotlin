package com.elementary.tasks.google_tasks.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchIo
import com.google.gson.Gson
import java.io.IOException

class SaveNewTaskWorker(
  context: Context,
  workerParams: WorkerParameters,
  private val gTasks: GTasks
) : Worker(context, workerParams) {

  override fun doWork(): Result {
    val json = inputData.getString(Constants.INTENT_JSON) ?: "{}"
    if (json.isNotEmpty()) {
      val googleTask = Gson().fromJson(json, GoogleTask::class.java)
      if (googleTask != null && gTasks.isLogged) {
        launchIo {
          try {
            gTasks.insertTask(googleTask)
          } catch (e: IOException) {
          }
        }
      }
    }
    return Result.success()
  }
}
