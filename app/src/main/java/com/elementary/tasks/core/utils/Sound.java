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

package com.elementary.tasks.core.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.File;
import java.io.IOException;

public class Sound {
    
    private Context mContext;
    private MediaPlayer mMediaPlayer;
    private boolean isPaused;
    private String lastFile;

    public Sound(Context context){
        this.mContext = context;
    }

    public void stop(){
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            isPaused = false;
        }
    }

    public void pause(){
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            isPaused = true;
        }
    }

    public void resume(){
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            isPaused = false;
        }
    }

    public boolean isPaused(){
        return isPaused;
    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public boolean isSameFile(String path) {
        return lastFile != null && path.equalsIgnoreCase(lastFile);
    }

    public void play(String path){
        lastFile = path;
        File file = new File(path);
        Uri soundUri = Uri.fromFile(file);
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mContext, soundUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setLooping(false);
        mMediaPlayer.setOnPreparedListener(MediaPlayer::start);
        try {
            mMediaPlayer.prepareAsync();
        } catch (IllegalStateException e){
            e.printStackTrace();
        }
    }

    public void playAlarm(Uri path, boolean looping){
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mContext, path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SharedPrefs prefs = SharedPrefs.getInstance(mContext);
        boolean isSystem = prefs.getBoolean(Prefs.SYSTEM_VOLUME);
        if (isSystem) {
            int stream = prefs.getInt(Prefs.SOUND_STREAM);
            mMediaPlayer.setAudioStreamType(stream);
        } else mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mMediaPlayer.setLooping(looping);
        mMediaPlayer.setOnPreparedListener(MediaPlayer::start);
        try {
            mMediaPlayer.prepareAsync();
        } catch (IllegalStateException e){
            e.printStackTrace();
        }
    }

    public void playAlarm(AssetFileDescriptor afd, boolean looping){
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
        SharedPrefs prefs = SharedPrefs.getInstance(mContext);
        boolean isSystem = prefs.getBoolean(Prefs.SYSTEM_VOLUME);
        if (isSystem) {
            int stream = prefs.getInt(Prefs.SOUND_STREAM);
            mMediaPlayer.setAudioStreamType(stream);
        } else mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setLooping(looping);
        mMediaPlayer.setOnPreparedListener(MediaPlayer::start);
        try {
            mMediaPlayer.prepareAsync();
        } catch (IllegalStateException e){
            e.printStackTrace();
        }
    }
}
