package com.elementary.tasks.navigation.settings.theme;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.services.PermanentReminderService;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.views.ColorPickerView;

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

public class SelectThemeActivity extends ThemedActivity implements ColorPickerView.OnColorListener {

    private FloatingActionButton mFab;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_theme_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(getString(R.string.theme_color));
        int loaded = Prefs.getInstance(this).getAppThemeColor();
        ColorPickerView pickerView = (ColorPickerView) findViewById(R.id.pickerView);
        pickerView.setListener(this);
        pickerView.setSelectedColor(loaded);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setBackgroundTintList(ViewUtils.getFabState(this, themeUtil.colorAccent(), themeUtil.colorPrimary()));
    }

    private void saveColor(int code) {
        Prefs.getInstance(this).setAppThemeColor(code);
        Prefs.getInstance(this).setUiChanged(true);
    }

    @Override
    public void onBackPressed() {
        updateNotification();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                updateNotification();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateNotification() {
        if (Prefs.getInstance(this).isSbNotificationEnabled()) {
            startService(new Intent(this, PermanentReminderService.class).setAction(PermanentReminderService.ACTION_SHOW));
        }
    }

    private void refreshUi() {
        toolbar.setBackgroundColor(ViewUtils.getColor(this, themeUtil.colorPrimary()));
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, themeUtil.colorPrimaryDark()));
        }
        mFab.setBackgroundTintList(ViewUtils.getFabState(this, themeUtil.colorAccent(), themeUtil.colorPrimary()));
        mFab.setRippleColor(ViewUtils.getColor(this, themeUtil.colorPrimary()));
    }

    @Override
    public void onColorSelect(int code) {
        saveColor(code);
        refreshUi();
    }
}
