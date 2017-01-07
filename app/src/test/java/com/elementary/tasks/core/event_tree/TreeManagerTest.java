package com.elementary.tasks.core.event_tree;

import android.app.AlarmManager;

import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.reminder.models.Reminder;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
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
    private static final int NUM_OF_NODES = 1000000;

    private static EventRoot root;
    private static List<Reminder> list = new ArrayList<>();
    private static String testUid;

    @BeforeClass
    public static void setup() {
        root = new EventRoot();
        long time = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        for (int i = 0; i < NUM_OF_NODES; i++) {
            Reminder reminder = new Reminder();
            reminder.setEventTime(TimeUtil.getGmtFromDateTime(calendar.getTimeInMillis())).setSummary("Node " + i);
            list.add(reminder);
            calendar.setTimeInMillis(calendar.getTimeInMillis() + 1000);
        }
        long procTime = System.currentTimeMillis() - time;
        System.out.println("setup: " + procTime);
    }

    @Test
    public void test() throws Exception {
        long time = System.currentTimeMillis();
        for (int i = 0; i < NUM_OF_NODES; i++) {
            root.addNode(list.get(i));
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
        System.out.println("getAll: " + procTime);
        assertNotEquals(null, list);
        System.out.println("getAll: " + list.size());
    }

    @Test
    public void test2() throws Exception {
        long time = System.currentTimeMillis();
        List<Object> list = root.getNodes(2017, 9, 15, 17);
        long procTime = System.currentTimeMillis() - time;
        System.out.println("get by hour: " + procTime);
//        assertNotEquals(null, list);
//        System.out.println("get by hour: " + list.size());
    }

    @Test
    public void test3() throws Exception {
        long time = System.currentTimeMillis();
        List<Object> list = root.getNodes(2017, 9, 15);
        long procTime = System.currentTimeMillis() - time;
        System.out.println("get by day: " + procTime);
//        assertNotEquals(null, list);
//        System.out.println("get by day: " + list.size());
    }

    @Test
    public void test4() throws Exception {
        long time = System.currentTimeMillis();
        List<Object> list = root.getNodes(2017);
        long procTime = System.currentTimeMillis() - time;
        System.out.println("get by year: " + procTime);
        assertNotEquals(null, list);
//        System.out.println("get by year: " + list.size());
    }

    @Test
    public void test5() throws Exception {
        long time = System.currentTimeMillis();
        List<Object> list = root.getNodes(2017, 9);
        long procTime = System.currentTimeMillis() - time;
        System.out.println("get by month: " + procTime);
//        assertNotEquals(null, list);
//        System.out.println("get by month: " + list.size());
    }

    @Test
    public void test6() throws Exception {
        assertNotEquals(null, list.get(0));
        long time = System.currentTimeMillis();
        root.remove(list.get(0).getUuId());
        long procTime = System.currentTimeMillis() - time;
        System.out.println("remove: " + procTime);
    }

    @Test
    public void test7() throws Exception {
        long time = System.currentTimeMillis();
        root.clearMonth(2017, 7);
        long procTime = System.currentTimeMillis() - time;
        System.out.println("clear month: " + procTime);
    }

    @Test
    public void test8() throws Exception {
        long time = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.set(2017, 6, 25, 10, 20);
        Reminder reminder = new Reminder();
        testUid = reminder.getUuId();
        for (int i = 0; i < 1000; i++) {
            reminder.setEventTime(TimeUtil.getGmtFromDateTime(calendar.getTimeInMillis())).setSummary("Node t " + i);
            root.addNode(reminder);
            calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_FIFTEEN_MINUTES);
        }
        long procTime = System.currentTimeMillis() - time;
        System.out.println("add multiple with same id: " + procTime);
    }

    @Test
    public void test9() throws Exception {
        assertNotEquals(null, testUid);
        long time = System.currentTimeMillis();
        root.remove(testUid);
        long procTime = System.currentTimeMillis() - time;
        System.out.println("remove multiple instances: " + procTime);
    }
}
