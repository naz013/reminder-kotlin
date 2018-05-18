package com.elementary.tasks.reminder.filters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
public class GroupFilter extends AbstractFilter<List<String>, Reminder> {

    @NonNull
    private List<String> groupIds = new ArrayList<>();

    public GroupFilter(@Nullable ObjectFilter<Reminder> filter) {
        super(filter);
    }

    @Override
    public boolean filter(Reminder reminder) {
        if (!super.filter(reminder)) return false;
        if (groupIds.isEmpty()) return true;
        for (String s : groupIds) {
            if (reminder.getGroupUuId().equals(s)) return true;
        }
        return false;
    }

    @Override
    public void accept(List<String> s) throws Exception {
        this.groupIds = s;
    }
}
