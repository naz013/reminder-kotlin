package com.elementary.tasks.core;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ThemeUtil;

public abstract class ThemedActivity extends AppCompatActivity {

    protected ThemeUtil themeUtil;
    protected Prefs mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = Prefs.getInstance(this);
        themeUtil = ThemeUtil.getInstance(this);
        setTheme(themeUtil.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(themeUtil.getColor(themeUtil.colorPrimaryDark()));
        }
    }
}
