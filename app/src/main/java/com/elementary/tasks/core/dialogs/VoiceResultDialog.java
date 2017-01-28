package com.elementary.tasks.core.dialogs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.creators.CreateReminderActivity;
import com.elementary.tasks.databinding.ReminderListItemBinding;
import com.elementary.tasks.reminder.models.Reminder;

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

public class VoiceResultDialog extends BaseDialog {

    private DialogInterface.OnCancelListener mCancelListener = dialogInterface -> finish();
    private DialogInterface.OnDismissListener mOnDismissListener = dialogInterface -> finish();

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.saved));
        String uuId = getIntent().getStringExtra(Constants.INTENT_ID);
        Reminder reminder = RealmDb.getInstance().getReminder(uuId);
        ReminderListItemBinding binding = ReminderListItemBinding.inflate(LayoutInflater.from(this), null, false);
        binding.setItem(reminder);
        binding.itemCheck.setVisibility(View.GONE);
        binding.reminderContainer.setBackgroundColor(themeUtil.getCardStyle());
        alert.setView(binding.getRoot());
        alert.setCancelable(true);
        alert.setNegativeButton(R.string.edit, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            startActivity(new Intent(VoiceResultDialog.this, CreateReminderActivity.class).putExtra(Constants.INTENT_ID, uuId));
            finish();
        });
        alert.setPositiveButton(R.string.ok, (dialog, id) -> {
            dialog.dismiss();
            finish();
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.setOnCancelListener(mCancelListener);
        alertDialog.setOnDismissListener(mOnDismissListener);
        alertDialog.show();
    }
}
