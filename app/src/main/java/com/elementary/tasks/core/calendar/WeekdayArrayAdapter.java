package com.elementary.tasks.core.calendar;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

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

public class WeekdayArrayAdapter extends ArrayAdapter<String> {

    private boolean isDark;

    public WeekdayArrayAdapter(Context context, int textViewResourceId,
                               List<String> objects, boolean isDark) {
        super(context, textViewResourceId, objects);
        this.isDark = isDark;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView textView = (TextView) super.getView(position, convertView, parent);
        String item = getItem(position);
        if (item != null) {
            if (item.length() <= 2) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            } else {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            }
        }
        int textColor;
        if (isDark) {
            textColor = getContext().getResources().getColor(android.R.color.white);
        } else {
            textColor = getContext().getResources().getColor(android.R.color.black);
        }
        textView.setTextColor(textColor);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }
}
