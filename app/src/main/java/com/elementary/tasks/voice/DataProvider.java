package com.elementary.tasks.voice;

import com.elementary.tasks.birthdays.BirthdayItem;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.notes.NoteItem;
import com.elementary.tasks.reminder.models.Reminder;

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

class DataProvider {

    private DataProvider() {}

    static List<GroupItem> getGroups() {
        return RealmDb.getInstance().getAllGroups();
    }

    static List<NoteItem> getNotes() {
        return RealmDb.getInstance().getAllNotes(null);
    }

    static List<Reminder> getShoppingReminders() {
        List<Reminder> list = new LinkedList<>(RealmDb.getInstance().getActiveReminders());
        for (int i = list.size() - 1; i >= 0; i--) {
            if (!Reminder.isSame(list.get(i).getType(), Reminder.BY_DATE_SHOP)) {
                list.remove(i);
            }
        }
        return list;
    }

    static List<Reminder> getActiveReminders(long dateTime) {
        List<Reminder> list = new LinkedList<>(RealmDb.getInstance().getEnabledReminders());
        if (dateTime == 0) return list;
        for (int i = list.size() - 1; i >= 0; i--) {
            Reminder reminder = list.get(i);
            if (reminder.getDateTime() != 0 && (reminder.getDateTime() > dateTime || reminder.getDateTime() < System.currentTimeMillis())) {
                list.remove(i);
            }
        }
        return list;
    }

    static List<Reminder> getReminders(long dateTime) {
        List<Reminder> list = new LinkedList<>(RealmDb.getInstance().getActiveReminders());
        if (dateTime == 0) return list;
        for (int i = list.size() - 1; i >= 0; i--) {
            Reminder reminder = list.get(i);
            if (reminder.getDateTime() != 0 && (reminder.getDateTime() > dateTime || reminder.getDateTime() < System.currentTimeMillis())) {
                list.remove(i);
            }
        }
        return list;
    }

    static List<BirthdayItem> getBirthdays(long dateTime, long time) {
        List<BirthdayItem> list = new LinkedList<>(RealmDb.getInstance().getAllBirthdays());
        if (dateTime == 0) return list;
        for (int i = list.size() - 1; i >= 0; i--) {
            long itemTime = list.get(i).getDateTime(time);
            if (itemTime < System.currentTimeMillis() || itemTime > dateTime) {
                list.remove(i);
            }
        }
        return list;
    }
}
