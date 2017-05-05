package com.elementary.tasks.core.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

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
    private MediaPlayer mMediaPlayer;
    private boolean isPaused;
    private String lastFile;
    private Ringtone ringtone;
    private boolean isDone;
    private boolean isSaved;

    public Sound(Context context) {
        this.mContext = context;
    }

    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            isPaused = false;
        }
        if (ringtone != null) {
            ringtone.stop();
        }
    }

    public void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            isPaused = true;
        }
    }

    public void resume() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            isPaused = false;
        }
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public boolean isDone() {
        return isDone;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public boolean isSameFile(String path) {
        return lastFile != null && path.equalsIgnoreCase(lastFile);
    }

    public void play(String path) {
        lastFile = path;
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        mMediaPlayer = new MediaPlayer();
        try {
            File file = new File(path);
            mMediaPlayer.setDataSource(mContext, Uri.fromFile(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setLooping(false);
        mMediaPlayer.setOnPreparedListener(MediaPlayer::start);
        try {
            mMediaPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void playAlarm(Uri path, boolean looping) {
        if (isPlaying()) {
            return;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mContext, path);
        } catch (IOException e) {
            ringtone = RingtoneManager.getRingtone(mContext, path);
            ringtone.play();
        }

        Prefs prefs = Prefs.getInstance(mContext);
        if (prefs.isSystemLoudnessEnabled()) {
            mMediaPlayer.setAudioStreamType(prefs.getSoundStream());
        } else {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }

        mMediaPlayer.setLooping(looping);
        mMediaPlayer.setOnPreparedListener(MediaPlayer::start);
        mMediaPlayer.setOnCompletionListener(mp -> isDone = true);
        try {
            mMediaPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void playAlarm(AssetFileDescriptor afd, boolean looping) {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
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
        if (prefs.isSystemLoudnessEnabled()) {
            mMediaPlayer.setAudioStreamType(prefs.getSoundStream());
        } else {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        mMediaPlayer.setLooping(looping);
        mMediaPlayer.setOnPreparedListener(MediaPlayer::start);
        try {
            mMediaPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public static boolean isDefaultMelody(String defMelody) {
        return defMelody.equals(Constants.DEFAULT);
    }
}
