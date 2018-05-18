package com.elementary.tasks.birthdays;

import androidx.annotation.Nullable;

/**
 * Copyright 2018 Nazar Suhovich
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
public class EventsDataSingleton {

    private static EventsDataSingleton instance;
    @Nullable
    private DayViewProvider provider;

    private EventsDataSingleton() {
    }

    public static EventsDataSingleton getInstance() {
        if (instance == null) {
            synchronized (EventsDataSingleton.class) {
                if (instance == null) {
                    instance = new EventsDataSingleton();
                }
            }
        }
        return instance;
    }

    public void setProvider(@Nullable DayViewProvider provider) {
        this.provider = provider;
    }

    @Nullable
    public DayViewProvider getProvider() {
        return provider;
    }

    public void setChanged() {
        if (provider != null) provider.setDataChanged(true);
    }
}
