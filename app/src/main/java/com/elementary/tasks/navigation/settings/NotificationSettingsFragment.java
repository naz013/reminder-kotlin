package com.elementary.tasks.navigation.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;

import com.elementary.tasks.R;
import com.elementary.tasks.core.file_explorer.FileExplorerActivity;
import com.elementary.tasks.core.services.PermanentReminderReceiver;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.LED;
import com.elementary.tasks.core.utils.Language;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.UriUtil;
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding;
import com.elementary.tasks.databinding.FragmentSettingsNotificationBinding;

import java.io.File;
import java.util.Locale;

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

    private static final int MELODY_CODE = 125;
    private static final int PERM_BT = 1425;
    private static final int PERM_SD = 1426;

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
    private View.OnClickListener mAutoCallClick = view -> changeAutoCallPrefs();

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
        initAutoCallPrefs();
        initReminderTypePrefs();
        initIgnoreWindowTypePrefs();
        if (!Permissions.checkPermission(getContext(), Permissions.READ_EXTERNAL)) {
            Permissions.requestPermission(getActivity(), PERM_SD, Permissions.READ_EXTERNAL);
        }
        return binding.getRoot();
    }

    private void changeIgnoreWindowTypePrefs() {
        boolean isChecked = binding.ignoreWindowType.isChecked();
        binding.ignoreWindowType.setChecked(!isChecked);
        getPrefs().setIgnoreWindowType(!isChecked);
    }

    private void initIgnoreWindowTypePrefs() {
        binding.ignoreWindowType.setOnClickListener(v -> changeIgnoreWindowTypePrefs());
        binding.ignoreWindowType.setChecked(getPrefs().isIgnoreWindowType());
    }

    private void showRepeatTimeDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setTitle(R.string.interval);
        DialogWithSeekAndTitleBinding b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(getContext()));
        b.seekBar.setMax(60);
        b.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                b.titleView.setText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                        String.valueOf(progress)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        int repeatTime = getPrefs().getNotificationRepeatTime();
        b.seekBar.setProgress(repeatTime);
        b.titleView.setText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                String.valueOf(repeatTime)));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            getPrefs().setNotificationRepeatTime(b.seekBar.getProgress());
            showRepeatTime();
            initRepeatTimePrefs();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void initRepeatTimePrefs() {
        binding.repeatIntervalPrefs.setValue(getPrefs().getNotificationRepeatTime());
        binding.repeatIntervalPrefs.setOnClickListener(mRepeatTimeClick);
        binding.repeatIntervalPrefs.setDependentView(binding.repeatNotificationOptionPrefs);
        showRepeatTime();
    }

    private void showRepeatTime() {
        binding.repeatIntervalPrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                String.valueOf(getPrefs().getNotificationRepeatTime())));
    }

    private void changeRepeatPrefs() {
        boolean isChecked = binding.repeatNotificationOptionPrefs.isChecked();
        binding.repeatNotificationOptionPrefs.setChecked(!isChecked);
        getPrefs().setNotificationRepeatEnabled(!isChecked);
    }

    private void initRepeatPrefs() {
        binding.repeatNotificationOptionPrefs.setOnClickListener(mRepeatClick);
        binding.repeatNotificationOptionPrefs.setChecked(getPrefs().isNotificationRepeatEnabled());
    }

    private void showLedColorDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.led_color));
        String[] colors = LED.getAllNames(getContext());
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_single_choice, colors);
        mItemSelect = getPrefs().getLedColor();
        builder.setSingleChoiceItems(adapter, mItemSelect, (dialog, which) -> mItemSelect = which);
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            getPrefs().setLedColor(mItemSelect);
            showLedColor();
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialogInterface -> mItemSelect = 0);
        dialog.setOnDismissListener(dialogInterface -> mItemSelect = 0);
        dialog.show();
    }

    private void showLedColor() {
        binding.chooseLedColorPrefs.setDetailText(LED.getTitle(getContext(), getPrefs().getLedColor()));
    }

    private void initLedColorPrefs() {
        binding.chooseLedColorPrefs.setOnClickListener(mLedColorClick);
        binding.chooseLedColorPrefs.setDependentView(binding.ledPrefs);
        showLedColor();
    }

    private void changeLedPrefs() {
        boolean isChecked = binding.ledPrefs.isChecked();
        binding.ledPrefs.setChecked(!isChecked);
        getPrefs().setLedEnabled(!isChecked);
    }

    private void initLedPrefs() {
        binding.ledPrefs.setOnClickListener(mLedClick);
        binding.ledPrefs.setChecked(getPrefs().isLedEnabled());
    }

    private void initSnoozeTimePrefs() {
        binding.delayForPrefs.setOnClickListener(mSnoozeClick);
        binding.delayForPrefs.setValue(getPrefs().getSnoozeTime());
        showSnooze();
    }

    private void showSnooze() {
        binding.delayForPrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                String.valueOf(getPrefs().getSnoozeTime())));
    }

    private void showSnoozeDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setTitle(R.string.snooze_time);
        DialogWithSeekAndTitleBinding b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(getContext()));
        b.seekBar.setMax(60);
        b.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                b.titleView.setText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                        String.valueOf(progress)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        int snoozeTime = getPrefs().getSnoozeTime();
        b.seekBar.setProgress(snoozeTime);
        b.titleView.setText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                String.valueOf(snoozeTime)));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            getPrefs().setSnoozeTime(b.seekBar.getProgress());
            showSnooze();
            initSnoozeTimePrefs();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void changeAutoCallPrefs() {
        boolean isChecked = binding.autoCallPrefs.isChecked();
        binding.autoCallPrefs.setChecked(!isChecked);
        getPrefs().setAutoCallEnabled(!isChecked);
    }

    private void initAutoCallPrefs() {
        binding.autoCallPrefs.setOnClickListener(mAutoCallClick);
        binding.autoCallPrefs.setChecked(getPrefs().isAutoCallEnabled());
    }

    private void changeAutoLaunchPrefs() {
        boolean isChecked = binding.autoLaunchPrefs.isChecked();
        binding.autoLaunchPrefs.setChecked(!isChecked);
        getPrefs().setAutoLaunchEnabled(!isChecked);
    }

    private void initAutoLaunchPrefs() {
        binding.autoLaunchPrefs.setOnClickListener(mAutoLaunchClick);
        binding.autoLaunchPrefs.setChecked(getPrefs().isAutoLaunchEnabled());
    }

    private void changeAutoSmsPrefs() {
        boolean isChecked = binding.silentSMSOptionPrefs.isChecked();
        binding.silentSMSOptionPrefs.setChecked(!isChecked);
        getPrefs().setAutoSmsEnabled(!isChecked);
    }

    private void initAutoSmsPrefs() {
        binding.silentSMSOptionPrefs.setOnClickListener(mAutoSmsClick);
        binding.silentSMSOptionPrefs.setChecked(getPrefs().isAutoSmsEnabled());
    }

    private void changeUnlockPrefs() {
        boolean isChecked = binding.unlockScreenPrefs.isChecked();
        binding.unlockScreenPrefs.setChecked(!isChecked);
        getPrefs().setDeviceUnlockEnabled(!isChecked);
    }

    private void initUnlockPrefs() {
        binding.unlockScreenPrefs.setOnClickListener(mUnlockClick);
        binding.unlockScreenPrefs.setChecked(getPrefs().isDeviceUnlockEnabled());
    }

    private void changeWakePrefs() {
        boolean isChecked = binding.wakeScreenOptionPrefs.isChecked();
        binding.wakeScreenOptionPrefs.setChecked(!isChecked);
        getPrefs().setDeviceAwakeEnabled(!isChecked);
    }

    private void initWakePrefs() {
        binding.wakeScreenOptionPrefs.setOnClickListener(mWakeClick);
        binding.wakeScreenOptionPrefs.setChecked(getPrefs().isDeviceAwakeEnabled());
    }

    private void showTtsLocaleDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.language));
        String locale = getPrefs().getTtsLocale();
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

    private void showTtsLocale() {
        String locale = getPrefs().getTtsLocale();
        int i = Language.getLocalePosition(locale);
        binding.localePrefs.setDetailText(Language.getLocaleNames(getContext()).get(i));
    }

    private ArrayAdapter<String> getLocaleAdapter() {
        return new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_single_choice, Language.getLocaleNames(getContext()));
    }

    private void saveTtsLocalePrefs() {
        getPrefs().setTtsLocale(Language.getLocaleByPosition(mItemSelect));
        showTtsLocale();
    }

    private void initTtsLocalePrefs() {
        binding.localePrefs.setOnClickListener(mTtsLocaleClick);
        binding.localePrefs.setDependentView(binding.ttsPrefs);
        showTtsLocale();
    }

    private void changeTtsPrefs() {
        boolean isChecked = binding.ttsPrefs.isChecked();
        binding.ttsPrefs.setChecked(!isChecked);
        getPrefs().setTtsEnabled(!isChecked);
    }

    private void initTtsPrefs() {
        binding.ttsPrefs.setOnClickListener(mTtsClick);
        binding.ttsPrefs.setChecked(getPrefs().isTtsEnabled());
    }

    private void changeIncreasePrefs() {
        if (SuperUtil.hasVolumePermission(getContext())) {
            changeIncrease();
        } else {
            openNotificationsSettings();
        }

    }

    private void openNotificationsSettings() {
        if (Module.isNougat()) {
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            getActivity().startActivityForResult(intent, 1248);
        }
    }

    private void changeIncrease() {
        boolean isChecked = binding.increasePrefs.isChecked();
        binding.increasePrefs.setChecked(!isChecked);
        getPrefs().setIncreasingLoudnessEnabled(!isChecked);
    }

    private void initIncreasingLoudnessPrefs() {
        binding.increasePrefs.setOnClickListener(mIncreaseClick);
        binding.increasePrefs.setChecked(getPrefs().isIncreasingLoudnessEnabled());
    }

    private void showLoudnessDialog() {
        if (!SuperUtil.hasVolumePermission(getContext())) {
            openNotificationsSettings();
            return;
        }
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setTitle(R.string.loudness);
        DialogWithSeekAndTitleBinding b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(getContext()));
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
        int loudness = getPrefs().getLoudness();
        b.seekBar.setProgress(loudness);
        b.titleView.setText(String.valueOf(loudness));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            getPrefs().setLoudness(b.seekBar.getProgress());
            showLoudness();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void initLoudnessPrefs() {
        binding.volumePrefs.setOnClickListener(mLoudnessClick);
        showLoudness();
    }

    private void showLoudness() {
        binding.volumePrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.loudness) + " %d",
                getPrefs().getLoudness()));
    }

    private void showStreamDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.sound_stream));
        String[] types = new String[]{getString(R.string.music),
                getString(R.string.alarm),
                getString(R.string.notification)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_single_choice, types);
        int stream = getPrefs().getSoundStream();
        mItemSelect = stream - 3;
        builder.setSingleChoiceItems(adapter, mItemSelect, (dialog, which) -> {
            if (which != -1) {
                mItemSelect = which;
            }
        });
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            getPrefs().setSoundStream(mItemSelect + 3);
            showStream();
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialogInterface -> mItemSelect = 0);
        dialog.setOnDismissListener(dialogInterface -> mItemSelect = 0);
        dialog.show();
    }

    private void initReminderTypePrefs() {
        binding.typePrefs.setOnClickListener(v -> showReminderTypeDialog());
        showReminderType();
    }

    private void showReminderTypeDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setCancelable(true);
        builder.setTitle(R.string.notification_type);
        String[] types = new String[]{
                getString(R.string.full_screen),
                getString(R.string.simple)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_single_choice, types);
        mItemSelect = getPrefs().getReminderType();
        builder.setSingleChoiceItems(adapter, mItemSelect, (dialog, which) -> {
            if (which != -1) {
                mItemSelect = which;
            }
        });
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            getPrefs().setReminderType(mItemSelect);
            showReminderType();
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialogInterface -> mItemSelect = 0);
        dialog.setOnDismissListener(dialogInterface -> mItemSelect = 0);
        dialog.show();
    }

    private void showReminderType() {
        String[] types = new String[]{
                getString(R.string.full_screen),
                getString(R.string.simple)};
        binding.typePrefs.setDetailText(types[getPrefs().getReminderType()]);
    }

    private void initSoundStreamPrefs() {
        binding.streamPrefs.setOnClickListener(mStreamClick);
        binding.streamPrefs.setDependentView(binding.systemPrefs);
        showStream();
    }

    private void showStream() {
        String[] types = new String[]{getString(R.string.music),
                getString(R.string.alarm),
                getString(R.string.notification)};
        binding.streamPrefs.setDetailText(types[getPrefs().getSoundStream() - 3]);
    }

    private void changeSystemLoudnessPrefs() {
        if (SuperUtil.hasVolumePermission(getContext())) {
            boolean isChecked = binding.systemPrefs.isChecked();
            binding.systemPrefs.setChecked(!isChecked);
            getPrefs().setSystemLoudnessEnabled(!isChecked);
        } else openNotificationsSettings();
    }

    private void initSystemLoudnessPrefs() {
        binding.systemPrefs.setOnClickListener(mSystemLoudnessClick);
        binding.systemPrefs.setChecked(getPrefs().isSystemLoudnessEnabled());
    }

    private void initMelodyPrefs() {
        binding.chooseSoundPrefs.setOnClickListener(mMelodyClick);
        showMelody();
    }

    private void showMelody(){
        String filePath = getPrefs().getMelodyFile();
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
        if (getPrefs().getMelodyFile() == null || getPrefs().getMelodyFile().matches(Constants.DEFAULT)) {
            mItemSelect = 0;
        } else {
            mItemSelect = 1;
        }
        builder.setSingleChoiceItems(adapter, mItemSelect, (dialog, which) -> mItemSelect = which);
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            if (mItemSelect == 0) {
                getPrefs().setMelodyFile(Constants.DEFAULT);
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

    private void changeInfiniteSoundPrefs() {
        boolean isChecked = binding.infiniteSoundOptionPrefs.isChecked();
        binding.infiniteSoundOptionPrefs.setChecked(!isChecked);
        getPrefs().setInfiniteSoundEnabled(!isChecked);
    }

    private void initInfiniteSoundPrefs() {
        binding.infiniteSoundOptionPrefs.setOnClickListener(mInfiniteSoundClick);
        binding.infiniteSoundOptionPrefs.setChecked(getPrefs().isInfiniteSoundEnabled());
    }

    private void changeSoundPrefs() {
        boolean isChecked = binding.soundOptionPrefs.isChecked();
        binding.soundOptionPrefs.setChecked(!isChecked);
        getPrefs().setSoundInSilentModeEnabled(!isChecked);
        if (!SuperUtil.checkNotificationPermission(getActivity())) {
            SuperUtil.askNotificationPermission(getActivity());
        } else if (!Permissions.checkPermission(getContext(), Permissions.BLUETOOTH)) {
            Permissions.requestPermission(getActivity(), PERM_BT, Permissions.BLUETOOTH);
        }
    }

    private void initSoundInSilentModePrefs() {
        binding.soundOptionPrefs.setOnClickListener(mSoundClick);
        binding.soundOptionPrefs.setChecked(getPrefs().isSoundInSilentModeEnabled());
    }

    private void changeInfiniteVibratePrefs() {
        boolean isChecked = binding.infiniteVibrateOptionPrefs.isChecked();
        binding.infiniteVibrateOptionPrefs.setChecked(!isChecked);
        getPrefs().setInfiniteVibrateEnabled(!isChecked);
    }

    private void initInfiniteVibratePrefs() {
        binding.infiniteVibrateOptionPrefs.setOnClickListener(mInfiniteVibrateClick);
        binding.infiniteVibrateOptionPrefs.setChecked(getPrefs().isInfiniteVibrateEnabled());
        binding.infiniteVibrateOptionPrefs.setDependentView(binding.vibrationOptionPrefs);
    }

    private void changeVibratePrefs() {
        boolean isChecked = binding.vibrationOptionPrefs.isChecked();
        binding.vibrationOptionPrefs.setChecked(!isChecked);
        getPrefs().setVibrateEnabled(!isChecked);
    }

    private void initVibratePrefs() {
        binding.vibrationOptionPrefs.setOnClickListener(mVibrateClick);
        binding.vibrationOptionPrefs.setChecked(getPrefs().isVibrateEnabled());
    }

    private void changeSbIconPrefs() {
        boolean isChecked = binding.statusIconPrefs.isChecked();
        binding.statusIconPrefs.setChecked(!isChecked);
        getPrefs().setSbIconEnabled(!isChecked);
        Notifier.updateReminderPermanent(getContext(), PermanentReminderReceiver.ACTION_SHOW);
    }

    private void initSbIconPrefs() {
        binding.statusIconPrefs.setOnClickListener(mSbIconClick);
        binding.statusIconPrefs.setChecked(getPrefs().isSbIconEnabled());
        binding.statusIconPrefs.setDependentView(binding.permanentNotificationPrefs);
    }

    private void changeSbPrefs() {
        boolean isChecked = binding.permanentNotificationPrefs.isChecked();
        binding.permanentNotificationPrefs.setChecked(!isChecked);
        getPrefs().setSbNotificationEnabled(!isChecked);
        if (getPrefs().isSbNotificationEnabled()) {
            Notifier.updateReminderPermanent(getContext(), PermanentReminderReceiver.ACTION_SHOW);
        } else {
            Notifier.updateReminderPermanent(getContext(), PermanentReminderReceiver.ACTION_HIDE);
        }
    }

    private void initSbPrefs() {
        binding.permanentNotificationPrefs.setOnClickListener(mSbClick);
        binding.permanentNotificationPrefs.setChecked(getPrefs().isSbNotificationEnabled());
    }

    private void changeManualPrefs() {
        boolean isChecked = binding.notificationDismissPrefs.isChecked();
        binding.notificationDismissPrefs.setChecked(!isChecked);
        getPrefs().setManualRemoveEnabled(!isChecked);
    }

    private void initManualPrefs() {
        binding.notificationDismissPrefs.setOnClickListener(mManualClick);
        binding.notificationDismissPrefs.setChecked(getPrefs().isManualRemoveEnabled());
    }

    private void initBlurPrefs() {
        binding.blurPrefs.setOnClickListener(mBlurClick);
        binding.blurPrefs.setChecked(getPrefs().isBlurEnabled());
    }

    private void changeBlurPrefs() {
        boolean isChecked = binding.blurPrefs.isChecked();
        binding.blurPrefs.setChecked(!isChecked);
        getPrefs().setBlurEnabled(!isChecked);
    }

    private void showImageDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.background));
        String[] types = new String[]{getString(R.string.none),
                getString(R.string.default_string),
                getString(R.string.choose_file)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_single_choice, types);
        String image = getPrefs().getReminderImage();
        if (image.matches(Constants.NONE)) {
            mItemSelect = 0;
        } else if (image.matches(Constants.DEFAULT)) {
            mItemSelect = 1;
        } else {
            mItemSelect = 2;
        }
        builder.setSingleChoiceItems(adapter, mItemSelect, (dialog, which) -> mItemSelect = which);
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            saveImagePrefs(mItemSelect);
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialogInterface -> mItemSelect = 0);
        dialog.setOnDismissListener(dialogInterface -> mItemSelect = 0);
        dialog.show();
    }

    private void saveImagePrefs(int which) {
        if (which == 0) {
            getPrefs().setReminderImage(Constants.NONE);
        } else if (which == 1) {
            getPrefs().setReminderImage(Constants.DEFAULT);
        } else if (which == 2) {
            startActivityForResult(new Intent(getContext(), FileExplorerActivity.class)
                    .putExtra(Constants.FILE_TYPE, FileExplorerActivity.TYPE_PHOTO), Constants.ACTION_REQUEST_GALLERY);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.notification));
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
                            getPrefs().setMelodyFile(file.toString());
                        }
                    }
                    showMelody();
                }
                break;
            case Constants.ACTION_REQUEST_GALLERY:
                if (resultCode == Activity.RESULT_OK) {
                    String filePath = data.getStringExtra(Constants.FILE_PICKED);
                    if (filePath != null) {
                        File file = new File(filePath);
                        if (file.exists()) {
                            Uri uri = UriUtil.getUri(getContext(), file);
                            getPrefs().setReminderImage(uri.toString());
                        }
                    }
                }
                break;
        }
    }
}