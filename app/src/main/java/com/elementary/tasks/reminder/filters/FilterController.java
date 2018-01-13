package com.elementary.tasks.reminder.filters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.elementary.tasks.reminder.models.Reminder;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2017 Nazar Suhovich
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

public class FilterController {

    @NonNull
    private FilterValue<String> searchValue = new FilterValue<>();
    @NonNull
    private FilterValue<String> groupValue = new FilterValue<>();
    @NonNull
    private FilterValue<Integer> typeValue = new FilterValue<>();
    @NonNull
    private FilterValue<Integer> statusValue = new FilterValue<>();

    @NonNull
    private List<Reminder> original = new ArrayList<>();
    @Nullable
    private ReminderFilterCallback mCallback;
    @Nullable
    private ReminderFilter mFilter;

    public FilterController(@Nullable ReminderFilterCallback callback) {
        this.mCallback = callback;
        initFilters();
    }

    private void initFilters() {
        SearchFilter searchFilter = new SearchFilter(null);
        searchValue.subscribe(searchFilter);

        GroupFilter groupFilter = new GroupFilter(searchFilter);
        groupValue.subscribe(groupFilter);

        TypeFilter typeFilter = new TypeFilter(groupFilter);
        typeValue.subscribe(typeFilter);

        StatusFilter statusFilter = new StatusFilter(typeFilter);
        statusValue.subscribe(statusFilter);

        this.mFilter = statusFilter;
    }

    public void setSearchValue(@Nullable String value) {
        if (value == null) {
            searchValue.setValue("");
        } else {
            searchValue.setValue(value);
        }
        onChanged();
    }

    public void setGroupValue(@Nullable String value) {
        if (value == null) {
            groupValue.setValue("");
        } else {
            groupValue.setValue(value);
        }
        onChanged();
    }

    public void setTypeValue(int value) {
        typeValue.setValue(value);
        onChanged();
    }

    public void setStatusValue(int value) {
        statusValue.setValue(value);
        onChanged();
    }

    @NonNull
    public List<Reminder> getOriginal() {
        return original;
    }

    public void setOriginal(@NonNull List<Reminder> original) {
        this.original = original;
        onChanged();
    }

    private void onChanged() {
        List<Reminder> list = new ArrayList<>();
        for (Reminder reminder : original) {
            if (mFilter != null) {
                if (mFilter.filter(reminder)) list.add(reminder);
            } else list.add(reminder);
        }
        if (mCallback != null) mCallback.onChanged(list);
    }
}
