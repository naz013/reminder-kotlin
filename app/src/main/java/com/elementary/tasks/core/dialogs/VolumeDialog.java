package com.elementary.tasks.core.dialogs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.SeekBar;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding;

/**
 * Copyright 2017 Nazar Suhovich
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

public class VolumeDialog extends BaseDialog {

    private DialogInterface.OnCancelListener mCancelListener = dialogInterface -> finish();
    private DialogInterface.OnDismissListener mOnDismissListener = dialogInterface -> finish();

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setTitle(R.string.loudness);
        DialogWithSeekAndTitleBinding b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(this));
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
        int loudness = Prefs.getInstance(this).getLoudness();
        b.seekBar.setProgress(loudness);
        b.titleView.setText(String.valueOf(loudness));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialog, which) -> Prefs.getInstance(this).setLoudness(b.seekBar.getProgress()));
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnCancelListener(mCancelListener);
        alertDialog.setOnDismissListener(mOnDismissListener);
        alertDialog.show();
    }
}
