package com.elementary.tasks.core.viewModels

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.data.AppDb
import javax.inject.Inject

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
open class BaseDbViewModel(application: Application) : AndroidViewModel(application), LifecycleObserver {

    private val _result = MutableLiveData<Commands>()
    val result: LiveData<Commands> = _result
    private val _isInProgress = MutableLiveData<Boolean>()
    val isInProgress: LiveData<Boolean> = _isInProgress

    @Inject
    lateinit var appDb: AppDb
    @Inject
    lateinit var updatesHelper: UpdatesHelper
    protected val handler = Handler(Looper.getMainLooper())

    init {
        ReminderApp.appComponent.inject(this)
    }

    protected fun postInProgress(isInProgress: Boolean) {
        _isInProgress.postValue(isInProgress)
    }

    protected fun postCommand(commands: Commands) {
        _result.postValue(commands)
    }

    protected fun startWork(clazz: Class<out Worker>, key: String, valueTag: String) {
        startWork(clazz, Data.Builder().putString(key, valueTag).build(), valueTag)
    }

    protected fun startWork(clazz: Class<out Worker>, data: Data, tag: String) {
        val work = OneTimeWorkRequest.Builder(clazz)
                .setInputData(data)
                .addTag(tag)
                .build()
        WorkManager.getInstance().enqueue(work)
    }
}
