package com.elementary.tasks.core.view_models

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
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

/**
 * Copyright 2018 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
open class BaseDbViewModel : ViewModel(), LifecycleObserver, KoinComponent {

    private val _result = MutableLiveData<Commands>()
    val result: LiveData<Commands> = _result
    private val _isInProgress = MutableLiveData<Boolean>()
    val isInProgress: LiveData<Boolean> = _isInProgress
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    protected val appDb: AppDb by inject()
    protected val prefs: Prefs by inject()

    protected fun postInProgress(isInProgress: Boolean) {
        _isInProgress.postValue(isInProgress)
    }

    protected fun postCommand(commands: Commands) {
        _result.postValue(commands)
    }

    protected fun postError(error: String) {
        _error.postValue(error)
    }

    protected fun startWork(clazz: Class<out Worker>, key: String, valueTag: String) {
        startWork(clazz, Data.Builder().putString(key, valueTag).build(), valueTag)
    }

    protected fun startWork(clazz: Class<out Worker>, data: Data, tag: String) {
        if (prefs.isBackupEnabled) {
            val work = OneTimeWorkRequest.Builder(clazz)
                    .setInputData(data)
                    .addTag(tag)
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }
}
