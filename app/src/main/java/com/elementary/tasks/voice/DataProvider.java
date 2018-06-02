package com.elementary.tasks.voice;

import com.elementary.tasks.birthdays.BirthdayItem;

import java.util.ArrayList;
import java.util.LinkedList;
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

final class DataProvider {

    private DataProvider() {
    }

    static List<BirthdayItem> getBirthdays(long dateTime, long time) {
        List<BirthdayItem> list = new LinkedList<>(new ArrayList<>());
        if (dateTime == 0) {
            return list;
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            long itemTime = list.get(i).getDateTime(time);
            if (itemTime < System.currentTimeMillis() || itemTime > dateTime) {
                list.remove(i);
            }
        }
        return list;
    }
}
