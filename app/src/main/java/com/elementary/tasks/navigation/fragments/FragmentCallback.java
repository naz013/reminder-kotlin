package com.elementary.tasks.navigation.fragments;

import android.app.Fragment;
import android.view.View;

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

public interface FragmentCallback {
    void replaceFragment(Fragment fragment, String title);

    void onTitleChange(String title);

    void onFragmentSelect(Fragment fragment);

    void setClick(View.OnClickListener listener);

    void onThemeChange(int primary, int primaryDark, int accent);

    void refreshMenu();
}
