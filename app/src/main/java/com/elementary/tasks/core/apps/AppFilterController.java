package com.elementary.tasks.core.apps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.elementary.tasks.reminder.filters.AbstractFilter;
import com.elementary.tasks.reminder.filters.FilterCallback;
import com.elementary.tasks.reminder.filters.FilterValue;
import com.elementary.tasks.reminder.filters.ObjectFilter;

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
public class AppFilterController {

    @NonNull
    private FilterValue<String> searchValue = new FilterValue<>();

    @NonNull
    private List<ApplicationItem> original = new ArrayList<>();
    @Nullable
    private FilterCallback<ApplicationItem> mCallback;
    @Nullable
    private ObjectFilter<ApplicationItem> mFilter;

    public AppFilterController(@Nullable FilterCallback<ApplicationItem> callback) {
        this.mCallback = callback;
        initFilters();
    }

    private void initFilters() {
        AbstractFilter<String, ApplicationItem> filter = new AbstractFilter<String, ApplicationItem>(null) {
            @Nullable
            private String query = null;

            @Override
            public boolean filter(ApplicationItem item) {
                if (query == null) return true;
                String text = item.getName().toLowerCase();
                return text.contains(query.toLowerCase());
            }

            @Override
            public void accept(String s) throws Exception {
                this.query = s;
            }
        };
        searchValue.subscribe(filter);
        this.mFilter = filter;
    }

    public void clearFilter() {
        setSearchValue("");
    }

    public void setSearchValue(@Nullable String value) {
        if (value == null) {
            searchValue.setValue("");
        } else {
            searchValue.setValue(value);
        }
        onChanged();
    }

    @NonNull
    public List<ApplicationItem> getOriginal() {
        return original;
    }

    public void setOriginal(@NonNull List<ApplicationItem> original) {
        this.original = original;
        onChanged();
    }

    private void onChanged() {
        List<ApplicationItem> list = new ArrayList<>();
        for (ApplicationItem item : original) {
            if (mFilter != null) {
                if (mFilter.filter(item)) list.add(item);
            } else list.add(item);
        }
        if (mCallback != null) mCallback.onChanged(list);
    }
}
