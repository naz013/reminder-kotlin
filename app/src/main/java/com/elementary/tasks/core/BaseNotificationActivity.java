package com.elementary.tasks.core;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.backdoor.shared.SharedConst;
import com.elementary.tasks.R;
import com.elementary.tasks.ReminderApp;
import com.elementary.tasks.core.interfaces.SendListener;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Language;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.Sound;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.IOException;

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

public abstract class BaseNotificationActivity extends ThemedActivity {

    private static final String TAG = "BNActivity";
    private static final int MY_DATA_CHECK_CODE = 111;

    protected Sound mSound;
    protected Prefs mPrefs;
    protected Tracker mTracker;
    private GoogleApiClient mGoogleApiClient;
    private TextToSpeech tts;
    private ProgressDialog mSendDialog;

    protected TextToSpeech.OnInitListener mTextToSpeachListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Language().getLocale(ReminderDialogActivity.this, false));
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "This Language is not supported");
                } else {
                    if (!TextUtils.isEmpty(getSummary())) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (Module.isLollipop()) {
                            tts.speak(getSummary(), TextToSpeech.QUEUE_FLUSH, null, null);
                        } else {
                            tts.speak(getSummary(), TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                }
            } else {
                Log.e(TAG, "Initialization Failed!");
            }
        }
    };
    protected SendListener mSendListener = new SendListener() {
        @Override
        public void messageSendResult(boolean isSent) {
            hideProgressDialog();
            if (isSent) {
                finish();
            } else {
                showSendingError();
            }
        }
    };
    protected GoogleApiClient.ConnectionCallbacks mGoogleCallback = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Wearable.DataApi.addListener(mGoogleApiClient, mDataListener);
            boolean silentSMS = mPrefs.isAutoSmsEnabled();
            String type = getType();
            if (type != null && type.contains(Constants.TYPE_MESSAGE) && silentSMS)
                return;

            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(SharedConst.WEAR_REMINDER);
            DataMap map = putDataMapReq.getDataMap();
            map.putString(SharedConst.KEY_TYPE, getType());
            map.putString(SharedConst.KEY_TASK, getSummary());
            map.putInt(SharedConst.KEY_COLOR, themeUtil.colorAccent());
            map.putBoolean(SharedConst.KEY_THEME, themeUtil.isDark());
            map.putBoolean(SharedConst.KEY_REPEAT, buttonCancel.getVisibility() == View.VISIBLE);
            map.putBoolean(SharedConst.KEY_TIMED, buttonDelay.getVisibility() == View.VISIBLE);
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };
    protected DataApi.DataListener mDataListener = new DataApi.DataListener() {
        @Override
        public void onDataChanged(DataEventBuffer dataEventBuffer) {
            Log.d(TAG, "Data received");
            for (DataEvent event : dataEventBuffer) {
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    // DataItem changed
                    DataItem item = event.getDataItem();
                    if (item.getUri().getPath().compareTo(SharedConst.PHONE_REMINDER) == 0) {
                        DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                        int keyCode = dataMap.getInt(SharedConst.REQUEST_KEY);
                        if (keyCode == SharedConst.KEYCODE_OK) {
                            ok();
                        } else if (keyCode == SharedConst.KEYCODE_FAVOURITE) {
                            favourite();
                        } else if (keyCode == SharedConst.KEYCODE_CANCEL) {
                            cancel();
                        } else if (keyCode == SharedConst.KEYCODE_SNOOZE) {
                            delay();
                        } else {
                            call();
                        }
                    }
                }
            }
        }
    };

    protected abstract void call();

    protected abstract void delay();

    protected abstract void cancel();

    protected abstract void favourite();

    protected abstract void ok();

    protected abstract void showSendingError();

    private void hideProgressDialog() {
        if (mSendDialog != null && mSendDialog.isShowing()) {
            mSendDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSound = new Sound(this);
        mPrefs = Prefs.getInstance(this);
        if (mPrefs.isWearEnabled()) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(mGoogleCallback)
                    .build();
        }
        if (SuperUtil.isGooglePlayServicesAvailable(this)) {
            ReminderApp application = (ReminderApp) getApplication();
            mTracker = application.getDefaultTracker();
        }
    }

    protected void startTts() {
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        try {
            startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        } catch (ActivityNotFoundException e){
            e.printStackTrace();
        }
    }

    public void showReminderNotification(Class<Activity> activityClass) {
        Intent notificationIntent = new Intent(this, activityClass);
        notificationIntent.putExtra(Constants.INTENT_ID, getUuId());
        notificationIntent.putExtra("int", 1);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(getSummary());
        builder.setContentIntent(intent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        if (mPrefs.isManualRemoveEnabled()) {
            builder.setOngoing(false);
        } else {
            builder.setOngoing(true);
        }
        String appName;
        if (Module.isPro()) {
            appName = getString(R.string.app_name_pro);
            if (mPrefs.isLedEnabled()) {
                builder.setLights(mPrefs.getLedColor(), 500, 1000);
            }
        } else {
            appName = getString(R.string.app_name);
        }
        builder.setContentText(appName);
        builder.setSmallIcon(R.drawable.ic_notifications_black_24dp);
        if (Module.isLollipop()) {
            builder.setColor(ViewUtils.getColor(this, R.color.bluePrimary));
        }
        if (isScreenResumed()) {
            Uri soundUri = getSoundUri();
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                mSound.playAlarm(soundUri, mPrefs.isInfiniteSoundEnabled());
            } else {
                if (mPrefs.isSoundInSilentModeEnabled()) {
                    mSound.playAlarm(soundUri, mPrefs.isInfiniteSoundEnabled());
                }
            }
        }
        if (isVibrate()) {
            long[] pattern;
            if (mPrefs.isInfiniteVibrateEnabled()) {
                pattern = new long[]{150, 86400000};
            } else {
                pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            }
            builder.setVibrate(pattern);
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
            showWearNotification(appName);
        }
    }

    private Uri getSoundUri() {
        if (!TextUtils.isEmpty(getMelody())) {
            File sound = new File(getMelody());
            return Uri.fromFile(sound);
        } else {
            String defMelody = mPrefs.getMelodyFile();
            if (!TextUtils.isEmpty(defMelody)) {
                File sound = new File(defMelody);
                return Uri.fromFile(sound);
            } else {
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        }
    }

    protected abstract String getMelody();

    protected abstract boolean isScreenResumed();

    private void showWearNotification(String secondaryText) {
        if (Module.isJellyMR2()) {
            final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(this);
            wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
            wearableNotificationBuilder.setContentTitle(getSummary());
            wearableNotificationBuilder.setContentText(secondaryText);
            if (Module.isLollipop()) {
                wearableNotificationBuilder.setColor(ViewUtils.getColor(this, R.color.bluePrimary));
            }
            wearableNotificationBuilder.setOngoing(false);
            wearableNotificationBuilder.setOnlyAlertOnce(true);
            wearableNotificationBuilder.setGroup("GROUP");
            wearableNotificationBuilder.setGroupSummary(false);
            NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(this);
            mNotifyMgr.notify(getId(), wearableNotificationBuilder.build());
        }
    }

    protected void showTTSNotification(Class<Activity> activityClass) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(getSummary());
        if (mPrefs.isFoldingEnabled()) {
            Intent notificationIntent = new Intent(this, activityClass);
            notificationIntent.putExtra(Constants.INTENT_ID, getUuId());
            notificationIntent.putExtra("int", 1);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(intent);
        }
        builder.setAutoCancel(false);
        builder.setPriority(Notification.PRIORITY_MAX);
        if (mPrefs.isManualRemoveEnabled()) {
            builder.setOngoing(false);
        } else {
            builder.setOngoing(true);
        }
        String appName;
        if (Module.isPro()) {
            appName = getString(R.string.app_name_pro);
            if (mPrefs.isLedEnabled()) {
                builder.setLights(mPrefs.getLedColor(), 500, 1000);
            }
        } else {
            appName = getString(R.string.app_name);
        }
        builder.setContentText(appName);
        builder.setSmallIcon(R.drawable.ic_notifications_black_24dp);
        if (Module.isLollipop()) {
            builder.setColor(ViewUtils.getColor(this, R.color.bluePrimary));
        }
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            playDefaultMelody();
        } else {
            if (mPrefs.isSoundInSilentModeEnabled()) {
                playDefaultMelody();
            }
        }
        if (isVibrate()) {
            long[] pattern;
            if (mPrefs.isInfiniteVibrateEnabled()) {
                pattern = new long[]{150, 86400000};
            } else {
                pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            }
            builder.setVibrate(pattern);
        }
        boolean isWear = mPrefs.isWearEnabled();
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

    protected abstract boolean isVibrate();

    protected abstract String getSummary();

    protected abstract String getUuId();

    protected abstract int getId();

    protected abstract int getLedColor();

    private void playDefaultMelody() {
        try {
            AssetFileDescriptor afd = getAssets().openFd("sounds/beep.mp3");
            mSound.playAlarm(afd, false);
        } catch (IOException e) {
            e.printStackTrace();
            mSound.playAlarm(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), false);
        }
    }
}
