package com.elementary.tasks.core.view_models.day_view;

import android.app.Application;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.DayViewProvider;
import com.elementary.tasks.birthdays.EventsDataSingleton;
import com.elementary.tasks.birthdays.EventsItem;
import com.elementary.tasks.birthdays.EventsPagerItem;
import com.elementary.tasks.birthdays.work.DeleteBirthdayFilesAsync;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.data.models.Birthday;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.view_models.BaseDbViewModel;
import com.elementary.tasks.core.view_models.Commands;
import com.elementary.tasks.reminder.work.UpdateFilesAsync;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

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
public class DayViewViewModel extends BaseDbViewModel {

    private DayViewLiveData liveData = new DayViewLiveData();
    public LiveData<List<EventsItem>> events = liveData;
    @Nullable
    private EventsPagerItem item;

    public DayViewViewModel(Application application) {
        super(application);
    }

    public void setItem(@Nullable EventsPagerItem item) {
        this.item = item;
        liveData.update();
    }

    public void deleteBirthday(@NonNull Birthday birthday) {
        isInProgress.postValue(true);
        run(() -> {
            getAppDb().birthdaysDao().delete(birthday);
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.DELETED);
                liveData.update();
            });
            new DeleteBirthdayFilesAsync(getApplication()).execute(birthday.getUuId());
        });
    }

    public void moveToTrash(@NonNull Reminder reminder) {
        isInProgress.postValue(true);
        run(() -> {
            reminder.setRemoved(true);
            EventControlFactory.getController(reminder).stop();
            getAppDb().reminderDao().insert(reminder);
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.DELETED);
                Toast.makeText(getApplication(), R.string.deleted, Toast.LENGTH_SHORT).show();
                liveData.update();
            });
            new UpdateFilesAsync(getApplication()).execute(reminder);
        });
    }

    private class DayViewLiveData extends LiveData<List<EventsItem>> implements DayViewProvider.Callback, DayViewProvider.InitCallback {

        DayViewLiveData() {
            DayViewProvider provider = EventsDataSingleton.getInstance().getProvider();
            if (provider != null) {
                provider.addObserver(this);
            }
        }

        void update() {
            DayViewProvider provider = EventsDataSingleton.getInstance().getProvider();
            if (provider != null && item != null) {
                provider.findMatches(item.getDay(), item.getMonth(), item.getYear(), true, this);
            }
        }

        @Override
        public void apply(@NonNull List<EventsItem> list) {
            postValue(list);
        }

        @Override
        protected void onInactive() {
            super.onInactive();
            DayViewProvider provider = EventsDataSingleton.getInstance().getProvider();
            if (provider != null) {
                provider.removeCallback(this);
                provider.removeObserver(this);
            }
        }

        @Override
        public void onFinish() {
            update();
        }
    }
}
