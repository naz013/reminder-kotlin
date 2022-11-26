package com.elementary.tasks.core.view_models

import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

open class BaseDbViewModel(
  protected val appDb: AppDb,
  protected val prefs: Prefs,
  protected val dispatcherProvider: DispatcherProvider
) : ViewModel(), LifecycleObserver {

  private val _result = mutableLiveDataOf<Commands>()
  val result = _result.toLiveData()
  private val _isInProgress = mutableLiveDataOf<Boolean>()
  val isInProgress = _isInProgress.toLiveData()
  private val _error = mutableLiveDataOf<String>()
  val error = _error.toLiveData()

  protected fun postInProgress(isInProgress: Boolean) {
    _isInProgress.postValue(isInProgress)
  }

  protected fun postCommand(commands: Commands) {
    _result.postValue(commands)
  }

  protected fun postError(error: String) {
    _error.postValue(error)
  }

  protected fun startWork(clazz: Class<out Worker>, key: String, valueTag: String, context: Context? = null) {
    startWork(clazz, Data.Builder().putString(key, valueTag).build(), valueTag, context)
  }

  protected fun startWork(clazz: Class<out Worker>, data: Data, tag: String, context: Context? = null) {
    if (prefs.isBackupEnabled) {
      val work = OneTimeWorkRequest.Builder(clazz)
        .setInputData(data)
        .addTag(tag)
        .build()
      if (context != null) {
        WorkManager.getInstance(context).enqueue(work)
      } else {
        WorkManager.getInstance().enqueue(work)
      }
    }
  }

  protected fun withProgress(doWork: ((error: String) -> Unit) -> Unit) {
    viewModelScope.launch(dispatcherProvider.default()) {
      postInProgress(true)
      runBlocking {
        doWork.invoke { postError(it) }
      }
      postInProgress(false)
    }
  }

  protected fun withResult(doWork: ((error: String) -> Unit) -> Commands) {
    viewModelScope.launch(dispatcherProvider.default()) {
      postInProgress(true)
      val commands = runBlocking {
        doWork.invoke { postError(it) }
      }
      postInProgress(false)
      postCommand(commands)
    }
  }
}
