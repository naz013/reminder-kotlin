package com.elementary.tasks.google_tasks;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import java.util.List;
import java.util.Map;

/**
 * Copyright 2016 Nazar Suhovich
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

public class TaskPagerAdapter extends FragmentStatePagerAdapter {

    private List<TaskListWrapperItem> mData;
    private Map<String, Integer> colors;

    public TaskPagerAdapter(final FragmentManager fm, final List<TaskListWrapperItem> data, Map<String, Integer> colors) {
        super(fm);
        this.mData = data;
        this.colors = colors;
    }

    @Override
    public Fragment getItem(final int position) {
        TaskListFragment fragment = new TaskListFragment();
        fragment.setData(mData.get(position).getmData(), colors);
        return fragment;
    }

    @Override
    public int getCount() {
        return mData.size();
    }
}
