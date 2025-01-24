package com.elementary.tasks.core.arch

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.Commands
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.logging.Logger
import kotlinx.coroutines.launch

open class BaseProgressViewModel(
  protected val dispatcherProvider: DispatcherProvider
) : ViewModel(), DefaultLifecycleObserver {

  private val _resultEvent = mutableLiveDataOf<Event<Commands>>()
  val resultEvent = _resultEvent.toLiveData()

  private val _isInProgress = mutableLiveDataOf<Boolean>()
  val isInProgress = _isInProgress.toLiveData()

  private val _errorEvent = mutableLiveDataOf<Event<String>>()
  val errorEvent = _errorEvent.toLiveData()

  protected fun postInProgress(isInProgress: Boolean) {
    Logger.d(TAG, "Post in progress: $isInProgress")
    _isInProgress.postValue(isInProgress)
  }

  protected fun postCommand(commands: Commands) {
    Logger.d(TAG, "Post command: $commands")
    _resultEvent.postValue(Event(commands))
  }

  protected fun postError(error: String) {
    Logger.e(TAG, "Post error: $error")
    _errorEvent.postValue(Event(error))
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

  companion object {
    private const val TAG = "BaseProgressViewModel"
  }
}
