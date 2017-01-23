package com.elementary.tasks.core.event_tree;

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
    private static final int NUM_OF_NODES = 10000;

    private static EventRoot root;

    @BeforeClass
    public static void setup() {
        root = new EventRoot();
        long time = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        for (int i = 0; i < NUM_OF_NODES; i++) {
            Reminder reminder = new Reminder();
            reminder.setEventTime(TimeUtil.getGmtFromDateTime(calendar.getTimeInMillis())).setSummary("Node " + i);
            calendar.setTimeInMillis(calendar.getTimeInMillis() + 1000);
            root.addNode(reminder);
        }
        long procTime = System.currentTimeMillis() - time;
        System.out.println("setup: " + procTime);
    }

    @Test
    public void print() throws Exception {
        long time = System.currentTimeMillis();
        root.print();
        long procTime = System.currentTimeMillis() - time;
        System.out.println("print_test: " + procTime);
        assertEquals(NUM_OF_NODES, root.size());
    }

    @Test
    public void test1() throws Exception {
        long time = System.currentTimeMillis();
        List<Object> list = root.getAll();
        long procTime = System.currentTimeMillis() - time;
        System.out.println("getAll: " + procTime);
        assertNotEquals(null, list);
        System.out.println("getAll: " + list.size());
    }

    @Test
    public void test4() throws Exception {
        long time = System.currentTimeMillis();
        List<Object> list = root.getNodes(2017);
        long procTime = System.currentTimeMillis() - time;
        System.out.println("get by year: " + procTime);
        assertNotEquals(null, list);
    }

    @Test
    public void test7() throws Exception {
        long time = System.currentTimeMillis();
        root.clearMonth(2017, 7);
        long procTime = System.currentTimeMillis() - time;
        System.out.println("clear month: " + procTime);
    }
}
