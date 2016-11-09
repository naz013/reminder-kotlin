package com.elementary.tasks.navigation.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.LED;
import com.elementary.tasks.core.utils.Language;
import com.elementary.tasks.core.file_explorer.FileExplorerActivity;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.databinding.FragmentBirthdayNotificationsBinding;

import java.io.File;
import java.util.ArrayList;

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

public class BirthdayNotificationFragment extends BaseSettingsFragment {

    private static final int MELODY_CODE = 126;

    private FragmentBirthdayNotificationsBinding binding;
    private int mItemSelect;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBirthdayNotificationsBinding.inflate(inflater, container, false);
        initGlobalPrefs();
        initVibratePrefs();
        initInfiniteVibratePrefs();
        initSilentPrefs();
        initInfiniteSoundPrefs();
        initWakePrefs();
        initTtsPrefs();
        initTtsLocalePrefs();
        initMelodyPrefs();
        initLedPrefs();
        initLedColorPrefs();
        return binding.getRoot();
    }

    private void initLedColorPrefs() {
        binding.chooseLedColorPrefs.setDependentView(binding.globalOptionPrefs);
        binding.chooseLedColorPrefs.setDependentView(binding.ledPrefs);
        binding.chooseLedColorPrefs.setOnClickListener(view -> showLedColorDialog());
    }

    private void showLedColorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        builder.setTitle(mContext.getString(R.string.led_color));
        String[] colors = new String[LED.NUM_OF_LEDS];
        for (int i = 0; i < LED.NUM_OF_LEDS; i++) {
            colors[i] = LED.getTitle(mContext, i);
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_list_item_single_choice, colors);
        mItemSelect = Prefs.getInstance(mContext).getBirthdayLedColor();
        builder.setSingleChoiceItems(adapter, mItemSelect, (dialog, which) -> {
            mItemSelect = which;
        });
        builder.setPositiveButton(mContext.getString(R.string.ok), (dialog, which) -> {
            Prefs.getInstance(mContext).setBirthdayLedColor(mItemSelect);
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialogInterface -> mItemSelect = 0);
        dialog.setOnDismissListener(dialogInterface -> mItemSelect = 0);
        dialog.show();
    }

    private void initLedPrefs() {
        binding.ledPrefs.setChecked(Prefs.getInstance(mContext).isBirthdayLedEnabled());
        binding.ledPrefs.setOnClickListener(view -> changeLedPrefs());
        binding.ledPrefs.setDependentView(binding.globalOptionPrefs);
    }

    private void changeLedPrefs() {
        boolean isChecked = binding.ledPrefs.isChecked();
        binding.ledPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setBirthdayLedEnabled(!isChecked);
    }

    private void initMelodyPrefs() {
        binding.chooseSoundPrefs.setOnClickListener(view -> showSoundDialog());
        binding.chooseSoundPrefs.setDependentView(binding.globalOptionPrefs);
        showMelody();
    }

    private void showMelody(){
        String filePath = Prefs.getInstance(mContext).getBirthdayMelody();
        if (filePath == null || filePath.matches(Constants.DEFAULT)) {
            binding.chooseSoundPrefs.setDetailText(getResources().getString(R.string.default_string));
        } else if (!filePath.matches("")) {
            File sound = new File(filePath);
            String fileName = sound.getName();
            int pos = fileName.lastIndexOf(".");
            String fileNameS = fileName.substring(0, pos);
            binding.chooseSoundPrefs.setDetailText(fileNameS);
        } else {
            binding.chooseSoundPrefs.setDetailText(getResources().getString(R.string.default_string));
        }
    }

    private void showSoundDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(true);
        builder.setTitle(mContext.getString(R.string.melody));
        String[] types = new String[]{mContext.getString(R.string.default_string),
                mContext.getString(R.string.choose_file)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_list_item_single_choice, types);
        if (Prefs.getInstance(mContext).getBirthdayMelody().matches(Constants.DEFAULT)) {
            mItemSelect = 0;
        } else {
            mItemSelect = 1;
        }
        builder.setSingleChoiceItems(adapter, mItemSelect, (dialog, which) -> {
            mItemSelect = which;
        });
        builder.setPositiveButton(mContext.getString(R.string.ok), (dialog, which) -> {
            if (mItemSelect == 0) {
                Prefs.getInstance(mContext).setBirthdayMelody(Constants.DEFAULT);
                showMelody();
            } else {
                dialog.dismiss();
                startActivityForResult(new Intent(mContext, FileExplorerActivity.class), MELODY_CODE);
            }
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialogInterface -> mItemSelect = 0);
        dialog.setOnDismissListener(dialogInterface -> mItemSelect = 0);
        dialog.show();
    }

    private void initTtsLocalePrefs() {
        binding.localePrefs.setDependentView(binding.globalOptionPrefs);
        binding.localePrefs.setDependentView(binding.ttsPrefs);
        binding.localePrefs.setOnClickListener(view -> showTtsLocaleDialog());
    }

    private void showTtsLocaleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        builder.setTitle(mContext.getString(R.string.language));
        String locale = Prefs.getInstance(mContext).getBirthdayTtsLocale();
        if (locale.matches(Language.ENGLISH)) mItemSelect = 0;
        if (locale.matches(Language.FRENCH)) mItemSelect = 1;
        if (locale.matches(Language.GERMAN)) mItemSelect = 2;
        if (locale.matches(Language.ITALIAN)) mItemSelect = 3;
        if (locale.matches(Language.JAPANESE)) mItemSelect = 4;
        if (locale.matches(Language.KOREAN)) mItemSelect = 5;
        if (locale.matches(Language.POLISH)) mItemSelect = 6;
        if (locale.matches(Language.RUSSIAN)) mItemSelect = 7;
        if (locale.matches(Language.SPANISH)) mItemSelect = 8;
        builder.setSingleChoiceItems(getLocaleAdapter(), mItemSelect, (dialog, which) -> {
            mItemSelect = which;
        });
        builder.setPositiveButton(mContext.getString(R.string.ok), (dialog, which) -> {
            saveTtsLocalePrefs();
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialogInterface -> mItemSelect = 0);
        dialog.setOnDismissListener(dialogInterface -> mItemSelect = 0);
        dialog.show();
    }

    private ArrayAdapter<String> getLocaleAdapter() {
        ArrayList<String> names = new ArrayList<>();
        names.add(mContext.getString(R.string.english));
        names.add(mContext.getString(R.string.french));
        names.add(mContext.getString(R.string.german));
        names.add(mContext.getString(R.string.italian));
        names.add(mContext.getString(R.string.japanese));
        names.add(mContext.getString(R.string.korean));
        names.add(mContext.getString(R.string.polish));
        names.add(mContext.getString(R.string.russian));
        names.add(mContext.getString(R.string.spanish));
        return new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_single_choice, names);
    }

    private void saveTtsLocalePrefs() {
        String locale = Language.ENGLISH;
        if (mItemSelect == 0) locale = Language.ENGLISH;
        if (mItemSelect == 1) locale = Language.FRENCH;
        if (mItemSelect == 2) locale = Language.GERMAN;
        if (mItemSelect == 3) locale = Language.ITALIAN;
        if (mItemSelect == 4) locale = Language.JAPANESE;
        if (mItemSelect == 5) locale = Language.KOREAN;
        if (mItemSelect == 6) locale = Language.POLISH;
        if (mItemSelect == 7) locale = Language.RUSSIAN;
        if (mItemSelect == 8) locale = Language.SPANISH;
        Prefs.getInstance(mContext).setBirthdayTtsLocale(locale);
    }

    private void initTtsPrefs() {
        binding.ttsPrefs.setChecked(Prefs.getInstance(mContext).isBirthdayTtsEnabled());
        binding.ttsPrefs.setDependentView(binding.globalOptionPrefs);
        binding.ttsPrefs.setOnClickListener(view -> changeTtsPrefs());
    }

    private void changeTtsPrefs() {
        boolean isChecked = binding.ttsPrefs.isChecked();
        binding.ttsPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setBirthdayTtsEnabled(!isChecked);
    }

    private void initWakePrefs() {
        binding.wakeScreenOptionPrefs.setChecked(Prefs.getInstance(mContext).isBirthdayWakeEnabled());
        binding.wakeScreenOptionPrefs.setDependentView(binding.globalOptionPrefs);
        binding.wakeScreenOptionPrefs.setOnClickListener(view -> changeWakePrefs());
    }

    private void changeWakePrefs() {
        boolean isChecked = binding.wakeScreenOptionPrefs.isChecked();
        binding.wakeScreenOptionPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setBirthdayWakeEnabled(!isChecked);
    }

    private void initInfiniteSoundPrefs() {
        binding.infiniteSoundOptionPrefs.setDependentView(binding.globalOptionPrefs);
        binding.infiniteSoundOptionPrefs.setChecked(Prefs.getInstance(mContext).isBirthdayInfiniteSoundEnabled());
        binding.infiniteSoundOptionPrefs.setOnClickListener(view -> changeInfiniteSoundPrefs());
    }

    private void changeInfiniteSoundPrefs() {
        boolean isChecked = binding.infiniteSoundOptionPrefs.isChecked();
        binding.infiniteSoundOptionPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setBirthdayInfiniteSoundEnabled(!isChecked);
    }

    private void initSilentPrefs() {
        binding.soundOptionPrefs.setChecked(Prefs.getInstance(mContext).isBirthdaySilentEnabled());
        binding.soundOptionPrefs.setOnClickListener(view -> changeSilentPrefs());
        binding.soundOptionPrefs.setDependentView(binding.globalOptionPrefs);
    }

    private void changeSilentPrefs() {
        boolean isChecked = binding.soundOptionPrefs.isChecked();
        binding.soundOptionPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setBirthdaySilentEnabled(!isChecked);
    }

    private void initInfiniteVibratePrefs() {
        binding.infiniteVibrateOptionPrefs.setChecked(Prefs.getInstance(mContext).isBirthdayInfiniteVibrationEnabled());
        binding.infiniteVibrateOptionPrefs.setOnClickListener(view -> changeInfiniteVibrationPrefs());
        binding.infiniteVibrateOptionPrefs.setDependentView(binding.globalOptionPrefs);
    }

    private void changeInfiniteVibrationPrefs() {
        boolean isChecked = binding.infiniteVibrateOptionPrefs.isChecked();
        binding.infiniteVibrateOptionPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setBirthdayInfiniteVibrationEnabled(!isChecked);
    }

    private void initVibratePrefs() {
        binding.vibrationOptionPrefs.setChecked(Prefs.getInstance(mContext).isBirthdayVibrationEnabled());
        binding.vibrationOptionPrefs.setOnClickListener(view -> changeVibrationPrefs());
        binding.vibrationOptionPrefs.setDependentView(binding.globalOptionPrefs);
    }

    private void changeVibrationPrefs() {
        boolean isChecked = binding.vibrationOptionPrefs.isChecked();
        binding.vibrationOptionPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setBirthdayVibrationEnabled(!isChecked);
    }

    private void initGlobalPrefs() {
        binding.globalOptionPrefs.setChecked(Prefs.getInstance(mContext).isBirthdayGlobalEnabled());
        binding.globalOptionPrefs.setOnClickListener(view -> changeGlobalPrefs());
    }

    private void changeGlobalPrefs() {
        boolean isChecked = binding.globalOptionPrefs.isChecked();
        binding.globalOptionPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setBirthdayGlobalEnabled(!isChecked);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.birthday_notification));
            mCallback.onFragmentSelect(this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MELODY_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    String filePath = data.getStringExtra(Constants.FILE_PICKED);
                    if (filePath != null) {
                        File file = new File(filePath);
                        if (file.exists()) {
                            Prefs.getInstance(mContext).setBirthdayMelody(file.toString());
                        }
                    }
                    showMelody();
                }
                break;
        }
    }
}
