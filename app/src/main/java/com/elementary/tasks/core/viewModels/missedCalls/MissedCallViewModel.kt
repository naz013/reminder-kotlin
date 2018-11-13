package com.elementary.tasks.core.viewModels.missedCalls

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.services.EventJobService
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.viewModels.BaseDbViewModel
import com.elementary.tasks.core.viewModels.Commands

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
class MissedCallViewModel private constructor(application: Application, number: String) : BaseDbViewModel(application) {

    var missedCall: LiveData<MissedCall>

    init {
        missedCall = appDb.missedCallsDao().loadByNumber(number)
    }

    fun deleteMissedCall(missedCall: MissedCall) {
        postInProgress(true)
        launchDefault {
            appDb.missedCallsDao().delete(missedCall)
            EventJobService.cancelMissedCall(missedCall.number)
            withUIContext {
                postInProgress(false)
                Commands.DELETED.post()
            }
        }
    }

    class Factory(private val application: Application, private val number: String) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MissedCallViewModel(application, number) as T
        }
    }
}
