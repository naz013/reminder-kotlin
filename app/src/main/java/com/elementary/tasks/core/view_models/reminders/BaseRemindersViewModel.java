package com.elementary.tasks.core.view_models.reminders;

import android.app.Application;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.data.models.Group;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.utils.CalendarUtils;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.view_models.BaseDbViewModel;
import com.elementary.tasks.core.view_models.Commands;
import com.elementary.tasks.reminder.work.DeleteFilesAsync;
import com.elementary.tasks.reminder.work.UpdateFilesAsync;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

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
public abstract class BaseRemindersViewModel extends BaseDbViewModel {

    public LiveData<Group> defaultGroup;
    public LiveData<List<Group>> allGroups;

    BaseRemindersViewModel(Application application) {
        super(application);

        defaultGroup = getAppDb().groupDao().loadDefault();
        allGroups = getAppDb().groupDao().loadAll();
    }

    public List<String> getAllGroupsNames() {
        List<String> list = new ArrayList<>();
        List<Group> groups = allGroups.getValue();
        if (groups != null) {
            list.addAll(groups.stream().map(Group::getTitle).collect(Collectors.toList()));
        }
        return list;
    }

    public void saveAndStartReminder(@NonNull Reminder reminder) {
        isInProgress.postValue(true);
        run(() -> {
            getAppDb().reminderDao().insert(reminder);
            EventControlFactory.getController(reminder).start();
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.SAVED);
            });
            new UpdateFilesAsync(getApplication()).execute(reminder);
        });
    }

    public void copyReminder(@NonNull Reminder reminder, long time, @Nullable String name) {
        if (reminder == null) return;
        isInProgress.postValue(true);
        run(() -> {
            Reminder newItem = reminder.copy();
            newItem.setSummary(name);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.setTimeInMillis(time);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            calendar.setTimeInMillis(TimeUtil.getDateTimeFromGmt(newItem.getEventTime()));
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            while (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            newItem.setEventTime(TimeUtil.getGmtFromDateTime(calendar.getTimeInMillis()));
            newItem.setStartTime(TimeUtil.getGmtFromDateTime(calendar.getTimeInMillis()));
            getAppDb().reminderDao().insert(newItem);
            EventControlFactory.getController(newItem).start();
            end(() -> Toast.makeText(getApplication(), R.string.reminder_created, Toast.LENGTH_SHORT).show());
        });
    }

    public void pauseReminder(@NonNull Reminder reminder) {
        isInProgress.postValue(true);
        run(() -> {
            EventControlFactory.getController(reminder).pause();
            end(() -> isInProgress.postValue(false));
        });
    }

    public void resumeReminder(@NonNull Reminder reminder) {
        isInProgress.postValue(true);
        run(() -> {
            EventControlFactory.getController(reminder).resume();
            end(() -> isInProgress.postValue(false));
        });
    }

    public void toggleReminder(@NonNull Reminder reminder) {
        if (reminder == null) return;
        isInProgress.postValue(true);
        run(() -> {
            if (!EventControlFactory.getController(reminder).onOff()) {
                end(() -> {
                    isInProgress.postValue(false);
                    Toast.makeText(getApplication(), R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show();
                });
            } else {
                end(() -> {
                    isInProgress.postValue(false);
                    result.postValue(Commands.SAVED);
                });
                new UpdateFilesAsync(getApplication()).execute(reminder);
            }
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
            });
            new UpdateFilesAsync(getApplication()).execute(reminder);
        });
    }

    public void changeGroup(@NonNull Reminder reminder, @NonNull String groupId) {
        isInProgress.postValue(true);
        run(() -> {
            reminder.setGroupUuId(groupId);
            getAppDb().reminderDao().insert(reminder);
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.SAVED);
            });
            new UpdateFilesAsync(getApplication()).execute(reminder);
        });
    }

    public void deleteReminder(@NonNull Reminder reminder) {
        isInProgress.postValue(true);
        run(() -> {
            EventControlFactory.getController(reminder).stop();
            getAppDb().reminderDao().delete(reminder);
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.DELETED);
                Toast.makeText(getApplication(), R.string.deleted, Toast.LENGTH_SHORT).show();
            });
            CalendarUtils.deleteEvents(getApplication(), reminder.getUniqueId());
            new DeleteFilesAsync(getApplication()).execute(reminder.getUuId());
        });
    }

    public void saveReminder(@NonNull Reminder reminder) {
        isInProgress.postValue(true);
        run(() -> {
            getAppDb().reminderDao().insert(reminder);
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.SAVED);
            });
            new UpdateFilesAsync(getApplication()).execute(reminder);
        });
    }
}
