package com.elementary.tasks.core;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.elementary.tasks.core.utils.Language;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ThemeUtil;

public abstract class ThemedActivity extends AppCompatActivity {

    private ThemeUtil themeUtil;
    private Prefs mPrefs;

    protected Prefs getPrefs() {
        return mPrefs;
    }

    protected ThemeUtil getThemeUtil() {
        return themeUtil;
    }

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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(Language.onAttach(newBase));
    }
}
