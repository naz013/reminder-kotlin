package com.elementary.tasks.core.app_widgets.events;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SeekBar;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.databinding.CurrentWidgetConfigLayoutBinding;
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding;

import java.util.ArrayList;

/**
 * Copyright 2015 Nazar Suhovich
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

public class EventsWidgetConfig extends ThemedActivity {

    private int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Intent resultValue;
    public final static String EVENTS_WIDGET_PREF = "widget_pref";
    public final static String EVENTS_WIDGET_TEXT_SIZE = "widget_text_size_";
    public final static String EVENTS_WIDGET_THEME = "widget_theme_";

    private int textSize;

    private CurrentWidgetConfigLayoutBinding binding;
    private ArrayList<EventsTheme> mThemes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readIntent();
        binding = DataBindingUtil.setContentView(this, R.layout.current_widget_config_layout);
        initActionBar();
        loadThemes();
        showCurrentTheme();
    }

    private void showCurrentTheme() {
        SharedPreferences sp = getSharedPreferences(EVENTS_WIDGET_PREF, MODE_PRIVATE);
        int theme = sp.getInt(EVENTS_WIDGET_THEME + widgetID, 0);
        binding.themePager.setCurrentItem(theme, true);
    }

    private void initActionBar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        binding.toolbar.setTitle(getString(R.string.active_reminders));
    }

    private void readIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        setResult(RESULT_CANCELED, resultValue);
    }

    private void loadThemes(){
        mThemes = EventsTheme.getThemes(this);
        MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mThemes);
        binding.themePager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.widget_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add:
                showTextSizeDialog();
                return true;
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private void showTextSizeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.text_size);
        DialogWithSeekAndTitleBinding b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(this));
        b.seekBar.setMax(13);
        b.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textSize = progress + 12;
                b.titleView.setText(String.valueOf(textSize));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        b.seekBar.setProgress(2);
        textSize = 2 + 12;
        b.titleView.setText(String.valueOf(textSize));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            updateWidget();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void updateWidget() {
        SharedPreferences sp = getSharedPreferences(EVENTS_WIDGET_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(EVENTS_WIDGET_THEME + widgetID, binding.themePager.getCurrentItem());
        editor.putFloat(EVENTS_WIDGET_TEXT_SIZE + widgetID, textSize);
        editor.apply();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        EventsWidget.updateWidget(EventsWidgetConfig.this, appWidgetManager, sp, widgetID);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        ArrayList<EventsTheme> arrayList;

        public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<EventsTheme> list) {
            super(fm);
            this.arrayList = list;
        }

        @Override
        public Fragment getItem(int position) {
            return EventsThemeFragment.newInstance(position, mThemes);
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }
    }
}
