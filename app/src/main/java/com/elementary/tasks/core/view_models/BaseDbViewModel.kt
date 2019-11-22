package com.elementary.tasks.core.view_models

import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.launchDefault
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.inject

open class BaseDbViewModel : ViewModel(), LifecycleObserver, KoinComponent {

    protected val appDb: AppDb by inject()
    protected val prefs: Prefs by inject()

    private val _result = MutableLiveData<Commands>()
    val result: LiveData<Commands> = _result
    private val _isInProgress = MutableLiveData<Boolean>()
    val isInProgress: LiveData<Boolean> = _isInProgress
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

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
        launchDefault {
            postInProgress(true)
            runBlocking {
                doWork.invoke { postError(it) }
            }
            postInProgress(false)
        }
    }

    protected fun withResult(doWork: ((error: String) -> Unit) -> Commands) {
        launchDefault {
            postInProgress(true)
            val commands = runBlocking {
                doWork.invoke { postError(it) }
            }
            postInProgress(false)
            postCommand(commands)
        }
    }
}
