package com.elementary.tasks.core.calendar;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

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

public class MonthPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<DateGridFragment> fragments;

    public ArrayList<DateGridFragment> getFragments() {
        if (fragments == null) {
            fragments = new ArrayList<>();
            for (int i = 0; i < getCount(); i++) {
                fragments.add(new DateGridFragment());
            }
        }
        return fragments;
    }

    public void setFragments(ArrayList<DateGridFragment> fragments) {
        this.fragments = fragments;
    }

    public MonthPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return getFragments().get(position);
    }

    @Override
    public int getCount() {
        return FlextCalendarFragment.NUMBER_OF_PAGES;
    }
}
