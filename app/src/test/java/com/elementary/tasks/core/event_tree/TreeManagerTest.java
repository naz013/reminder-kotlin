package com.elementary.tasks.core.event_tree;

import android.app.AlarmManager;

import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.reminder.models.Reminder;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Calendar;
import java.util.List;

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

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TreeManagerTest {

    private static final String TAG = "TreeManagerTest";
    private static final int NUM_OF_NODES = 100000;

    private static EventRoot root;
    private static Reminder reminder;

    @BeforeClass
    public static void setup() {
        root = new EventRoot();
    }

    public void addReminder(String eventTime, String summary) {
        Reminder reminder = new Reminder();
        reminder.setEventTime(eventTime).setSummary(summary);
        if (this.reminder == null) this.reminder = reminder;
        root.addNode(reminder);
    }

    @Test
    public void test() throws Exception {
        long time = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        for (int i = 0; i < NUM_OF_NODES; i++) {
            addReminder(TimeUtil.getGmtFromDateTime(calendar.getTimeInMillis()), "Node " + i);
            calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_FIFTEEN_MINUTES);
        }
        long procTime = System.currentTimeMillis() - time;
        System.out.println("addition_test: " + procTime);
        assertEquals(NUM_OF_NODES, root.size());
    }

    @Test
    public void test1() throws Exception {
        long time = System.currentTimeMillis();
        List<Object> list = root.getAll();
        long procTime = System.currentTimeMillis() - time;
        System.out.println("test1: " + procTime);
        assertNotEquals(null, list);
        System.out.println("test1: " + list.size());
    }

    @Test
    public void test2() throws Exception {
        long time = System.currentTimeMillis();
        List<Object> list = root.getNodes(2017, 9, 15, 17);
        long procTime = System.currentTimeMillis() - time;
        System.out.println("test2: " + procTime);
        assertNotEquals(null, list);
        System.out.println("test2: " + list.size());
    }

    @Test
    public void test3() throws Exception {
        long time = System.currentTimeMillis();
        List<Object> list = root.getNodes(2017, 9, 15);
        long procTime = System.currentTimeMillis() - time;
        System.out.println("test3: " + procTime);
        assertNotEquals(null, list);
        System.out.println("test3: " + list.size());
    }

    @Test
    public void test4() throws Exception {
        long time = System.currentTimeMillis();
        List<Object> list = root.getNodes(2017);
        long procTime = System.currentTimeMillis() - time;
        System.out.println("test4: " + procTime);
        assertNotEquals(null, list);
        System.out.println("test4: " + list.size());
    }

    @Test
    public void test5() throws Exception {
        long time = System.currentTimeMillis();
        List<Object> list = root.getNodes(2017, 9);
        long procTime = System.currentTimeMillis() - time;
        System.out.println("test5: " + procTime);
        assertNotEquals(null, list);
        System.out.println("test5: " + list.size());
    }

    @Test
    public void test6() throws Exception {
        assertNotEquals(null, reminder);
        long time = System.currentTimeMillis();
        root.remove(reminder.getUuId());
        long procTime = System.currentTimeMillis() - time;
        System.out.println("test6: " + procTime);
    }

    @Test
    public void test7() throws Exception {
        long time = System.currentTimeMillis();
        root.clearMonth(2017, 7);
        long procTime = System.currentTimeMillis() - time;
        System.out.println("test7: " + procTime);
    }
}
