package com.elementary.tasks.core.utils;

import android.app.AlertDialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import com.elementary.tasks.R;
import com.elementary.tasks.core.interfaces.LCAMListener;
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding;

import androidx.appcompat.widget.PopupMenu;

import static com.elementary.tasks.core.utils.ThemeUtil.THEME_AMOLED;

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
public class Dialogues {

    private static final int MAX_RADIUS = 100000;
    private static final int MAX_DEF_RADIUS = 5000;

    public static void showRadiusDialog(@NonNull Context context, int current, @NonNull OnValueSelectedListener<Integer> listener) {
        AlertDialog.Builder builder = Dialogues.getDialog(context);
        builder.setTitle(R.string.radius);
        DialogWithSeekAndTitleBinding b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(context));
        b.seekBar.setMax(MAX_DEF_RADIUS);
        while (b.seekBar.getMax() < current && b.seekBar.getMax() < MAX_RADIUS) {
            b.seekBar.setMax(b.seekBar.getMax() + 1000);
        }
        if (current > MAX_RADIUS) {
            b.seekBar.setMax(MAX_RADIUS);
        }
        b.seekBar.setMax(current * 2);
        if (current == 0) {
            b.seekBar.setMax(MAX_DEF_RADIUS);
        }
        b.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                b.titleView.setText(listener.getTitle(progress));
                float perc = (float) progress / (float) b.seekBar.getMax() * 100f;
                if (perc > 95f && b.seekBar.getMax() < MAX_RADIUS) {
                    b.seekBar.setMax(b.seekBar.getMax() + 1000);
                } else if (perc < 15f && b.seekBar.getMax() > 5000) {
                    b.seekBar.setMax(b.seekBar.getMax() - 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        b.seekBar.setProgress(current);
        b.titleView.setText(listener.getTitle(current));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> listener.onSelected(b.seekBar.getProgress()));
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    public static AlertDialog.Builder getDialog(@NonNull Context context) {
        if (Prefs.getInstance(context).getAppTheme() == THEME_AMOLED) {
            return new AlertDialog.Builder(context, ThemeUtil.getInstance(context).getDialogStyle());
        } else {
            return new AlertDialog.Builder(context);
        }
    }

    public static void showLCAM(@NonNull Context context, @Nullable LCAMListener listener, @NonNull String... actions) {
        AlertDialog.Builder builder = getDialog(context);
        builder.setItems(actions, (dialog, item) -> {
            dialog.dismiss();
            if (listener != null) listener.onAction(item);
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showPopup(@NonNull Context context, @NonNull View anchor,
                                 @Nullable LCAMListener listener, @NonNull String... actions) {
        PopupMenu popupMenu = new PopupMenu(context, anchor);
        popupMenu.setOnMenuItemClickListener(item -> {
            if (listener != null) {
                listener.onAction(item.getOrder());
            }
            return true;
        });
        for (int i = 0; i < actions.length; i++) {
            popupMenu.getMenu().add(1, i + 1000, i, actions[i]);
        }
        popupMenu.show();
    }

    public interface OnValueSelectedListener<T> {
        void onSelected(T t);
        String getTitle(T t);
    }
}
