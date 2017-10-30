package com.elementary.tasks.core.utils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

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

public class SoundStackHolder {

    private static final String TAG = "SoundStackHolder";

    private static SoundStackHolder instance;
    private Map<Class, Sound> stack = new LinkedHashMap<>();
    private int mMusicVolume = - 1;
    private int mAlarmVolume = - 1;
    private int mNotificationVolume = - 1;
    @Nullable
    private AudioManager audioManager;

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
        if (audioManager != null) return;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) audioManager.setMode(AudioManager.MODE_NORMAL);
    }

    @Nullable
    public AudioManager getAudioManager() {
        return audioManager;
    }

    public int getDefaultStreamVolume(int stream) {
        switch (stream) {
            case AudioManager.STREAM_ALARM:
                return mAlarmVolume;
            case AudioManager.STREAM_MUSIC:
                return mMusicVolume;
            case AudioManager.STREAM_NOTIFICATION:
                return mNotificationVolume;
        }
        return 0;
    }

    public synchronized void saveDefaultVolume() {
        if (mMusicVolume == -1 && mNotificationVolume == -1 && mAlarmVolume == -1) {
            if (audioManager != null) {
                mMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                mAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                mNotificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
            }
        }
    }

    public synchronized void restoreDefaultVolume() {
        if (isLast()) {
            if (audioManager != null) {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, mAlarmVolume, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mMusicVolume, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, mNotificationVolume, 0);
            }
            mMusicVolume = -1;
            mNotificationVolume = -1;
            mAlarmVolume = -1;
        }
    }

    private boolean isLast() {
        return stack.isEmpty();
    }

    public void addToStack(Activity activity, Sound sound) {
        LogUtil.d(TAG, "addToStack: " + activity.getClass());
        if (!stack.containsKey(activity.getClass())) {
            stack.put(activity.getClass(), sound);
        }
    }

    public Sound getFromStack(Activity activity) {
        LogUtil.d(TAG, "getFromStack: " + activity.getClass());
        if (stack.containsKey(activity.getClass())) {
            return stack.get(activity.getClass());
        }
        return null;
    }

    public void removeFromStack(Activity activity) {
        LogUtil.d(TAG, "removeFromStack: " + activity.getClass());
        if (stack.containsKey(activity.getClass())) {
            stack.remove(activity.getClass());
        }
    }

    public boolean hasInStack(Activity activity) {
        LogUtil.d(TAG, "hasInStack: " + activity.getClass());
        return stack.containsKey(activity.getClass());
    }
}
