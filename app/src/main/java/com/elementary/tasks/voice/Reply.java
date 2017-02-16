package com.elementary.tasks.voice;

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

public class Reply {

    public static final int REPLY = 0;
    public static final int REMINDER = 1;
    public static final int NOTE = 2;
    public static final int PREFS = 3;
    public static final int GROUP = 4;
    public static final int RESPONSE = 5;
    public static final int SHOW_MORE = 6;
    public static final int BIRTHDAY = 7;
    public static final int SHOPPING = 8;

    private int viewType;
    private Object object;

    public Reply(int viewType, Object object) {
        this.viewType = viewType;
        this.object = object;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
