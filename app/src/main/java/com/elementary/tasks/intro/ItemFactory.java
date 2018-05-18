package com.elementary.tasks.intro;

import android.content.Context;
import androidx.annotation.DrawableRes;

import com.elementary.tasks.R;

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

class ItemFactory {

    public static IntroItem getItem(Context context, int position) {
        switch (position) {
            case 0:
                return createItem(context.getString(R.string.reminder), context.getString(R.string.reminder_descr), R.drawable.ic_bell_illustration);
            case 1:
                return createItem(context.getString(R.string.notes_support), context.getString(R.string.notes_descr), R.drawable.ic_note_ill);
            case 2:
                return createItem(context.getString(R.string.google_integration), context.getString(R.string.google_descr), R.drawable.ic_search_ill);
            case 3:
                return createItem(context.getString(R.string.location_events), context.getString(R.string.location_descr), R.drawable.ic_map_ill);
            case 4:
                return createItem(context.getString(R.string.sync), context.getString(R.string.sync_descr), R.drawable.ic_drive_ill, R.drawable.ic_dropbox_ill);
        }
        return null;
    }

    private static IntroItem createItem(String string, String description, @DrawableRes int... icons) {
        return new IntroItem(string, description, icons);
    }
}
