package com.elementary.tasks.core.event_tree;

import com.elementary.tasks.birthdays.CheckBirthdaysAsync;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

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

public class TreeManager {

    private EventRoot root;

    public TreeManager() {
        root = new EventRoot();
    }

    public void addBirthdayEvent(String date, String name) {
        Date dt = null;
        try {
            dt = CheckBirthdaysAsync.DATE_FORMAT.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (dt != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dt);
            calendar.set(Calendar.HOUR_OF_DAY, 15);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.YEAR, 2017);
//            root.addNode(new BirthdayItem(name, date, null, 0, 0, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH)));
        }
    }

    public int getCount() {
        return root.size();
    }
}
