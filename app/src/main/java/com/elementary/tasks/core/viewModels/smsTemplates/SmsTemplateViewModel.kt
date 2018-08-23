package com.elementary.tasks.core.viewModels.smsTemplates

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.places.work.SingleBackupWorker
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

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
class SmsTemplateViewModel private constructor(application: Application, key: String) : BaseSmsTemplatesViewModel(application) {

    var smsTemplate: LiveData<SmsTemplate>

    init {
        smsTemplate = appDb.smsTemplatesDao().loadByKey(key)
    }

    fun saveTemplate(smsTemplate: SmsTemplate) {
        isInProgress.postValue(true)
        launch(CommonPool) {
            appDb.smsTemplatesDao().insert(smsTemplate)
            withUIContext {
                isInProgress.postValue(false)
                result.postValue(Commands.SAVED)
            }
            val work = OneTimeWorkRequest.Builder(SingleBackupWorker::class.java)
                    .setInputData(Data.Builder().putString(Constants.INTENT_ID, smsTemplate.key).build())
                    .addTag(smsTemplate.key)
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }

    class Factory(private val application: Application, private val key: String) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SmsTemplateViewModel(application, key) as T
        }
    }
}
