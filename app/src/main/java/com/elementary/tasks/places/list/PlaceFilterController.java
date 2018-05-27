package com.elementary.tasks.places.list;

import com.elementary.tasks.core.data.models.Place;
import com.elementary.tasks.reminder.lists.filters.AbstractFilter;
import com.elementary.tasks.reminder.lists.filters.FilterCallback;
import com.elementary.tasks.reminder.lists.filters.FilterValue;
import com.elementary.tasks.reminder.lists.filters.ObjectFilter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
public class PlaceFilterController {

    @NonNull
    private FilterValue<String> searchValue = new FilterValue<>();

    @NonNull
    private List<Place> original = new ArrayList<>();
    @Nullable
    private FilterCallback<Place> mCallback;
    @Nullable
    private ObjectFilter<Place> mFilter;

    public PlaceFilterController(@Nullable FilterCallback<Place> callback) {
        this.mCallback = callback;
        initFilters();
    }

    private void initFilters() {
        AbstractFilter<String, Place> filter = new AbstractFilter<String, Place>(null) {
            @Nullable
            private String query = null;

            @Override
            public boolean filter(Place item) {
                return (query == null || query.length() == 0 || item.getName().toLowerCase().contains(query.toLowerCase()));
            }

            @Override
            public void accept(String s) {
                this.query = s;
            }
        };
        searchValue.subscribe(filter);
        this.mFilter = filter;
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
    public List<Place> getOriginal() {
        return original;
    }

    public void setOriginal(@NonNull List<Place> original) {
        this.original = original;
        onChanged();
    }

    private void onChanged() {
        List<Place> list = new ArrayList<>();
        for (Place item : original) {
            if (mFilter != null) {
                if (mFilter.filter(item)) list.add(item);
            } else list.add(item);
        }
        if (mCallback != null) mCallback.onChanged(list);
    }
}
