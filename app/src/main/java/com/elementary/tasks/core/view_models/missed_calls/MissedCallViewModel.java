package com.elementary.tasks.core.view_models.missed_calls;

import android.app.Application;

import com.elementary.tasks.core.data.models.MissedCall;
import com.elementary.tasks.core.services.EventJobService;
import com.elementary.tasks.core.view_models.BaseDbViewModel;
import com.elementary.tasks.core.view_models.Commands;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

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
public class MissedCallViewModel extends BaseDbViewModel {

    public LiveData<MissedCall> missedCall;

    private MissedCallViewModel(Application application, String number) {
        super(application);
        missedCall = getAppDb().missedCallsDao().loadByNumber(number);
    }

    public void deleteMissedCall(@NonNull MissedCall missedCall) {
        isInProgress.postValue(true);
        run(() -> {
            getAppDb().missedCallsDao().delete(missedCall);
            EventJobService.cancelMissedCall(missedCall.getNumber());
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.DELETED);
            });
        });
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private Application application;
        private String number;

        public Factory(Application application, String number) {
            this.application = application;
            this.number = number;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new MissedCallViewModel(application, number);
        }
    }
}
