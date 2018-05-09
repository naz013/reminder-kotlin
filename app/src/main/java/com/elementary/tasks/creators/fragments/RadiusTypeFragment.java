package com.elementary.tasks.creators.fragments;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.Locale;

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
abstract class RadiusTypeFragment extends TypeFragment {

    protected int radius = Prefs.getInstance(getContext()).getRadius();

    protected final void showRadiusPickerDialog() {
        Dialogues.showRadiusDialog(getContext(), radius, new Dialogues.OnValueSelectedListener<Integer>() {
            @Override
            public void onSelected(Integer integer) {
                radius = integer - 1;
                recreateMarker();
            }

            @Override
            public String getTitle(Integer integer) {
                return getTitleString(integer);
            }
        });
    }

    private String getTitleString(int progress) {
        if (progress == 0) {
            return getString(R.string.default_string);
        } else {
            return String.format(Locale.getDefault(), getString(R.string.radius_x_meters), String.valueOf(progress - 1));
        }
    }

    protected abstract void recreateMarker();

    @Override
    public Reminder prepare() {
        if (!SuperUtil.checkLocationEnable(getContext())) {
            SuperUtil.showLocationAlert(getContext(), getInterface());
            return null;
        }
        return new Reminder();
    }
}
