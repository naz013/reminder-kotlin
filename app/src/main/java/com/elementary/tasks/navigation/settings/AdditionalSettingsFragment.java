package com.elementary.tasks.navigation.settings;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.views.PrefsView;
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding;
import com.elementary.tasks.databinding.FragmentSettingsAdditionalBinding;
import com.elementary.tasks.navigation.settings.additional.TemplatesFragment;

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

public class AdditionalSettingsFragment extends BaseSettingsFragment {

    private static final int MISSED = 107;
    private static final int QUICK_SMS = 108;
    private static final int FOLLOW = 109;

    private FragmentSettingsAdditionalBinding binding;
    private PrefsView mMissedPrefs;
    private PrefsView mQuickSmsPrefs;
    private View.OnClickListener mMissedClick = view -> changeMissedPrefs();
    private View.OnClickListener mMissedTimeClick = view -> showTimePickerDialog();
    private View.OnClickListener mQuickSmsClick = view -> changeQuickSmsPrefs();
    private View.OnClickListener mFollowClick = view -> changeFollowPrefs();
    private View.OnClickListener mMessagesClick = view -> replaceFragment(new TemplatesFragment(), getString(R.string.messages));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsAdditionalBinding.inflate(inflater, container, false);
        initMissedPrefs();
        initMissedTimePrefs();
        initQuickSmsPrefs();
        initMessagesPrefs();
        binding.followReminderPrefs.setOnClickListener(mFollowClick);
        binding.followReminderPrefs.setChecked(getPrefs().isFollowReminderEnabled());
        return binding.getRoot();
    }

    private void initMessagesPrefs() {
        PrefsView mMessagesPrefs = binding.templatesPrefs;
        mMessagesPrefs.setOnClickListener(mMessagesClick);
        mMessagesPrefs.setDependentView(mQuickSmsPrefs);
    }

    private void initQuickSmsPrefs() {
        mQuickSmsPrefs = binding.quickSMSPrefs;
        mQuickSmsPrefs.setOnClickListener(mQuickSmsClick);
        mQuickSmsPrefs.setChecked(getPrefs().isQuickSmsEnabled());
    }

    private void initMissedTimePrefs() {
        binding.missedTimePrefs.setOnClickListener(mMissedTimeClick);
        binding.missedTimePrefs.setDependentView(mMissedPrefs);
        showTime();
    }

    private void showTime() {
        binding.missedTimePrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                String.valueOf(getPrefs().getMissedReminderTime())));
    }

    private void initMissedPrefs() {
        mMissedPrefs = binding.missedPrefs;
        mMissedPrefs.setOnClickListener(mMissedClick);
        mMissedPrefs.setChecked(getPrefs().isMissedReminderEnabled());
    }

    private void changeFollowPrefs() {
        if (!Permissions.checkPermission(getActivity(), Permissions.READ_PHONE_STATE)) {
            Permissions.requestPermission(getActivity(), FOLLOW, Permissions.READ_PHONE_STATE);
            return;
        }
        boolean isChecked = binding.followReminderPrefs.isChecked();
        binding.followReminderPrefs.setChecked(!isChecked);
        getPrefs().setFollowReminderEnabled(!isChecked);
    }

    private void changeMissedPrefs() {
        if (!Permissions.checkPermission(getActivity(), Permissions.READ_PHONE_STATE)) {
            Permissions.requestPermission(getActivity(), MISSED, Permissions.READ_PHONE_STATE);
            return;
        }
        boolean isChecked = mMissedPrefs.isChecked();
        mMissedPrefs.setChecked(!isChecked);
        getPrefs().setMissedReminderEnabled(!isChecked);
    }

    private void changeQuickSmsPrefs() {
        if (!Permissions.checkPermission(getActivity(), Permissions.READ_PHONE_STATE)) {
            Permissions.requestPermission(getActivity(), QUICK_SMS, Permissions.READ_PHONE_STATE);
            return;
        }
        boolean isChecked = mQuickSmsPrefs.isChecked();
        mQuickSmsPrefs.setChecked(!isChecked);
        getPrefs().setQuickSmsEnabled(!isChecked);
    }

    private void showTimePickerDialog(){
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
        int time = getPrefs().getMissedReminderTime();
        b.seekBar.setProgress(time);
        b.titleView.setText(String.format(Locale.getDefault(), getString(R.string.x_minutes), String.valueOf(time)));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            getPrefs().setMissedReminderTime(b.seekBar.getProgress());
            showTime();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.additional));
            getCallback().onFragmentSelect(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 0) return;
        switch (requestCode) {
            case MISSED:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    changeMissedPrefs();
                }
                break;
            case QUICK_SMS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    changeQuickSmsPrefs();
                }
                break;
            case FOLLOW:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    changeFollowPrefs();
                }
                break;
        }
    }
}
