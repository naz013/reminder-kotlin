package com.elementary.tasks.core.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
public class Sound {

    private Context mContext;
    @Nullable
    private MediaPlayer mMediaPlayer;
    private boolean isPaused;
    @Nullable
    private String lastFile;
    @Nullable
    private Ringtone mRingtone;
    private boolean isDone;
    @Nullable
    private PlaybackCallback mCallback;
    @NonNull
    private Handler mRingtoneHandler = new Handler();
    @NonNull
    private Runnable mRingtoneRunnable = new Runnable() {
        @Override
        public void run() {
            mRingtoneHandler.removeCallbacks(mRingtoneRunnable);
            if (mRingtone != null && mRingtone.isPlaying()) {
                mRingtoneHandler.postDelayed(mRingtoneRunnable, 100);
            } else {
                mRingtone = null;
                notifyFinish();
            }
        }
    };

    public Sound(Context context) {
        this.mContext = context;
    }

    public void setCallback(@Nullable PlaybackCallback callback) {
        this.mCallback = callback;
    }

    public void stop(boolean notify) {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            } catch (IllegalStateException ignored) {
            }
            isPaused = false;
        }
        if (mRingtone != null) {
            mRingtoneHandler.removeCallbacks(mRingtoneRunnable);
            try {
                mRingtone.stop();
            } catch (Exception ignored) {
            }
        }
        if (notify) notifyFinish();
    }

    public void pause() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.pause();
            } catch (IllegalStateException ignored) {
            }
            isPaused = true;
        }
    }

    public void resume() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.start();
            } catch (IllegalStateException ignored) {
            }
            isPaused = false;
        }
    }

    public boolean isDone() {
        return isDone;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isPlaying() {
        try {
            return mMediaPlayer != null && mMediaPlayer.isPlaying();
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public boolean isSameFile(String path) {
        return lastFile != null && path.equalsIgnoreCase(lastFile);
    }

    public void play(String path) {
        if (!Permissions.checkPermission(mContext, Permissions.READ_EXTERNAL)) return;
        lastFile = path;
        stop(false);
        mMediaPlayer = new MediaPlayer();
        try {
            File file = new File(path);
            mMediaPlayer.setDataSource(mContext, Uri.fromFile(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Module.isLollipop()) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .build();
            mMediaPlayer.setAudioAttributes(attributes);
        } else {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        mMediaPlayer.setLooping(false);
        mMediaPlayer.setOnPreparedListener(mp -> {
            notifyStart();
            mp.start();
        });
        mMediaPlayer.setOnCompletionListener(mp -> notifyFinish());
        mMediaPlayer.setOnErrorListener((mp, what, extra) -> {
            notifyFinish();
            return false;
        });
        try {
            mMediaPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void notifyFinish() {
        isDone = true;
        if (mCallback != null) {
            mCallback.onFinish();
        }
    }

    public void playAlarm(Uri path, boolean looping) {
        if (isPlaying() && !Permissions.checkPermission(mContext, Permissions.READ_EXTERNAL)) {
            return;
        }
        stop(false);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mContext, path);
            Prefs prefs = Prefs.getInstance(mContext);
            int stream = AudioManager.STREAM_MUSIC;
            if (prefs.isSystemLoudnessEnabled()) {
                stream = prefs.getSoundStream();
            }
            if (Module.isLollipop()) {
                AudioAttributes attributes = new AudioAttributes.Builder()
                        .setLegacyStreamType(stream)
                        .build();
                mMediaPlayer.setAudioAttributes(attributes);
            } else {
                mMediaPlayer.setAudioStreamType(stream);
            }
            mMediaPlayer.setLooping(looping);
            mMediaPlayer.setOnPreparedListener(mp -> {
                notifyStart();
                mp.start();
            });
            mMediaPlayer.setOnCompletionListener(mp -> notifyFinish());
            mMediaPlayer.setOnErrorListener((mp, what, extra) -> {
                notifyFinish();
                return false;
            });
            try {
                mMediaPlayer.prepareAsync();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            playRingtone(path);
        }
    }

    private void playRingtone(Uri path) {
        notifyStart();
        mRingtone = RingtoneManager.getRingtone(mContext, path);
        mRingtone.play();
        mRingtoneHandler.postDelayed(mRingtoneRunnable, 100);
    }

    public void playAlarm(AssetFileDescriptor afd) {
        if (!Permissions.checkPermission(mContext, Permissions.READ_EXTERNAL)) return;
        stop(false);
        if (isDone) {
            return;
        }
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Prefs prefs = Prefs.getInstance(mContext);
        int stream = AudioManager.STREAM_MUSIC;
        if (prefs.isSystemLoudnessEnabled()) {
            stream = prefs.getSoundStream();
        }
        if (Module.isLollipop()) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setLegacyStreamType(stream)
                    .build();
            mMediaPlayer.setAudioAttributes(attributes);
        } else {
            mMediaPlayer.setAudioStreamType(stream);
        }
        mMediaPlayer.setLooping(false);
        mMediaPlayer.setOnPreparedListener(mp -> {
            notifyStart();
            mp.start();
        });
        mMediaPlayer.setOnCompletionListener(mp -> notifyFinish());
        mMediaPlayer.setOnErrorListener((mp, what, extra) -> {
            notifyFinish();
            return false;
        });
        try {
            mMediaPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void notifyStart() {
        if (mCallback != null) {
            mCallback.onStart();
        }
    }

    public static boolean isDefaultMelody(String defMelody) {
        return defMelody.equals(Constants.DEFAULT);
    }

    public interface PlaybackCallback {
        void onStart();
        void onFinish();
    }
}
