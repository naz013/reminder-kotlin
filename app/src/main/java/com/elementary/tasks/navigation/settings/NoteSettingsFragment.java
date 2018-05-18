package com.elementary.tasks.navigation.settings;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding;
import com.elementary.tasks.databinding.FragmentSettingsNotesLayoutBinding;

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

public class NoteSettingsFragment extends BaseSettingsFragment {

    private FragmentSettingsNotesLayoutBinding binding;
    private View.OnClickListener mNoteReminderClick = view -> changeNoteReminder();
    private View.OnClickListener mNoteColorRememberClick = view -> changeNoteColorRemembering();
    private View.OnClickListener mNoteTimeClick = view -> showTimePickerDialog();
    private View.OnClickListener mNoteTextSizeClick = view -> showTextSizePickerDialog();
    private View.OnClickListener mNoteColorOpacityClick = view -> showOpacityPickerDialog();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsNotesLayoutBinding.inflate(inflater, container, false);
        initNoteReminderPrefs();
        initNoteTime();
        initTextSizePrefs();
        initNoteColorRememberPrefs();
        initColorOpacityPrefs();
        return binding.getRoot();
    }

    private void initNoteColorRememberPrefs() {
        binding.noteColorRememberPrefs.setOnClickListener(mNoteColorRememberClick);
        binding.noteColorRememberPrefs.setChecked(getPrefs().isNoteColorRememberingEnabled());
    }

    private void changeNoteColorRemembering() {
        boolean isChecked = binding.noteColorRememberPrefs.isChecked();
        binding.noteColorRememberPrefs.setChecked(!isChecked);
        getPrefs().setNoteColorRememberingEnabled(!isChecked);
    }

    private void initColorOpacityPrefs() {
        binding.noteColorOpacity.setOnClickListener(mNoteColorOpacityClick);
        showNoteColorSaturation();
    }

    private void initTextSizePrefs() {
        binding.textSize.setOnClickListener(mNoteTextSizeClick);
        showTextSize();
    }

    private void showTextSize() {
        binding.textSize.setDetailText(String.format(Locale.getDefault(), "%d pt", (getPrefs().getNoteTextSize() + 12)));
    }

    private void initNoteReminderPrefs() {
        binding.noteReminderPrefs.setOnClickListener(mNoteReminderClick);
        binding.noteReminderPrefs.setChecked(getPrefs().isNoteReminderEnabled());
    }

    private void initNoteTime() {
        binding.noteReminderTime.setOnClickListener(mNoteTimeClick);
        binding.noteReminderTime.setDependentView(binding.noteReminderPrefs);
        showNoteTime();
    }

    private void showNoteTime() {
        binding.noteReminderTime.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                String.valueOf(getPrefs().getNoteReminderTime())));
    }

    private void showNoteColorSaturation() {
        binding.noteColorOpacity.setDetailText(String.format(Locale.getDefault(), "%d%%", getPrefs().getNoteColorOpacity()));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.notes));
            getCallback().onFragmentSelect(this);
        }
    }

    private void changeNoteReminder() {
        boolean isChecked = binding.noteReminderPrefs.isChecked();
        binding.noteReminderPrefs.setChecked(!isChecked);
        getPrefs().setNoteReminderEnabled(!isChecked);
    }

    private void showTextSizePickerDialog(){
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setTitle(R.string.text_size);
        DialogWithSeekAndTitleBinding b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(getContext()));
        b.seekBar.setMax(18);
        b.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                b.titleView.setText(String.format(Locale.getDefault(), "%d pt", (progress + 12)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        int textSize = getPrefs().getNoteTextSize();
        b.seekBar.setProgress(textSize);
        b.titleView.setText(String.format(Locale.getDefault(), "%d pt", (textSize + 12)));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            getPrefs().setNoteTextSize(b.seekBar.getProgress());
            showTextSize();
            dialogInterface.dismiss();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showTimePickerDialog(){
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setTitle(R.string.time);
        DialogWithSeekAndTitleBinding b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(getContext()));
        b.seekBar.setMax(120);
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
        int time = getPrefs().getNoteReminderTime();
        b.seekBar.setProgress(time);
        b.titleView.setText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                String.valueOf(time)));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            getPrefs().setNoteReminderTime(b.seekBar.getProgress());
            showNoteTime();
            dialogInterface.dismiss();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showOpacityPickerDialog(){
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setTitle(R.string.color_saturation);
        DialogWithSeekAndTitleBinding b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(getContext()));
        b.seekBar.setMax(100);
        b.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                b.titleView.setText(String.format(Locale.getDefault(), "%d%%", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        int opacity = getPrefs().getNoteColorOpacity();
        b.seekBar.setProgress(opacity);
        b.titleView.setText(String.format(Locale.getDefault(), "%d%%", opacity));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            getPrefs().setNoteColorOpacity(b.seekBar.getProgress());
            showNoteColorSaturation();
            dialogInterface.dismiss();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}
