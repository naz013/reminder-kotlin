package com.elementary.tasks.navigation.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.elementary.tasks.R;
import com.elementary.tasks.core.file_explorer.FileExplorerActivity;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.LED;
import com.elementary.tasks.core.utils.Language;
import com.elementary.tasks.databinding.FragmentBirthdayNotificationsBinding;

import java.io.File;

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
        binding.chooseLedColorPrefs.setReverseDependentView(binding.globalOptionPrefs);
        binding.chooseLedColorPrefs.setDependentView(binding.ledPrefs);
        binding.chooseLedColorPrefs.setOnClickListener(view -> showLedColorDialog());
        showLedColor();
    }

    private void showLedColor() {
        binding.chooseLedColorPrefs.setDetailText(LED.getTitle(getContext(), getPrefs().getBirthdayLedColor()));
    }

    private void showLedColorDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setTitle(getString(R.string.led_color));
        String[] colors = LED.getAllNames(getContext());
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_single_choice, colors);
        mItemSelect = getPrefs().getBirthdayLedColor();
        builder.setSingleChoiceItems(adapter, mItemSelect, (dialog, which) -> mItemSelect = which);
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            getPrefs().setBirthdayLedColor(mItemSelect);
            showLedColor();
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialogInterface -> mItemSelect = 0);
        dialog.setOnDismissListener(dialogInterface -> mItemSelect = 0);
        dialog.show();
    }

    private void initLedPrefs() {
        binding.ledPrefs.setChecked(getPrefs().isBirthdayLedEnabled());
        binding.ledPrefs.setOnClickListener(view -> changeLedPrefs());
        binding.ledPrefs.setReverseDependentView(binding.globalOptionPrefs);
    }

    private void changeLedPrefs() {
        boolean isChecked = binding.ledPrefs.isChecked();
        binding.ledPrefs.setChecked(!isChecked);
        getPrefs().setBirthdayLedEnabled(!isChecked);
    }

    private void initMelodyPrefs() {
        binding.chooseSoundPrefs.setOnClickListener(view -> showSoundDialog());
        binding.chooseSoundPrefs.setReverseDependentView(binding.globalOptionPrefs);
        showMelody();
    }

    private void showMelody(){
        String filePath = getPrefs().getBirthdayMelody();
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
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.melody));
        String[] types = new String[]{getString(R.string.default_string),
                getString(R.string.choose_file)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_single_choice, types);
        if (getPrefs().getBirthdayMelody() == null || getPrefs().getBirthdayMelody().matches(Constants.DEFAULT)) {
            mItemSelect = 0;
        } else {
            mItemSelect = 1;
        }
        builder.setSingleChoiceItems(adapter, mItemSelect, (dialog, which) -> mItemSelect = which);
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            if (mItemSelect == 0) {
                getPrefs().setBirthdayMelody(Constants.DEFAULT);
                showMelody();
            } else {
                dialog.dismiss();
                startActivityForResult(new Intent(getContext(), FileExplorerActivity.class), MELODY_CODE);
            }
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialogInterface -> mItemSelect = 0);
        dialog.setOnDismissListener(dialogInterface -> mItemSelect = 0);
        dialog.show();
    }

    private void initTtsLocalePrefs() {
        binding.localePrefs.setReverseDependentView(binding.globalOptionPrefs);
        binding.localePrefs.setDependentView(binding.ttsPrefs);
        binding.localePrefs.setOnClickListener(view -> showTtsLocaleDialog());
        showTtsLocale();
    }

    private void showTtsLocale() {
        String locale = getPrefs().getBirthdayTtsLocale();
        int i = Language.getLocalePosition(locale);
        binding.localePrefs.setDetailText(Language.getLocaleNames(getContext()).get(i));
    }

    private void showTtsLocaleDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.language));
        String locale = getPrefs().getBirthdayTtsLocale();
        mItemSelect = Language.getLocalePosition(locale);
        builder.setSingleChoiceItems(getLocaleAdapter(), mItemSelect, (dialog, which) -> mItemSelect = which);
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            saveTtsLocalePrefs();
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialogInterface -> mItemSelect = 0);
        dialog.setOnDismissListener(dialogInterface -> mItemSelect = 0);
        dialog.show();
    }


    private ArrayAdapter<String> getLocaleAdapter() {
        return new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_single_choice,
                Language.getLocaleNames(getContext()));
    }

    private void saveTtsLocalePrefs() {
        getPrefs().setBirthdayTtsLocale(Language.getLocaleByPosition(mItemSelect));
        showTtsLocale();
    }

    private void initTtsPrefs() {
        binding.ttsPrefs.setChecked(getPrefs().isBirthdayTtsEnabled());
        binding.ttsPrefs.setReverseDependentView(binding.globalOptionPrefs);
        binding.ttsPrefs.setOnClickListener(view -> changeTtsPrefs());
    }

    private void changeTtsPrefs() {
        boolean isChecked = binding.ttsPrefs.isChecked();
        binding.ttsPrefs.setChecked(!isChecked);
        getPrefs().setBirthdayTtsEnabled(!isChecked);
    }

    private void initWakePrefs() {
        binding.wakeScreenOptionPrefs.setChecked(getPrefs().isBirthdayWakeEnabled());
        binding.wakeScreenOptionPrefs.setReverseDependentView(binding.globalOptionPrefs);
        binding.wakeScreenOptionPrefs.setOnClickListener(view -> changeWakePrefs());
    }

    private void changeWakePrefs() {
        boolean isChecked = binding.wakeScreenOptionPrefs.isChecked();
        binding.wakeScreenOptionPrefs.setChecked(!isChecked);
        getPrefs().setBirthdayWakeEnabled(!isChecked);
    }

    private void initInfiniteSoundPrefs() {
        binding.infiniteSoundOptionPrefs.setReverseDependentView(binding.globalOptionPrefs);
        binding.infiniteSoundOptionPrefs.setChecked(getPrefs().isBirthdayInfiniteSoundEnabled());
        binding.infiniteSoundOptionPrefs.setOnClickListener(view -> changeInfiniteSoundPrefs());
    }

    private void changeInfiniteSoundPrefs() {
        boolean isChecked = binding.infiniteSoundOptionPrefs.isChecked();
        binding.infiniteSoundOptionPrefs.setChecked(!isChecked);
        getPrefs().setBirthdayInfiniteSoundEnabled(!isChecked);
    }

    private void initSilentPrefs() {
        binding.soundOptionPrefs.setChecked(getPrefs().isBirthdaySilentEnabled());
        binding.soundOptionPrefs.setOnClickListener(view -> changeSilentPrefs());
        binding.soundOptionPrefs.setReverseDependentView(binding.globalOptionPrefs);
    }

    private void changeSilentPrefs() {
        boolean isChecked = binding.soundOptionPrefs.isChecked();
        binding.soundOptionPrefs.setChecked(!isChecked);
        getPrefs().setBirthdaySilentEnabled(!isChecked);
    }

    private void initInfiniteVibratePrefs() {
        binding.infiniteVibrateOptionPrefs.setChecked(getPrefs().isBirthdayInfiniteVibrationEnabled());
        binding.infiniteVibrateOptionPrefs.setOnClickListener(view -> changeInfiniteVibrationPrefs());
        binding.infiniteVibrateOptionPrefs.setReverseDependentView(binding.globalOptionPrefs);
    }

    private void changeInfiniteVibrationPrefs() {
        boolean isChecked = binding.infiniteVibrateOptionPrefs.isChecked();
        binding.infiniteVibrateOptionPrefs.setChecked(!isChecked);
        getPrefs().setBirthdayInfiniteVibrationEnabled(!isChecked);
    }

    private void initVibratePrefs() {
        binding.vibrationOptionPrefs.setChecked(getPrefs().isBirthdayVibrationEnabled());
        binding.vibrationOptionPrefs.setOnClickListener(view -> changeVibrationPrefs());
        binding.vibrationOptionPrefs.setReverseDependentView(binding.globalOptionPrefs);
    }

    private void changeVibrationPrefs() {
        boolean isChecked = binding.vibrationOptionPrefs.isChecked();
        binding.vibrationOptionPrefs.setChecked(!isChecked);
        getPrefs().setBirthdayVibrationEnabled(!isChecked);
    }

    private void initGlobalPrefs() {
        binding.globalOptionPrefs.setChecked(getPrefs().isBirthdayGlobalEnabled());
        binding.globalOptionPrefs.setOnClickListener(view -> changeGlobalPrefs());
    }

    private void changeGlobalPrefs() {
        boolean isChecked = binding.globalOptionPrefs.isChecked();
        binding.globalOptionPrefs.setChecked(!isChecked);
        getPrefs().setBirthdayGlobalEnabled(!isChecked);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.birthday_notification));
            getCallback().onFragmentSelect(this);
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
                            getPrefs().setBirthdayMelody(file.toString());
                        }
                    }
                    showMelody();
                }
                break;
        }
    }
}
