package com.elementary.tasks.core.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.view.View;

import com.elementary.tasks.core.interfaces.LCAMListener;

import org.jetbrains.annotations.NotNull;

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

    public static void showPopup(@NonNull @NotNull Context context, @NonNull View anchor,
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
}
