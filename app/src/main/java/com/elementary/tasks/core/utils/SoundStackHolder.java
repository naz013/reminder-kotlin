package com.elementary.tasks.core.utils;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import timber.log.Timber;

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
public class SoundStackHolder implements Sound.PlaybackCallback {

    private static SoundStackHolder instance;
    @Nullable
    private Sound mSound;

    private int mMusicVolume = - 1;
    private int mAlarmVolume = - 1;
    private int mNotificationVolume = - 1;

    private boolean isDoNotDisturbEnabled;
    private boolean isHeadset;
    private boolean isSystemLoudnessEnabled;
    private boolean isIncreasingLoudnessEnabled;
    private boolean hasDefaultSaved;
    private boolean hasVolumePermission;

    @Nullable
    private AudioManager mAudioManager;
    @NonNull
    private Handler mHandler = new Handler();

    private int mStreamVol;
    private int mVolume;
    private int mStream;
    private int mMaxVolume;
    private int mSystemStream;

    @NonNull
    private Runnable mVolumeIncrease = new Runnable() {
        @Override
        public void run() {
            Timber.d("mVolumeIncrease -> run: " + mVolume + ", " + mStreamVol);
            if (mVolume < mStreamVol) {
                mVolume++;
                mHandler.postDelayed(mVolumeIncrease, 750);
                if (mAudioManager != null) mAudioManager.setStreamVolume(mStream, mVolume, 0);
            } else mHandler.removeCallbacks(mVolumeIncrease);
        }
    };

    private SoundStackHolder() {
    }

    public static SoundStackHolder getInstance() {
        if (instance == null) {
            synchronized (SoundStackHolder.class) {
                if (instance == null) {
                    instance = new SoundStackHolder();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        isHeadset = SuperUtil.isHeadsetUsing(context);
        hasVolumePermission = SuperUtil.hasVolumePermission(context);
        isSystemLoudnessEnabled = Prefs.getInstance(context).isSystemLoudnessEnabled();
        isIncreasingLoudnessEnabled = Prefs.getInstance(context).isIncreasingLoudnessEnabled();
        if (isSystemLoudnessEnabled) mSystemStream = Prefs.getInstance(context).getSoundStream();
        if (mAudioManager != null) return;

        if (mSound != null) mSound.stop(true);
        else mSound = new Sound(context);

        mSound.setCallback(this);
        mAudioManager = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManager != null && Permissions.checkPermission(context, Permissions.BLUETOOTH)) mAudioManager.setMode(AudioManager.MODE_NORMAL);
        isDoNotDisturbEnabled = SuperUtil.isDoNotDisturbEnabled(context);
    }

    public void setMaxVolume(int maxVolume) {
        this.mMaxVolume = maxVolume;
    }

    @Nullable
    public Sound getSound() {
        return mSound;
    }

    private synchronized void saveDefaultVolume() {
        Timber.d("saveDefaultVolume: %s", hasDefaultSaved);
        if (!hasDefaultSaved && mAudioManager != null) {
            mMusicVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mAlarmVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            mNotificationVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
            hasDefaultSaved = true;
        }
    }

    private synchronized void restoreDefaultVolume() {
        Timber.d("restoreDefaultVolume: " + hasDefaultSaved + ", doNot: " + isDoNotDisturbEnabled + ", am " + mAudioManager);
        if (hasDefaultSaved && !isDoNotDisturbEnabled) {
            if (mAudioManager != null) {
                try {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mAlarmVolume, 0);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mMusicVolume, 0);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, mNotificationVolume, 0);
                } catch (SecurityException ignored) {
                }
            }
            mMusicVolume = -1;
            mNotificationVolume = -1;
            mAlarmVolume = -1;
        }
        hasDefaultSaved = false;
    }

    @Override
    public void onFinish() {
        cancelIncreaseSound();
        restoreDefaultVolume();
    }

    @Override
    public void onStart() {
        saveDefaultVolume();
        setPlayerVolume();
    }

    private void setPlayerVolume() {
        cancelIncreaseSound();
        if (isHeadset) return;
        if (!hasVolumePermission) return;
        if (mAudioManager == null) return;

        if (isSystemLoudnessEnabled) mStream = mSystemStream;
        else mStream = AudioManager.STREAM_MUSIC;

        float volPercent = (float) mMaxVolume / Configs.MAX_VOLUME;
        int maxVol = mAudioManager.getStreamMaxVolume(mStream);
        mStreamVol = (int) (maxVol * volPercent);
        mVolume = mStreamVol;
        if (isIncreasingLoudnessEnabled) {
            mVolume = 0;
            mHandler.postDelayed(mVolumeIncrease, 750);
        }
        mAudioManager.setStreamVolume(mStream, mVolume, 0);
    }

    public void cancelIncreaseSound() {
        mHandler.removeCallbacks(mVolumeIncrease);
    }
}
