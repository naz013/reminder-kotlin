package com.elementary.tasks.core;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.elementary.tasks.R;
import com.elementary.tasks.core.interfaces.SendListener;
import com.elementary.tasks.core.utils.BlurTransformation;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Language;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Sound;
import com.elementary.tasks.core.utils.SoundStackHolder;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.utils.UriUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.views.TextDrawable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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

    @Nullable
    private TextToSpeech tts;
    @Nullable
    private ProgressDialog mSendDialog;

    private static AtomicInteger instanceCount = new AtomicInteger(0);

    @NonNull
    protected TextToSpeech.OnInitListener mTextToSpeechListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            LogUtil.d(TAG, "onInit: ");
            if (status == TextToSpeech.SUCCESS && tts != null) {
                int result = tts.setLanguage(getTtsLocale());
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    LogUtil.d(TAG, "This Language is not supported");
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
                LogUtil.d(TAG, "Initialization Failed!");
            }
        }
    };
    @NonNull
    protected SendListener mSendListener = isSent -> {
        hideProgressDialog();
        if (isSent) {
            finish();
        } else {
            showSendingError();
        }
    };

    protected abstract void call();

    protected abstract void delay();

    protected abstract void cancel();

    protected abstract void favourite();

    protected abstract void ok();

    protected abstract void showSendingError();

    protected abstract String getMelody();

    protected abstract boolean isScreenResumed();

    protected abstract boolean isVibrate();

    protected abstract String getSummary();

    protected abstract String getUuId();

    protected abstract int getId();

    protected abstract int getLedColor();

    protected abstract boolean isAwakeDevice();

    protected abstract boolean isUnlockDevice();

    protected abstract boolean isGlobal();

    protected abstract int getMaxVolume();

    @Nullable
    protected Sound getSound() {
        return SoundStackHolder.getInstance().getSound();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SoundStackHolder.getInstance().init(this);
        int current = instanceCount.incrementAndGet();
        LogUtil.d(TAG, "onCreate: " + current + ", " + TimeUtil.getFullDateTime(System.currentTimeMillis(), true, true));
    }

    protected void init() {
        setUpScreenOptions();
        SoundStackHolder.getInstance().setMaxVolume(getMaxVolume());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        int left = instanceCount.decrementAndGet();
        LogUtil.d(TAG, "onDestroy: " + left);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SoundStackHolder.getInstance().cancelIncreaseSound();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            discardMedia();
        }
        return super.onTouchEvent(event);
    }

    protected void setTextDrawable(FloatingActionButton button, String text) {
        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .textColor(Color.BLACK)
                .useFont(Typeface.MONOSPACE)
                .fontSize(30)
                .bold()
                .toUpperCase()
                .endConfig()
                .buildRound(text, Color.TRANSPARENT);
        button.setImageDrawable(drawable);
    }

    protected void colorify(FloatingActionButton... fab) {
        for (FloatingActionButton button : fab) {
            button.setBackgroundTintList(ViewUtils.getFabState(this, getThemeUtil().colorAccent(), getThemeUtil().colorAccent()));
        }
    }

    protected void loadImage(ImageView imageView) {
        imageView.setVisibility(View.GONE);
        String imagePrefs = getPrefs().getReminderImage();
        boolean blur = getPrefs().isBlurEnabled();
        LogUtil.d(TAG, "loadImage: " + imagePrefs + ", blur " + blur);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = (int) (metrics.heightPixels * 0.75);
        if (imagePrefs == null || imagePrefs.matches(Constants.DEFAULT)) {
            if (blur && Module.isPro()) {
                Glide.with(this)
                        .load(R.drawable.photo)
                        .apply(RequestOptions.bitmapTransform(new BlurTransformation(15, 2)))
                        .apply(RequestOptions.overrideOf(width, height))
                        .into(imageView);
            } else {
                Glide.with(this)
                        .load(R.drawable.photo)
                        .apply(RequestOptions.overrideOf(width, height))
                        .into(imageView);
            }
            imageView.setVisibility(View.VISIBLE);
        } else if (imagePrefs.matches(Constants.NONE)) {
            imageView.setVisibility(View.GONE);
        } else {
            if (blur && Module.isPro()) {
                Glide.with(this)
                        .load(Uri.parse(imagePrefs))
                        .apply(RequestOptions.bitmapTransform(new BlurTransformation(15, 2)))
                        .apply(RequestOptions.overrideOf(width, height))
                        .into(imageView);
            } else {
                Glide.with(this)
                        .load(Uri.parse(imagePrefs))
                        .apply(RequestOptions.overrideOf(width, height))
                        .into(imageView);
            }
            imageView.setVisibility(View.VISIBLE);
        }
    }

    private void setUpScreenOptions() {
        boolean isFull = getPrefs().isDeviceUnlockEnabled();
        boolean isWake = getPrefs().isDeviceAwakeEnabled();
        if (!isGlobal()) {
            isFull = isUnlockDevice();
            isWake = isAwakeDevice();
        }
        if (isFull) {
            runOnUiThread(() -> getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD));
        }
        if (isWake) {
            PowerManager.WakeLock screenLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "reminder:ReminderAPPTAG");
            screenLock.acquire();
            screenLock.release();
        }
    }

    protected void removeFlags() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, mTextToSpeechListener);
            } else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                try {
                    startActivity(installTTSIntent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void startTts() {
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        try {
            startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected boolean isBirthdayInfiniteVibration() {
        return true;
    }

    protected boolean isBirthdayInfiniteSound() {
        return true;
    }

    protected void showMissedReminder(String name) {
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

    protected final void showProgressDialog(String message) {
        hideProgressDialog();
        mSendDialog = ProgressDialog.show(this, null, message, true, false);
    }

    protected final void hideProgressDialog() {
        if (mSendDialog != null && mSendDialog.isShowing()) {
            mSendDialog.dismiss();
        }
    }

    protected Locale getTtsLocale() {
        LogUtil.d(TAG, "getTtsLocale: ");
        return new Language().getLocale(this, false);
    }

    protected Uri getSoundUri() {
        if (!TextUtils.isEmpty(getMelody()) && !Sound.isDefaultMelody(getMelody())) {
            return UriUtil.getUri(this, getMelody());
        } else {
            String defMelody = getPrefs().getMelodyFile();
            if (!TextUtils.isEmpty(defMelody) && !Sound.isDefaultMelody(defMelody)) {
                File sound = new File(defMelody);
                if (sound.exists()) {
                    return UriUtil.getUri(this, sound);
                }
            }
        }
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }

    protected void discardNotification(int id) {
        discardMedia();
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(this);
        mNotifyMgr.cancel(id);
    }

    protected void discardMedia() {
        if (getSound() != null) getSound().stop(true);
    }

    protected void showWearNotification(String secondaryText) {
        LogUtil.d(TAG, "showWearNotification: ");
        if (Module.isJellyMR2()) {
            final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER);
            wearableNotificationBuilder.setSmallIcon(R.drawable.ic_notification_nv_white);
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
}
