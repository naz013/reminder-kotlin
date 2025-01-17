package com.elementary.tasks.core.arch

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.data.Commands
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.livedata.toSingleEvent
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import kotlinx.coroutines.launch

open class BaseProgressViewModel(
  protected val dispatcherProvider: DispatcherProvider
) : ViewModel(), DefaultLifecycleObserver {

  private val _result = mutableLiveDataOf<Commands>()
  val result = _result.toLiveData()

  private val _isInProgress = mutableLiveDataOf<Boolean>()
  val isInProgress = _isInProgress.toLiveData()

  private val _error = mutableLiveDataOf<String>()
  val error = _error.toSingleEvent()

  protected fun postInProgress(isInProgress: Boolean) {
    _isInProgress.postValue(isInProgress)
  }

  protected fun postCommand(commands: Commands) {
    _result.postValue(commands)
  }

  protected fun postError(error: String) {
    _error.postValue(error)
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
