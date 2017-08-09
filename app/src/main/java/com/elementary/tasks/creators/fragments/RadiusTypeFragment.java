package com.elementary.tasks.creators.fragments;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.SeekBar;
import android.widget.TextView;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding;

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

abstract class RadiusTypeFragment extends TypeFragment {

    protected int radius = Prefs.getInstance(getContext()).getRadius();

    protected final void showRadiusPickerDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setTitle(R.string.radius);
        DialogWithSeekAndTitleBinding b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(getContext()));
        b.seekBar.setMax(5001);
        b.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setTitle(b.titleView, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        b.seekBar.setProgress(radius);
        setTitle(b.titleView, radius + 1);
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            radius = b.seekBar.getProgress() - 1;
            recreateMarker();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void setTitle(TextView textView, int progress) {
        if (progress == 0) {
            textView.setText(getString(R.string.default_string));
        } else {
            textView.setText(String.format(Locale.getDefault(), getString(R.string.radius_x_meters), String.valueOf(progress - 1)));
        }
    }

    protected abstract void recreateMarker();

    @Override
    public boolean save() {
        if (!SuperUtil.checkLocationEnable(getContext())) {
            SuperUtil.showLocationAlert(getContext(), getInterface());
            return false;
        }
        return true;
    }
}
