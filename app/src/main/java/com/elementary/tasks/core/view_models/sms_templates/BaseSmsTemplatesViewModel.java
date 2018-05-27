package com.elementary.tasks.core.view_models.sms_templates;

import android.app.Application;

import com.elementary.tasks.core.data.models.SmsTemplate;
import com.elementary.tasks.core.view_models.BaseDbViewModel;
import com.elementary.tasks.core.view_models.Commands;
import com.elementary.tasks.navigation.settings.additional.DeleteTemplateFilesAsync;

import androidx.annotation.NonNull;

/**
 * Copyright 2018 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
abstract class BaseSmsTemplatesViewModel extends BaseDbViewModel {

    BaseSmsTemplatesViewModel(Application application) {
        super(application);
    }

    public void deleteSmsTemplate(@NonNull SmsTemplate smsTemplate) {
        isInProgress.postValue(true);
        run(() -> {
            getAppDb().smsTemplatesDao().delete(smsTemplate);
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.DELETED);
            });
            new DeleteTemplateFilesAsync(getApplication()).execute(smsTemplate.getKey());
        });
    }
}
