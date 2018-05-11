package com.elementary.tasks.core.app_widgets.voice_control;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.app_widgets.WidgetUtils;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.databinding.VoiceWidgetConfigLayoutBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

public class VoiceWidgetConfig extends ThemedActivity {

    public static final String VOICE_WIDGET_PREF = "widget_pref";
    public static final String VOICE_WIDGET_COLOR = "widget_color_";
    private int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;

    private Intent resultValue;
    private int color;

    private VoiceWidgetConfigLayoutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readIntent();
        binding = DataBindingUtil.setContentView(this, R.layout.voice_widget_config_layout);
        initActionBar();

        Spinner headerBgColor = findViewById(R.id.headerBgColor);
        boolean isPro = Module.isPro();
        List<String> spinnerArray = new ArrayList<>();
        String[] colorsArray = getResources().getStringArray(R.array.color_list);
        Collections.addAll(spinnerArray, colorsArray);
        if (isPro) {
            spinnerArray.add(getString(R.string.dark_purple));
            spinnerArray.add(getString(R.string.dark_orange));
            spinnerArray.add(getString(R.string.lime));
            spinnerArray.add(getString(R.string.indigo));
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        headerBgColor.setAdapter(spinnerArrayAdapter);
        headerBgColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                color = WidgetUtils.getDrawable(i);
                binding.widgetLayout.widgetBg.setBackgroundResource(color);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initActionBar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        binding.toolbar.setTitle(getString(R.string.voice_control));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.widget_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                savePrefs();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void savePrefs() {
        SharedPreferences sp = getSharedPreferences(VOICE_WIDGET_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(VOICE_WIDGET_COLOR + widgetID, color);
        editor.apply();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        VoiceWidget.updateWidget(VoiceWidgetConfig.this, appWidgetManager, sp, widgetID);
        setResult(RESULT_OK, resultValue);
        finish();
    }
}
