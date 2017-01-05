package com.elementary.tasks.core.event_tree;

import android.util.Log;

import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.reminder.models.Reminder;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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

public class TreeManagerTest {

    private static final String TAG = "TreeManagerTest";

    @Test
    public void addition_test() throws Exception {
        TreeManager manager = new TreeManager();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(2017, 0, 15, 12, 32);
        String evOne = "Event One";
        manager.addReminder(TimeUtil.getGmtFromDateTime(calendar.getTimeInMillis()), evOne);
        assertEquals(manager.getCount(), 1);
        String evTwo = "Event Two";
        calendar.set(Calendar.MINUTE, 35);
        manager.addReminder(TimeUtil.getGmtFromDateTime(calendar.getTimeInMillis()), evTwo);
        assertEquals(manager.getCount(), 2);
        EventInterface eventInterface = manager.getItem(2017, 0, 15, 12, 32);
        assertNotEquals(eventInterface, null);
        if (eventInterface instanceof Reminder) {
            Reminder reminder = (Reminder) eventInterface;
            Log.d(TAG, "addition_test: " + reminder.getSummary());
        }
    }
}
