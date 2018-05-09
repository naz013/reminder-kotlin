package com.elementary.tasks.birthdays;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

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
public class CalendarPagerAdapter extends FragmentPagerAdapter {

    private List<EventsPagerItem> datas = new ArrayList<>();

    public CalendarPagerAdapter(final FragmentManager fm, final List<EventsPagerItem> datas) {
        super(fm);
        this.datas.addAll(datas);
    }

    @Override
    public Fragment getItem(final int position) {
        return EventsListFragment.newInstance(datas.get(position));
    }

    @Override
    public int getCount() {
        return datas.size();
    }
}
