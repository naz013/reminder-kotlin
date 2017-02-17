package com.elementary.tasks.missed_calls;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.backdoor.shared.SharedConst;
import com.elementary.tasks.R;
import com.elementary.tasks.core.BaseNotificationActivity;
import com.elementary.tasks.core.services.MissedCallReceiver;
import com.elementary.tasks.core.utils.Configs;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Contacts;
import com.elementary.tasks.core.utils.LED;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TelephonyUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.databinding.ActivityReminderDialogBinding;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.sql.Date;

import de.hdodenhof.circleimageview.CircleImageView;

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

    private static final int CALL_PERM = 612;

    private ActivityReminderDialogBinding binding;

    private MissedCallReceiver alarm = new MissedCallReceiver();

    private CallItem mCallItem;
    private boolean mIsResumed;
    private String wearMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mIsResumed = getIntent().getBooleanExtra(Constants.INTENT_NOTIFICATION, false);
        mCallItem = RealmDb.getInstance().getMissedCall(getIntent().getStringExtra(Constants.INTENT_ID));
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder_dialog);
        binding.card.setCardBackgroundColor(themeUtil.getCardStyle());
        if (Module.isLollipop()) binding.card.setCardElevation(Configs.CARD_ELEVATION);
        binding.singleContainer.setVisibility(View.VISIBLE);
        binding.container.setVisibility(View.GONE);
        binding.subjectContainer.setVisibility(View.GONE);
        loadImage(binding.bgImage);
        FloatingActionButton buttonCancel = (FloatingActionButton) findViewById(R.id.buttonCancel);
        FloatingActionButton buttonCall = (FloatingActionButton) findViewById(R.id.buttonCall);
        FloatingActionButton buttonDelay = (FloatingActionButton) findViewById(R.id.buttonDelay);
        FloatingActionButton buttonDelayFor = (FloatingActionButton) findViewById(R.id.buttonDelayFor);
        FloatingActionButton buttonNotification = (FloatingActionButton) findViewById(R.id.buttonNotification);
        colorify(binding.buttonOk, buttonCall, buttonCancel, buttonDelay, buttonDelayFor,
                buttonNotification, binding.buttonEdit);
        buttonDelay.setVisibility(View.GONE);
        buttonDelayFor.setVisibility(View.GONE);
        buttonNotification.setVisibility(View.GONE);
        binding.buttonEdit.setVisibility(View.GONE);

        binding.buttonOk.setImageResource(R.drawable.ic_done_black_24dp);
        buttonCancel.setImageResource(R.drawable.ic_clear_black_24dp);
        buttonCall.setImageResource(R.drawable.ic_call_black_24dp);

        CircleImageView contactPhoto = binding.contactPhoto;
        contactPhoto.setBorderColor(themeUtil.getColor(themeUtil.colorPrimary()));
        contactPhoto.setVisibility(View.GONE);

        TextView remText = (TextView) findViewById(R.id.remText);
        String formattedTime = TimeUtil.getTime(new Date(mCallItem.getDateTime()), mPrefs.is24HourFormatEnabled());
        String name = Contacts.getNameFromNumber(mCallItem.getNumber(), this);
        wearMessage = name + "\n" + mCallItem.getNumber();
        if (mCallItem.getNumber() != null) {
            long conID = Contacts.getIdFromNumber(mCallItem.getNumber(), this);
            Uri photo = Contacts.getPhoto(conID);
            if (photo != null) {
                contactPhoto.setImageURI(photo);
            } else {
                contactPhoto.setVisibility(View.GONE);
            }
            remText.setText(R.string.missed_call);
            binding.contactInfo.setText(wearMessage);
            binding.actionDirect.setText(R.string.from);
            binding.someView.setText(R.string.last_called);
            binding.messageView.setText(formattedTime);
        }
        buttonCancel.setOnClickListener(v -> sendSMS());
        binding.buttonOk.setOnClickListener(v -> ok());
        buttonCall.setOnClickListener(v -> call());
        showMissedReminder(mCallItem, name == null || name.matches("") ? mCallItem.getNumber() : name);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SuperUtil.isGooglePlayServicesAvailable(this)) {
            mTracker.setScreenName("Missed call Reminder ");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        if (mPrefs.isWearEnabled()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPrefs.isWearEnabled()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, mDataListener);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeFlags();
    }

    @Override
    public void onBackPressed() {
        discardMedia();
        if (mPrefs.isFoldingEnabled()){
            removeFlags();
            finish();
        } else {
            Toast.makeText(MissedCallDialogActivity.this, getString(R.string.select_one_of_item), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void sendDataToWear() {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(SharedConst.WEAR_BIRTHDAY);
        DataMap map = putDataMapReq.getDataMap();
        map.putString(SharedConst.KEY_TASK, wearMessage);
        map.putInt(SharedConst.KEY_COLOR, themeUtil.colorAccent());
        map.putBoolean(SharedConst.KEY_THEME, themeUtil.isDark());
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    @Override
    protected void call() {
        removeMissed();
        makeCall();
    }

    private void makeCall() {
        if (Permissions.checkPermission(this, Permissions.CALL_PHONE)) {
            TelephonyUtil.makeCall(mCallItem.getNumber(), MissedCallDialogActivity.this);
            removeFlags();
            finish();
        } else {
            Permissions.requestPermission(this, CALL_PERM, Permissions.CALL_PHONE);
        }
    }

    @Override
    protected void delay() {
        removeFlags();
        finish();
    }

    @Override
    protected void cancel() {
        sendSMS();
        removeFlags();
        finish();
    }

    private void sendSMS() {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setType("vnd.android-dir/mms-sms");
        sendIntent.putExtra("address", mCallItem.getNumber());
        startActivity(Intent.createChooser(sendIntent, "SMS:"));
        removeMissed();
        removeFlags();
        finish();
    }

    @Override
    protected void favourite() {
        removeFlags();
        finish();
    }

    @Override
    protected void ok() {
        removeMissed();
        removeFlags();
        finish();
    }

    private void removeMissed() {
        alarm.cancelAlarm(getApplicationContext(), getId());
        discardNotification(getId());
        RealmDb.getInstance().deleteMissedCall(mCallItem);
    }

    @Override
    protected void showSendingError() {
        binding.remText.setText(getString(R.string.error_sending));
        binding.buttonCall.setImageResource(R.drawable.ic_refresh);
        if (binding.buttonCall.getVisibility() == View.GONE) {
            binding.buttonCall.setVisibility(View.VISIBLE);
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
        return mPrefs.isVibrateEnabled();
    }

    @Override
    protected String getSummary() {
        return mCallItem.getNumber();
    }

    @Override
    protected String getUuId() {
        return null;
    }

    @Override
    protected int getId() {
        return mCallItem.getUniqueId();
    }

    @Override
    protected int getLedColor() {
        return LED.getLED(mPrefs.getLedColor());
    }

    @Override
    protected boolean isAwakeDevice() {
        return mPrefs.isDeviceAwakeEnabled();
    }

    @Override
    protected boolean isGlobal() {
        return false;
    }

    @Override
    protected boolean isUnlockDevice() {
        return mPrefs.isDeviceUnlockEnabled();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CALL_PERM:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeCall();
                }
                break;
        }
    }
}
