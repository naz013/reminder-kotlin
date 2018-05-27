package com.elementary.tasks.missed_calls;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.BaseNotificationActivity;
import com.elementary.tasks.core.data.models.MissedCall;
import com.elementary.tasks.core.utils.Configs;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Contacts;
import com.elementary.tasks.core.utils.LED;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TelephonyUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.view_models.missed_calls.MissedCallViewModel;
import com.elementary.tasks.databinding.ActivityReminderDialogBinding;

import java.sql.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

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
public class MissedCallDialogActivity extends BaseNotificationActivity {

    private static final String TAG = "MCDialogActivity";

    private static final int CALL_PERM = 612;

    private ActivityReminderDialogBinding binding;
    private MissedCallViewModel viewModel;

    @Nullable
    private MissedCall mMissedCall;
    private boolean mIsResumed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mIsResumed = getIntent().getBooleanExtra(Constants.INTENT_NOTIFICATION, false);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder_dialog);

        binding.card.setCardBackgroundColor(getThemeUtil().getCardStyle());
        if (Module.isLollipop()) binding.card.setCardElevation(Configs.CARD_ELEVATION);
        binding.container.setVisibility(View.GONE);
        binding.subjectContainer.setVisibility(View.GONE);
        loadImage(binding.bgImage);
        colorify(binding.buttonOk, binding.buttonCancel, binding.buttonCall, binding.buttonDelay,
                binding.buttonDelayFor, binding.buttonNotification, binding.buttonEdit);
        binding.buttonDelay.hide();
        binding.buttonDelayFor.hide();
        binding.buttonNotification.hide();
        binding.buttonEdit.hide();

        binding.buttonOk.setImageResource(R.drawable.ic_done_black_24dp);
        binding.buttonCancel.setImageResource(R.drawable.ic_clear_black_24dp);
        binding.buttonCall.setImageResource(R.drawable.ic_call_black_24dp);

        binding.contactPhoto.setBorderColor(getThemeUtil().getColor(getThemeUtil().colorPrimary()));
        binding.contactPhoto.setVisibility(View.GONE);

        initViewModel();
    }

    private void initViewModel() {
        viewModel = ViewModelProviders.of(this, new MissedCallViewModel.Factory(getApplication(),
                getIntent().getStringExtra(Constants.INTENT_ID))).get(MissedCallViewModel.class);
        viewModel.missedCall.observe(this, missedCall -> {
            if (missedCall != null) {
                showInfo(missedCall);
            } else {
                closeWindow();
            }
        });
        viewModel.result.observe(this, commands -> {
            if (commands != null) {
                switch (commands) {
                    case DELETED:
                        closeWindow();
                        break;
                }
            }
        });
    }

    private void showInfo(@NonNull MissedCall missedCall) {
        this.mMissedCall = missedCall;
        String formattedTime = "";
        try {
            formattedTime = TimeUtil.getTime(new Date(missedCall.getDateTime()), getPrefs().is24HourFormatEnabled());
        } catch (NullPointerException e) {
            LogUtil.d(TAG, "onCreate: " + e.getLocalizedMessage());
        }
        String name = Contacts.getNameFromNumber(missedCall.getNumber(), this);
        String wearMessage = (name != null ? name : "") + "\n" + missedCall.getNumber();
        if (missedCall.getNumber() != null) {
            long conID = Contacts.getIdFromNumber(missedCall.getNumber(), this);
            Uri photo = Contacts.getPhoto(conID);
            if (photo != null) {
                binding.contactPhoto.setImageURI(photo);
            } else {
                binding.contactPhoto.setVisibility(View.GONE);
            }
            binding.remText.setText(R.string.missed_call);
            binding.contactInfo.setText(wearMessage);
            binding.actionDirect.setText(R.string.from);
            binding.someView.setText(R.string.last_called);
            binding.messageView.setText(formattedTime);
            binding.container.setVisibility(View.VISIBLE);
        }
        binding.buttonCancel.setOnClickListener(v -> sendSMS());
        binding.buttonOk.setOnClickListener(v -> ok());
        binding.buttonCall.setOnClickListener(v -> call());
        showMissedReminder(name == null || name.matches("") ? missedCall.getNumber() : name);
        init();
    }

    private void closeWindow() {
        removeFlags();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeFlags();
    }

    @Override
    public void onBackPressed() {
        discardMedia();
        if (getPrefs().isFoldingEnabled()) {
            closeWindow();
        } else {
            Toast.makeText(MissedCallDialogActivity.this, getString(R.string.select_one_of_item), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void call() {
        makeCall();
    }

    private void makeCall() {
        if (Permissions.checkPermission(this, Permissions.CALL_PHONE)) {
            TelephonyUtil.makeCall(mMissedCall.getNumber(), MissedCallDialogActivity.this);
            removeMissed();
        } else {
            Permissions.requestPermission(this, CALL_PERM, Permissions.CALL_PHONE);
        }
    }

    @Override
    protected void delay() {
        closeWindow();
    }

    @Override
    protected void cancel() {
        sendSMS();
    }

    private void sendSMS() {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setType("vnd.android-dir/mms-sms");
        sendIntent.putExtra("address", mMissedCall.getNumber());
        startActivity(Intent.createChooser(sendIntent, "SMS:"));
        removeMissed();
    }

    @Override
    protected void favourite() {
        closeWindow();
    }

    @Override
    protected void ok() {
        removeMissed();
    }

    private void removeMissed() {
        if (mMissedCall != null) {
            viewModel.deleteMissedCall(mMissedCall);
        }
    }

    @Override
    protected void showSendingError() {
        binding.remText.setText(getString(R.string.error_sending));
        binding.buttonCall.setImageResource(R.drawable.ic_refresh);
        if (binding.buttonCall.getVisibility() == View.GONE) {
            binding.buttonCall.show();
        }
    }

    @Override
    protected String getMelody() {
        return null;
    }

    @Override
    protected boolean isScreenResumed() {
        return mIsResumed;
    }

    @Override
    protected boolean isVibrate() {
        return getPrefs().isVibrateEnabled();
    }

    @Override
    protected String getSummary() {
        return mMissedCall.getNumber();
    }

    @Override
    protected String getUuId() {
        return null;
    }

    @Override
    protected int getId() {
        return mMissedCall.getUniqueId();
    }

    @Override
    protected int getLedColor() {
        return LED.getLED(getPrefs().getLedColor());
    }

    @Override
    protected boolean isAwakeDevice() {
        return getPrefs().isDeviceAwakeEnabled();
    }

    @Override
    protected boolean isGlobal() {
        return false;
    }

    @Override
    protected boolean isUnlockDevice() {
        return getPrefs().isDeviceUnlockEnabled();
    }

    @Override
    protected int getMaxVolume() {
        return getPrefs().getLoudness();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 0) return;
        switch (requestCode) {
            case CALL_PERM:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeCall();
                }
                break;
        }
    }

    private void showMissedReminder(String name) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER);
        builder.setContentTitle(name);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        if (getPrefs().isManualRemoveEnabled()) {
            builder.setOngoing(false);
        } else {
            builder.setOngoing(true);
        }
        String appName;
        if (Module.isPro()) {
            appName = getString(R.string.app_name_pro);
            if (getPrefs().isLedEnabled()) {
                builder.setLights(getLedColor(), 500, 1000);
            }
        } else {
            appName = getString(R.string.app_name);
        }
        builder.setContentText(appName);
        if (Module.isLollipop()) {
            builder.setSmallIcon(R.drawable.ic_call_white_24dp);
        } else {
            builder.setSmallIcon(R.drawable.ic_call_nv_white);
        }
        if (Module.isLollipop()) {
            builder.setColor(ViewUtils.getColor(this, R.color.bluePrimary));
        }
        if (getSound() != null && !isScreenResumed() && (!SuperUtil.isDoNotDisturbEnabled(this) ||
                (SuperUtil.checkNotificationPermission(this) && getPrefs().isSoundInSilentModeEnabled()))) {
            Uri soundUri = getSoundUri();
            getSound().playAlarm(soundUri, getPrefs().isInfiniteSoundEnabled());
        }
        if (isVibrate()) {
            long[] pattern;
            if (getPrefs().isInfiniteVibrateEnabled()) {
                pattern = new long[]{150, 86400000};
            } else {
                pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            }
            builder.setVibrate(pattern);
        }
        boolean isWear = getPrefs().isWearEnabled();
        if (isWear && Module.isJellyMR2()) {
            builder.setOnlyAlertOnce(true);
            builder.setGroup("GROUP");
            builder.setGroupSummary(true);
        }
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(this);
        mNotifyMgr.notify(getId(), builder.build());
        if (isWear) {
            showWearNotification(appName);
        }
    }
}
