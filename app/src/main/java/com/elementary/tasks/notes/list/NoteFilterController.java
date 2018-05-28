package com.elementary.tasks.notes.list;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.elementary.tasks.core.data.models.Note;
import com.elementary.tasks.reminder.lists.filters.AbstractFilter;
import com.elementary.tasks.reminder.lists.filters.FilterCallback;
import com.elementary.tasks.reminder.lists.filters.FilterValue;
import com.elementary.tasks.reminder.lists.filters.ObjectFilter;

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
public class NoteFilterController {

    @NonNull
    private FilterValue<String> searchValue = new FilterValue<>();

    @NonNull
    private List<Note> original = new ArrayList<>();
    @Nullable
    private FilterCallback<Note> mCallback;
    @Nullable
    private ObjectFilter<Note> mFilter;

    public NoteFilterController(@Nullable FilterCallback<Note> callback) {
        this.mCallback = callback;
        initFilters();
    }

    private void initFilters() {
        AbstractFilter<String, Note> filter = new AbstractFilter<String, Note>(null) {
            @Nullable
            private String query = null;

            @Override
            public boolean filter(Note item) {
                return (query == null || query.length() == 0 || item.getSummary().toLowerCase().contains(query.toLowerCase()));
            }

            @Override
            public void accept(String s) throws Exception {
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
    public List<Note> getOriginal() {
        return original;
    }

    public void setOriginal(@NonNull List<Note> original) {
        this.original = original;
        onChanged();
    }

    private void onChanged() {
        List<Note> list = new ArrayList<>();
        for (Note item : original) {
            if (mFilter != null) {
                if (mFilter.filter(item)) list.add(item);
            } else list.add(item);
        }
        if (mCallback != null) mCallback.onChanged(list);
    }
}
