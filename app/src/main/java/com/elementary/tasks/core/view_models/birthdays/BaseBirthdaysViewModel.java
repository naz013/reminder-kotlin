package com.elementary.tasks.core.view_models.birthdays;

import android.app.Application;

import com.elementary.tasks.birthdays.work.DeleteBirthdayFilesAsync;
import com.elementary.tasks.core.data.models.Birthday;
import com.elementary.tasks.core.view_models.BaseDbViewModel;
import com.elementary.tasks.core.view_models.Commands;

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
abstract class BaseBirthdaysViewModel extends BaseDbViewModel {

    BaseBirthdaysViewModel(Application application) {
        super(application);
    }

    public void deleteBirthday(@NonNull Birthday birthday) {
        isInProgress.postValue(true);
        run(() -> {
            getAppDb().birthdaysDao().delete(birthday);
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.DELETED);
            });
            new DeleteBirthdayFilesAsync(getApplication()).execute(birthday.getUuId());
        });
    }

    public void saveBirthday(@NonNull Birthday birthday) {
        isInProgress.postValue(true);
        run(() -> {
            getAppDb().birthdaysDao().insert(birthday);
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.SAVED);
            });
        });
    }
}
