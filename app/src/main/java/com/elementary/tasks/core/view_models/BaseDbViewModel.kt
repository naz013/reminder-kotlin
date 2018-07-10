package com.elementary.tasks.core.view_models

import android.app.Application
import android.os.Handler
import android.os.Looper

import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.data.AppDb

import javax.inject.Inject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData

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

    var result = MutableLiveData<Commands>()
    var isInProgress = MutableLiveData<Boolean>()

    @Inject
    protected var appDb: AppDb? = null
        set
    protected val handler = Handler(Looper.getMainLooper())

    init {
        ReminderApp.appComponent!!.inject(this)
    }

    protected fun end(runnable: Runnable) {
        handler.post(runnable)
    }

    protected fun run(runnable: Runnable) {
        Thread(runnable).start()
    }
}
