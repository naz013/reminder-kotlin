package com.elementary.tasks.core.utils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;

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

    public synchronized void saveDefaultVolume(Context context) {
        if (mMusicVolume == -1 && mNotificationVolume == -1 && mAlarmVolume == -1) {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                mMusicVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                mAlarmVolume = am.getStreamVolume(AudioManager.STREAM_ALARM);
                mNotificationVolume = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
            }
        }
    }

    public synchronized void restoreDefaultVolume(Context context) {
        if (isLast()) {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                am.setStreamVolume(AudioManager.STREAM_ALARM, mAlarmVolume, 0);
                am.setStreamVolume(AudioManager.STREAM_MUSIC, mMusicVolume, 0);
                am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, mNotificationVolume, 0);
            }
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
