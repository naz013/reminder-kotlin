package com.elementary.tasks.navigation.fragments;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.ThemeUtil;

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

public class HelpFragment extends BaseWebViewFragment {

    @Override
    protected String getUrl() {
        boolean isDark = new ThemeUtil(mContext).isDark();
        String localeCheck = Locale.getDefault().toString().toLowerCase();
        String url;
        if (localeCheck.startsWith("uk")) {
            if (isDark) url = Constants.WEB_URL + "app_help/index.html";
            else url = Constants.WEB_URL + "app_help/index_light.html";
        } else if (localeCheck.startsWith("ru")) {
            if (isDark) url = Constants.WEB_URL + "app_help/index_ru.html";
            else url = Constants.WEB_URL + "app_help/index_light_ru.html";
        } else {
            if (isDark) url = Constants.WEB_URL + "app_help/index_en.html";
            else url = Constants.WEB_URL + "app_help/index_light_en.html";
        }
        return url;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.help));
            mCallback.onFragmentSelect(this);
        }
    }
}
