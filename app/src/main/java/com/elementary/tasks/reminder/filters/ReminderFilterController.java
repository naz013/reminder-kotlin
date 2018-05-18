package com.elementary.tasks.reminder.filters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.elementary.tasks.reminder.models.Reminder;

import java.util.ArrayList;
import java.util.Collections;
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
public class ReminderFilterController {

    @NonNull
    private FilterValue<String> searchValue = new FilterValue<>();
    @NonNull
    private FilterValue<List<String>> groupValue = new FilterValue<>();
    @NonNull
    private FilterValue<Integer> typeValue = new FilterValue<>();
    @NonNull
    private FilterValue<Integer> statusValue = new FilterValue<>();
    @NonNull
    private FilterValue<DateFilter.DateRange> rangeValue = new FilterValue<>();

    @NonNull
    private List<Reminder> original = new ArrayList<>();
    @Nullable
    private FilterCallback<Reminder> mCallback;
    @Nullable
    private ObjectFilter<Reminder> mFilter;

    public ReminderFilterController(@Nullable FilterCallback<Reminder> callback) {
        this.mCallback = callback;
        initFilters();
    }

    private void initFilters() {
        SearchFilter searchFilter = new SearchFilter(null);
        searchValue.subscribe(searchFilter);

        DateFilter dateFilter = new DateFilter(searchFilter);
        rangeValue.subscribe(dateFilter);

        GroupFilter groupFilter = new GroupFilter(dateFilter);
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
            groupValue.setValue(Collections.singletonList(""));
        } else {
            groupValue.setValue(Collections.singletonList(value));
        }
        onChanged();
    }

    public void setGroupValues(@NonNull List<String> value) {
        groupValue.setValue(value);
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

    public void setRangeValue(int value) {
        switch (value) {
            case 0:
                this.rangeValue.setValue(DateFilter.DateRange.ALL);
                break;
            case 1:
                this.rangeValue.setValue(DateFilter.DateRange.PERMANENT);
                break;
            case 2:
                this.rangeValue.setValue(DateFilter.DateRange.TODAY);
                break;
            case 3:
                this.rangeValue.setValue(DateFilter.DateRange.TOMORROW);
                break;
        }
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

    public void remove(@NonNull Reminder reminder) {
        if (original.contains(reminder)) {
            original.remove(reminder);
            onChanged();
        }
    }
}
