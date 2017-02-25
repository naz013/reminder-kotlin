package com.elementary.tasks.birthdays;

import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.backdoor.shared.SharedConst;
import com.elementary.tasks.R;
import com.elementary.tasks.core.BaseNotificationActivity;
import com.elementary.tasks.core.async.BackupTask;
import com.elementary.tasks.core.utils.Configs;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Contacts;
import com.elementary.tasks.core.utils.LED;
import com.elementary.tasks.core.utils.Language;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TelephonyUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.ActivityShowBirthdayBinding;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Calendar;
import java.util.Locale;

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

public class ShowBirthdayActivity extends BaseNotificationActivity {

    private static final int CALL_PERM = 612;
    private static final int SMS_PERM = 613;

    private ActivityShowBirthdayBinding binding;

    private BirthdayItem mBirthdayItem;
    private boolean mIsResumed;
    private String wearMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mIsResumed = getIntent().getBooleanExtra(Constants.INTENT_NOTIFICATION, false);
        mBirthdayItem = RealmDb.getInstance().getBirthday(getIntent().getStringExtra(Constants.INTENT_ID));
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_show_birthday);
        binding.card.setCardBackgroundColor(themeUtil.getCardStyle());
        if (Module.isLollipop()) binding.card.setCardElevation(Configs.CARD_ELEVATION);
        binding.singleContainer.setVisibility(View.VISIBLE);
        loadImage(binding.bgImage);
        colorify(binding.buttonOk, binding.buttonCall, binding.buttonSend);

        binding.buttonOk.setOnClickListener(view -> ok());
        binding.buttonCall.setOnClickListener(view -> call());
        binding.buttonSend.setOnClickListener(view -> sendSMS());

        binding.buttonOk.setImageResource(R.drawable.ic_done_black_24dp);
        binding.buttonCall.setImageResource(R.drawable.ic_call_black_24dp);
        binding.buttonSend.setImageResource(R.drawable.ic_send_black_24dp);

        CircleImageView contactPhoto = binding.contactPhoto;
        contactPhoto.setBorderColor(themeUtil.getColor(themeUtil.colorPrimary()));
        contactPhoto.setVisibility(View.GONE);

        if (TextUtils.isEmpty(mBirthdayItem.getNumber())) {
            mBirthdayItem.setNumber(Contacts.getNumber(mBirthdayItem.getName(), ShowBirthdayActivity.this));
        }
        if (mBirthdayItem.getContactId() == 0) {
            mBirthdayItem.setContactId(Contacts.getIdFromNumber(mBirthdayItem.getNumber(), ShowBirthdayActivity.this));
        }
        Uri photo = Contacts.getPhoto(mBirthdayItem.getContactId());
        if (photo != null) contactPhoto.setImageURI(photo);
        else contactPhoto.setVisibility(View.GONE);
        String years =  TimeUtil.getAgeFormatted(this, mBirthdayItem.getDate());
        RoboTextView userName = binding.userName;
        userName.setText(mBirthdayItem.getName());
        RoboTextView userNumber = (RoboTextView) findViewById(R.id.userNumber);
        RoboTextView userYears = (RoboTextView) findViewById(R.id.userYears);
        userYears.setText(years);
        wearMessage = mBirthdayItem.getName() + "\n" + years;
        if (mBirthdayItem.getNumber() == null || mBirthdayItem.getNumber().matches("noNumber")) {
            binding.buttonCall.setVisibility(View.GONE);
            binding.buttonSend.setVisibility(View.GONE);
            userNumber.setVisibility(View.GONE);
        } else {
            userNumber.setText(mBirthdayItem.getNumber());
        }
        showNotification(TimeUtil.getAge(mBirthdayItem.getDate()), mBirthdayItem.getName());
        if (isTtsEnabled()) {
            startTts();
        }
    }

    public void showNotification(int years, String name){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(name);
        builder.setContentText(TimeUtil.getAgeFormatted(this, years));
        builder.setSmallIcon(R.drawable.ic_cake_white_24dp);
        if (Module.isLollipop()) {
            builder.setColor(ViewUtils.getColor(this, R.color.bluePrimary));
        }
        if (!isScreenResumed()) {
            Uri soundUri = getSoundUri();
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                mSound.playAlarm(soundUri, isBirthdayInfiniteSound());
            } else {
                if (isBirthdaySilentEnabled()) {
                    mSound.playAlarm(soundUri, isBirthdayInfiniteSound());
                }
            }
        }
        if (isVibrate()){
            long[] pattern = new long[]{150, 86400000};
            if (isBirthdayInfiniteVibration()){
                pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            }
            builder.setVibrate(pattern);
        }
        if (Module.isPro()){
            builder.setLights(getLedColor(), 500, 1000);
        }
        boolean isWear = mPrefs.isWearEnabled();
        if (isWear) {
            if (Module.isJellyMR2()) {
                builder.setOnlyAlertOnce(true);
                builder.setGroup("GROUP");
                builder.setGroupSummary(true);
            }
        }
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(this);
        mNotifyMgr.notify(getId(), builder.build());
        if (isWear) {
            showWearNotification(name);
        }
    }

    private boolean isBirthdaySilentEnabled() {
        boolean is = mPrefs.isSoundInSilentModeEnabled();
        if (Module.isPro() && !isGlobal()) {
            is = mPrefs.isBirthdaySilentEnabled();
        }
        return is;
    }

    private boolean isTtsEnabled() {
        boolean is = mPrefs.isTtsEnabled();
        if (Module.isPro() && !isGlobal()) {
            is = mPrefs.isBirthdayTtsEnabled();
        }
        return is;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SuperUtil.isGooglePlayServicesAvailable(this)) {
            mTracker.setScreenName("Birthday Reminder ");
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
        if (mPrefs.isAutoBackupEnabled()) {
            new BackupTask(this).execute();
        }
    }

    @Override
    public void onBackPressed() {
        discardMedia();
        if (mPrefs.isFoldingEnabled()){
            removeFlags();
            finish();
        } else {
            Toast.makeText(ShowBirthdayActivity.this, getString(R.string.select_one_of_item), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void sendDataToWear() {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(SharedConst.WEAR_STOP);
        DataMap map = putDataMapReq.getDataMap();
        map.putBoolean(SharedConst.KEY_STOP_B, true);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    @Override
    protected void call() {
        makeCall();
    }

    private void makeCall() {
        if (Permissions.checkPermission(this, Permissions.CALL_PHONE)) {
            TelephonyUtil.makeCall(mBirthdayItem.getNumber(), this);
            updateBirthday();
        } else {
            Permissions.requestPermission(this, CALL_PERM, Permissions.CALL_PHONE);
        }
    }

    @Override
    protected void delay() {
        close();
    }

    @Override
    protected void cancel() {
        sendSMS();
    }

    private void sendSMS() {
        if (Permissions.checkPermission(ShowBirthdayActivity.this, Permissions.SEND_SMS)) {
            TelephonyUtil.sendSms(mBirthdayItem.getNumber(), ShowBirthdayActivity.this);
            updateBirthday();
        } else {
            Permissions.requestPermission(ShowBirthdayActivity.this, SMS_PERM, Permissions.SEND_SMS);
        }
    }

    @Override
    protected void favourite() {
        close();
    }

    @Override
    protected void ok() {
        updateBirthday();
    }

    private void updateBirthday() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        mBirthdayItem.setShowedYear(year);
        RealmDb.getInstance().saveObject(mBirthdayItem);
        close();
    }

    private void close() {
        removeFlags();
        discardNotification(getId());
        finish();
    }


    @Override
    protected void showSendingError() {
        binding.buttonCall.setImageResource(R.drawable.ic_refresh);
        if (binding.buttonCall.getVisibility() == View.GONE) {
            binding.buttonCall.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected Locale getTtsLocale() {
        Locale locale = new Language().getLocale(this, false);
        if (Module.isPro() && !isGlobal()) {
            locale = new Language().getLocale(this, true);
        }
        return locale;
    }

    @Override
    protected String getMelody() {
        if (Module.isPro() && !isGlobal()) {
            return mPrefs.getBirthdayMelody();
        } else {
            return mPrefs.getMelodyFile();
        }
    }

    @Override
    protected boolean isBirthdayInfiniteVibration() {
        boolean vibrate = mPrefs.isInfiniteVibrateEnabled();
        if (Module.isPro() && !isGlobal()){
            vibrate = mPrefs.isBirthdayInfiniteVibrationEnabled();
        }
        return vibrate;
    }

    @Override
    protected boolean isBirthdayInfiniteSound() {
        boolean isLooping = mPrefs.isInfiniteSoundEnabled();
        if (Module.isPro() && !isGlobal()){
            isLooping = mPrefs.isBirthdayInfiniteSoundEnabled();
        }
        return isLooping;
    }

    @Override
    protected boolean isScreenResumed() {
        return mIsResumed;
    }

    @Override
    protected boolean isVibrate() {
        boolean vibrate = mPrefs.isVibrateEnabled();
        if (Module.isPro() && !isGlobal()){
            vibrate = mPrefs.isBirthdayVibrationEnabled();
        }
        return vibrate;
    }

    @Override
    protected String getSummary() {
        return wearMessage;
    }

    @Override
    protected String getUuId() {
        return mBirthdayItem.getUuId();
    }

    @Override
    protected int getId() {
        return mBirthdayItem.getUniqueId();
    }

    @Override
    protected int getLedColor() {
        int ledColor = LED.getLED(mPrefs.getLedColor());
        if (Module.isPro() && !isGlobal()) {
            ledColor = LED.getLED(mPrefs.getBirthdayLedColor());
        }
        return ledColor;
    }

    @Override
    protected boolean isAwakeDevice() {
        boolean isWake = mPrefs.isDeviceAwakeEnabled();
        if (Module.isPro() && !isGlobal()) {
            isWake = mPrefs.isBirthdayWakeEnabled();
        }
        return isWake;
    }

    @Override
    protected int getMaxVolume() {
        return mPrefs.getLoudness();
    }

    @Override
    protected boolean isGlobal() {
        return mPrefs.isBirthdayGlobalEnabled();
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
            case SMS_PERM:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendSMS();
                }
                break;
        }
    }
}
