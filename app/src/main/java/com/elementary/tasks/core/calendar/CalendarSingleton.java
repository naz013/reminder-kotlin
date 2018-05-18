package com.elementary.tasks.core.calendar;

import androidx.annotation.Nullable;
import android.view.View;

import com.elementary.tasks.birthdays.EventsDataProvider;

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

public class CalendarSingleton {

    private static CalendarSingleton instance;
    @Nullable
    private EventsDataProvider provider;
    private View.OnClickListener fabClick;

    private CalendarSingleton() {
    }

    public static CalendarSingleton getInstance() {
        if (instance == null) {
            synchronized (CalendarSingleton.class) {
                if (instance == null) {
                    instance = new CalendarSingleton();
                }
            }
        }
        return instance;
    }

    public void setFabClick(View.OnClickListener fabClick) {
        this.fabClick = fabClick;
    }

    public View.OnClickListener getFabClick() {
        return fabClick;
    }

    public void setProvider(@Nullable EventsDataProvider provider) {
        this.provider = provider;
    }

    @Nullable
    public EventsDataProvider getProvider() {
        return provider;
    }
}
