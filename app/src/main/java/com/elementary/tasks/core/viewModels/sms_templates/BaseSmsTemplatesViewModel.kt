package com.elementary.tasks.core.viewModels.sms_templates

import android.app.Application

import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.viewModels.BaseDbViewModel
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.navigation.settings.additional.DeleteTemplateFilesAsync

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
internal abstract class BaseSmsTemplatesViewModel(application: Application) : BaseDbViewModel(application) {

    fun deleteSmsTemplate(smsTemplate: SmsTemplate) {
        isInProgress.postValue(true)
        run {
            appDb!!.smsTemplatesDao().delete(smsTemplate)
            end {
                isInProgress.postValue(false)
                result.postValue(Commands.DELETED)
            }
            DeleteTemplateFilesAsync(getApplication()).execute(smsTemplate.key)
        }
    }
}