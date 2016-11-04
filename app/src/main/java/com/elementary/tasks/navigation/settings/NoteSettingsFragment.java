package com.elementary.tasks.navigation.settings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.views.PrefsView;
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding;
import com.elementary.tasks.databinding.FragmentSettingsNotesLayoutBinding;

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

public class NoteSettingsFragment extends BaseSettingsFragment {

    private PrefsView mNoteReminderPrefs;
    private View.OnClickListener mNoteReminderClick = view -> changeNoteReminder();
    private View.OnClickListener mNoteTimeClick = view -> showTimePickerDialog();
    private View.OnClickListener mNoteTextSizeClick = view -> showTextSizePickerDialog();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentSettingsNotesLayoutBinding binding = FragmentSettingsNotesLayoutBinding.inflate(inflater, container, false);
        mNoteReminderPrefs = binding.noteReminderPrefs;
        mNoteReminderPrefs.setOnClickListener(mNoteReminderClick);
        mNoteReminderPrefs.setChecked(Prefs.getInstance(mContext).isNoteReminderEnabled());
        PrefsView mNoteReminderTimePrefs = binding.noteReminderTime;
        mNoteReminderTimePrefs.setOnClickListener(mNoteTimeClick);
        mNoteReminderTimePrefs.setDependentView(mNoteReminderPrefs);
        binding.textSize.setOnClickListener(mNoteTextSizeClick);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.notes));
            mCallback.onFragmentSelect(this);
        }
    }

    private void changeNoteReminder() {
        boolean isChecked = mNoteReminderPrefs.isChecked();
        mNoteReminderPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setNoteReminderEnabled(!isChecked);
    }

    private void showTextSizePickerDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.text_size);
        DialogWithSeekAndTitleBinding b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(mContext));
        b.seekBar.setMax(18);
        b.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                b.titleView.setText(String.valueOf(progress + 12));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        int textSize = Prefs.getInstance(mContext).getNoteTextSize();
        b.seekBar.setProgress(textSize);
        b.titleView.setText(String.valueOf(textSize + 12));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialog, which) -> Prefs.getInstance(mContext).setNoteTextSize(b.seekBar.getProgress()));
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showTimePickerDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.time);
        DialogWithSeekAndTitleBinding b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(mContext));
        b.seekBar.setMax(120);
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
        int time = Prefs.getInstance(mContext).getNoteReminderTime();
        b.seekBar.setProgress(time);
        b.titleView.setText(String.valueOf(time));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialog, which) -> Prefs.getInstance(mContext).setNoteReminderTime(b.seekBar.getProgress()));
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}
