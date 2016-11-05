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
import android.widget.SeekBar;

import com.elementary.tasks.R;
import com.elementary.tasks.core.LED;
import com.elementary.tasks.core.Language;
import com.elementary.tasks.core.file_explorer.FileExplorerActivity;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding;
import com.elementary.tasks.databinding.FragmentSettingsNotificationBinding;

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

public class NotificationSettingsFragment extends BaseSettingsFragment {

    private static final String TAG = "NotificationSettings";
    private static final int MELODY_CODE = 125;

    private FragmentSettingsNotificationBinding binding;
    private int mItemSelect;

    private View.OnClickListener mImageClick = view -> showImageDialog();
    private View.OnClickListener mBlurClick = view -> changeBlurPrefs();
    private View.OnClickListener mManualClick = view -> changeManualPrefs();
    private View.OnClickListener mSbClick = view -> changeSbPrefs();
    private View.OnClickListener mSbIconClick = view -> changeSbIconPrefs();
    private View.OnClickListener mVibrateClick = view -> changeVibratePrefs();
    private View.OnClickListener mInfiniteVibrateClick = view -> changeInfiniteVibratePrefs();
    private View.OnClickListener mSoundClick = view -> changeSoundPrefs();
    private View.OnClickListener mInfiniteSoundClick = view -> changeInfiniteSoundPrefs();
    private View.OnClickListener mMelodyClick = view -> showSoundDialog();
    private View.OnClickListener mSystemLoudnessClick = view -> changeSystemLoudnessPrefs();
    private View.OnClickListener mStreamClick = view -> showStreamDialog();
    private View.OnClickListener mLoudnessClick = view -> showLoudnessDialog();
    private View.OnClickListener mIncreaseClick = view -> changeIncreasePrefs();
    private View.OnClickListener mTtsClick = view -> changeTtsPrefs();
    private View.OnClickListener mTtsLocaleClick = view -> showTtsLocaleDialog();
    private View.OnClickListener mWakeClick = view -> changeWakePrefs();
    private View.OnClickListener mUnlockClick = view -> changeUnlockPrefs();
    private View.OnClickListener mAutoSmsClick = view -> changeAutoSmsPrefs();
    private View.OnClickListener mAutoLaunchClick = view -> changeAutoLaunchPrefs();
    private View.OnClickListener mSnoozeClick = view -> showSnoozeDialog();
    private View.OnClickListener mRepeatClick = view -> changeRepeatPrefs();
    private View.OnClickListener mLedColorClick = view -> showLedColorDialog();
    private View.OnClickListener mLedClick = view -> changeLedPrefs();
    private View.OnClickListener mRepeatTimeClick = view -> showRepeatTimeDialog();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsNotificationBinding.inflate(inflater, container, false);
        binding.imagePrefs.setOnClickListener(mImageClick);
        initBlurPrefs();
        initManualPrefs();
        initSbPrefs();
        initSbIconPrefs();
        initVibratePrefs();
        initInfiniteVibratePrefs();
        initSoundInSilentModePrefs();
        initInfiniteSoundPrefs();
        initMelodyPrefs();
        initSystemLoudnessPrefs();
        initSoundStreamPrefs();
        initLoudnessPrefs();
        initIncreasingLoudnessPrefs();
        initTtsPrefs();
        initTtsLocalePrefs();
        initWakePrefs();
        initUnlockPrefs();
        initAutoSmsPrefs();
        initAutoLaunchPrefs();
        initSnoozeTimePrefs();
        initLedPrefs();
        initLedColorPrefs();
        initRepeatPrefs();
        initRepeatTimePrefs();
        return binding.getRoot();
    }

    private void showRepeatTimeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.interval);
        DialogWithSeekAndTitleBinding b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(mContext));
        b.seekBar.setMax(60);
        b.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                b.titleView.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        int loudness = Prefs.getInstance(mContext).getNotificationRepeatTime();
        b.seekBar.setProgress(loudness);
        b.titleView.setText(String.valueOf(loudness));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            Prefs.getInstance(mContext).setNotificationRepeatTime(b.seekBar.getProgress());
            initRepeatTimePrefs();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void initRepeatTimePrefs() {
        binding.repeatIntervalPrefs.setValue(Prefs.getInstance(mContext).getNotificationRepeatTime());
        binding.repeatIntervalPrefs.setOnClickListener(mRepeatTimeClick);
        binding.repeatIntervalPrefs.setDependentView(binding.repeatNotificationOptionPrefs);
    }

    private void changeRepeatPrefs() {
        boolean isChecked = binding.repeatNotificationOptionPrefs.isChecked();
        binding.repeatNotificationOptionPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setNotificationRepeatEnabled(!isChecked);
    }

    private void initRepeatPrefs() {
        binding.repeatNotificationOptionPrefs.setOnClickListener(mRepeatClick);
        binding.repeatNotificationOptionPrefs.setChecked(Prefs.getInstance(mContext).isNotificationRepeatEnabled());
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
        int position = Prefs.getInstance(mContext).getLedColor();
        builder.setSingleChoiceItems(adapter, position, (dialog, which) -> {
            mItemSelect = which;
        });
        builder.setPositiveButton(mContext.getString(R.string.ok), (dialog, which) -> {
            dialog.dismiss();
            Prefs.getInstance(mContext).setLedColor(mItemSelect);
        });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialogInterface -> mItemSelect = 0);
        dialog.setOnDismissListener(dialogInterface -> mItemSelect = 0);
        dialog.show();
    }

    private void initLedColorPrefs() {
        binding.chooseLedColorPrefs.setOnClickListener(mLedColorClick);
        binding.chooseLedColorPrefs.setDependentView(binding.ledPrefs);
    }

    private void changeLedPrefs() {
        boolean isChecked = binding.ledPrefs.isChecked();
        binding.ledPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setLedEnabled(!isChecked);
    }

    private void initLedPrefs() {
        binding.ledPrefs.setOnClickListener(mLedClick);
        binding.ledPrefs.setChecked(Prefs.getInstance(mContext).isLedEnabled());
    }

    private void initSnoozeTimePrefs() {
        binding.delayForPrefs.setOnClickListener(mSnoozeClick);
        binding.delayForPrefs.setValue(Prefs.getInstance(mContext).getSnoozeTime());
    }

    private void showSnoozeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.snooze_time);
        DialogWithSeekAndTitleBinding b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(mContext));
        b.seekBar.setMax(60);
        b.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                b.titleView.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        int loudness = Prefs.getInstance(mContext).getSnoozeTime();
        b.seekBar.setProgress(loudness);
        b.titleView.setText(String.valueOf(loudness));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            Prefs.getInstance(mContext).setSnoozeTime(b.seekBar.getProgress());
            initSnoozeTimePrefs();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void changeAutoLaunchPrefs() {
        boolean isChecked = binding.autoLaunchPrefs.isChecked();
        binding.autoLaunchPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setAutoLaunchEnabled(!isChecked);
    }

    private void initAutoLaunchPrefs() {
        binding.autoLaunchPrefs.setOnClickListener(mAutoLaunchClick);
        binding.autoLaunchPrefs.setChecked(Prefs.getInstance(mContext).isAutoLaunchEnabled());
    }

    private void changeAutoSmsPrefs() {
        boolean isChecked = binding.silentSMSOptionPrefs.isChecked();
        binding.silentSMSOptionPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setAutoSmsEnabled(!isChecked);
    }

    private void initAutoSmsPrefs() {
        binding.silentSMSOptionPrefs.setOnClickListener(mAutoSmsClick);
        binding.silentSMSOptionPrefs.setChecked(Prefs.getInstance(mContext).isAutoSmsEnabled());
    }

    private void changeUnlockPrefs() {
        boolean isChecked = binding.unlockScreenPrefs.isChecked();
        binding.unlockScreenPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setDeviceUnlockEnabled(!isChecked);
    }

    private void initUnlockPrefs() {
        binding.unlockScreenPrefs.setOnClickListener(mUnlockClick);
        binding.unlockScreenPrefs.setChecked(Prefs.getInstance(mContext).isDeviceUnlockEnabled());
    }

    private void changeWakePrefs() {
        boolean isChecked = binding.wakeScreenOptionPrefs.isChecked();
        binding.wakeScreenOptionPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setDeviceAwakeEnabled(!isChecked);
    }

    private void initWakePrefs() {
        binding.wakeScreenOptionPrefs.setOnClickListener(mWakeClick);
        binding.wakeScreenOptionPrefs.setChecked(Prefs.getInstance(mContext).isDeviceAwakeEnabled());
    }

    private void showTtsLocaleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        builder.setTitle(mContext.getString(R.string.language));
        String locale = Prefs.getInstance(mContext).getTtsLocale();
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
            dialog.dismiss();
            saveTtsLocalePrefs();
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
        Prefs.getInstance(mContext).setTtsLocale(locale);
    }

    private void initTtsLocalePrefs() {
        binding.localePrefs.setOnClickListener(mTtsLocaleClick);
        binding.localePrefs.setDependentView(binding.ttsPrefs);
    }

    private void changeTtsPrefs() {
        boolean isChecked = binding.ttsPrefs.isChecked();
        binding.ttsPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setTtsEnabled(!isChecked);
    }

    private void initTtsPrefs() {
        binding.ttsPrefs.setOnClickListener(mTtsClick);
        binding.ttsPrefs.setChecked(Prefs.getInstance(mContext).isTtsEnabled());
    }

    private void changeIncreasePrefs() {
        boolean isChecked = binding.increasePrefs.isChecked();
        binding.increasePrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setIncreasingLoudnessEnabled(!isChecked);
    }

    private void initIncreasingLoudnessPrefs() {
        binding.increasePrefs.setOnClickListener(mIncreaseClick);
        binding.increasePrefs.setChecked(Prefs.getInstance(mContext).isIncreasingLoudnessEnabled());
    }

    private void showLoudnessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.loudness);
        DialogWithSeekAndTitleBinding b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(mContext));
        b.seekBar.setMax(25);
        b.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                b.titleView.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        int loudness = Prefs.getInstance(mContext).getLoudness();
        b.seekBar.setProgress(loudness);
        b.titleView.setText(String.valueOf(loudness));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialog, which) -> Prefs.getInstance(mContext).setLoudness(b.seekBar.getProgress()));
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void initLoudnessPrefs() {
        binding.volumePrefs.setOnClickListener(mLoudnessClick);
        binding.volumePrefs.setReverseDependentView(binding.systemPrefs);
    }

    private void showStreamDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(true);
        builder.setTitle(mContext.getString(R.string.sound_stream));
        String[] types = new String[]{mContext.getString(R.string.music),
                mContext.getString(R.string.alarm),
                mContext.getString(R.string.notification)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_list_item_single_choice, types);
        int stream = Prefs.getInstance(mContext).getSoundStream();
        mItemSelect = stream - 3;
        builder.setSingleChoiceItems(adapter, mItemSelect, (dialog, which) -> {
            if (which != -1) {
                mItemSelect = which;
            }
        });
        builder.setPositiveButton(mContext.getString(R.string.ok), (dialog, which) -> {
            dialog.dismiss();
            Prefs.getInstance(mContext).setSoundStream(mItemSelect + 3);
        });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialogInterface -> mItemSelect = 0);
        dialog.setOnDismissListener(dialogInterface -> mItemSelect = 0);
        dialog.show();
    }

    private void initSoundStreamPrefs() {
        binding.streamPrefs.setOnClickListener(mStreamClick);
        binding.streamPrefs.setDependentView(binding.systemPrefs);
    }

    private void changeSystemLoudnessPrefs() {
        boolean isChecked = binding.systemPrefs.isChecked();
        binding.systemPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setSystemLoudnessEnabled(!isChecked);
    }

    private void initSystemLoudnessPrefs() {
        binding.systemPrefs.setOnClickListener(mSystemLoudnessClick);
        binding.systemPrefs.setChecked(Prefs.getInstance(mContext).isSystemLoudnessEnabled());
    }

    private void initMelodyPrefs() {
        binding.chooseSoundPrefs.setOnClickListener(mMelodyClick);
        showMelody();
    }

    private void showMelody(){
        String filePath = Prefs.getInstance(mContext).getMelodyFile();
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
        if (Prefs.getInstance(mContext).getMelodyFile().matches(Constants.DEFAULT)) {
            mItemSelect = 0;
        } else {
            mItemSelect = 1;
        }
        builder.setSingleChoiceItems(adapter, mItemSelect, (dialog, which) -> {
            mItemSelect = which;
        });
        builder.setPositiveButton(mContext.getString(R.string.ok), (dialog, which) -> {
            dialog.dismiss();
            if (mItemSelect == 0) {
                Prefs.getInstance(mContext).setMelodyFile(Constants.DEFAULT);
                showMelody();
            } else {
                dialog.dismiss();
                startActivityForResult(new Intent(mContext, FileExplorerActivity.class), MELODY_CODE);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialogInterface -> mItemSelect = 0);
        dialog.setOnDismissListener(dialogInterface -> mItemSelect = 0);
        dialog.show();
    }

    private void changeInfiniteSoundPrefs() {
        boolean isChecked = binding.infiniteSoundOptionPrefs.isChecked();
        binding.infiniteSoundOptionPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setInfiniteSoundEnabled(!isChecked);
    }

    private void initInfiniteSoundPrefs() {
        binding.infiniteSoundOptionPrefs.setOnClickListener(mInfiniteSoundClick);
        binding.infiniteSoundOptionPrefs.setChecked(Prefs.getInstance(mContext).isInfiniteSoundEnabled());
    }

    private void changeSoundPrefs() {
        boolean isChecked = binding.soundOptionPrefs.isChecked();
        binding.soundOptionPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setSoundInSilentModeEnabled(!isChecked);
    }

    private void initSoundInSilentModePrefs() {
        binding.soundOptionPrefs.setOnClickListener(mSoundClick);
        binding.soundOptionPrefs.setChecked(Prefs.getInstance(mContext).isSoundInSilentModeEnabled());
    }

    private void changeInfiniteVibratePrefs() {
        boolean isChecked = binding.infiniteVibrateOptionPrefs.isChecked();
        binding.infiniteVibrateOptionPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setInfiniteVibrateEnabled(!isChecked);
    }

    private void initInfiniteVibratePrefs() {
        binding.infiniteVibrateOptionPrefs.setOnClickListener(mInfiniteVibrateClick);
        binding.infiniteVibrateOptionPrefs.setChecked(Prefs.getInstance(mContext).isInfiniteVibrateEnabled());
        binding.infiniteVibrateOptionPrefs.setDependentView(binding.vibrationOptionPrefs);
    }

    private void changeVibratePrefs() {
        boolean isChecked = binding.vibrationOptionPrefs.isChecked();
        binding.vibrationOptionPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setVibrateEnabled(!isChecked);
    }

    private void initVibratePrefs() {
        binding.vibrationOptionPrefs.setOnClickListener(mVibrateClick);
        binding.vibrationOptionPrefs.setChecked(Prefs.getInstance(mContext).isVibrateEnabled());
    }

    private void changeSbIconPrefs() {
        boolean isChecked = binding.statusIconPrefs.isChecked();
        binding.statusIconPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setSbIconEnabled(!isChecked);
        new Notifier(getActivity()).recreatePermanent();
    }

    private void initSbIconPrefs() {
        binding.statusIconPrefs.setOnClickListener(mSbIconClick);
        binding.statusIconPrefs.setDependentView(binding.permanentNotificationPrefs);
    }

    private void changeSbPrefs() {
        boolean isChecked = binding.permanentNotificationPrefs.isChecked();
        binding.permanentNotificationPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setSbNotificationEnabled(!isChecked);
        if (Prefs.getInstance(mContext).isSbNotificationEnabled()) {
            new Notifier(mContext).showPermanent();
        } else {
            new Notifier(mContext).hidePermanent();
        }
    }

    private void initSbPrefs() {
        binding.permanentNotificationPrefs.setOnClickListener(mSbClick);
        binding.permanentNotificationPrefs.setChecked(Prefs.getInstance(mContext).isSbNotificationEnabled());
    }

    private void changeManualPrefs() {
        boolean isChecked = binding.notificationDismissPrefs.isChecked();
        binding.notificationDismissPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setManualRemoveEnabled(!isChecked);
    }

    private void initManualPrefs() {
        binding.notificationDismissPrefs.setOnClickListener(mManualClick);
        binding.notificationDismissPrefs.setChecked(Prefs.getInstance(mContext).isManualRemoveEnabled());
    }

    private void initBlurPrefs() {
        binding.blurPrefs.setOnClickListener(mBlurClick);
        binding.blurPrefs.setChecked(Prefs.getInstance(mContext).isBlurEnabled());
    }

    private void changeBlurPrefs() {
        boolean isChecked = binding.blurPrefs.isChecked();
        binding.blurPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setBlurEnabled(!isChecked);
    }

    private void showImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(true);
        builder.setTitle(mContext.getString(R.string.background));
        String[] types = new String[]{mContext.getString(R.string.none),
                mContext.getString(R.string.default_string),
                mContext.getString(R.string.choose_file)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_list_item_single_choice, types);
        String image = Prefs.getInstance(mContext).getReminderImage();
        int selection;
        if (image.matches(Constants.NONE)) {
            selection = 0;
        } else if (image.matches(Constants.DEFAULT)) {
            selection = 1;
        } else {
            selection = 2;
        }
        builder.setSingleChoiceItems(adapter, selection, (dialog, which) -> {
            if (which != -1) {
                dialog.dismiss();
                saveImagePrefs(which);
            }
        });
        builder.setPositiveButton(mContext.getString(R.string.ok), (dialog, which) -> {
            dialog.dismiss();
            saveImagePrefs(which);
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveImagePrefs(int which) {
        Prefs prefs = Prefs.getInstance(mContext);
        if (which == 0) {
            prefs.setReminderImage(Constants.NONE);
        } else if (which == 1) {
            prefs.setReminderImage(Constants.DEFAULT);
        } else if (which == 2) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            if (Module.isKitkat()) {
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
            }
            Intent chooser = Intent.createChooser(intent, mContext.getString(R.string.image));
            startActivityForResult(chooser, Constants.ACTION_REQUEST_GALLERY);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.notification));
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
                            Prefs.getInstance(mContext).setMelodyFile(file.toString());
                        }
                    }
                    showMelody();
                }
                break;
        }
    }
}