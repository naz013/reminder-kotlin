package com.elementary.tasks.core.dialogs;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.elementary.tasks.core.utils.ThemeUtil;

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

public abstract class BaseDialog extends Activity {

    private ThemeUtil themeUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeUtil = ThemeUtil.getInstance(this);
        setTheme(themeUtil.getDialogStyle());
    }

    public ThemeUtil getThemeUtil() {
        return themeUtil;
    }
}
