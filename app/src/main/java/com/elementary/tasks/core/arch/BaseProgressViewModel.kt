package com.elementary.tasks.core.arch

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

open class BaseProgressViewModel(
  protected val dispatcherProvider: DispatcherProvider
) : ViewModel(), DefaultLifecycleObserver {

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

  protected fun withProgressSuspend(doWork: suspend ((error: String) -> Unit) -> Unit) {
    viewModelScope.launch(dispatcherProvider.default()) {
      postInProgress(true)
      doWork.invoke { postError(it) }
      postInProgress(false)
    }
  }

  protected fun withResultSuspend(doWork: suspend ((error: String) -> Unit) -> Commands) {
    viewModelScope.launch(dispatcherProvider.default()) {
      postInProgress(true)
      val commands = doWork.invoke { postError(it) }
      postInProgress(false)
      postCommand(commands)
    }
  }
}
