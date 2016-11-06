package com.elementary.tasks.navigation.settings.calendar;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Prefs;

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

public class FragmentRemindersColor extends FragmentStyle {
    @Override
    protected int getSelectedColor() {
        return Prefs.getInstance(mContext).getReminderColor();
    }

    @Override
    protected void saveToPrefs(int code) {
        Prefs.getInstance(mContext).setReminderColor(code);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.reminders_color));
            mCallback.onFragmentSelect(this);
        }
    }
}
