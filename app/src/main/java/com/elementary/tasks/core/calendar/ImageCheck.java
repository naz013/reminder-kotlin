package com.elementary.tasks.core.calendar;

import android.os.Environment;

import java.io.File;

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

public final class ImageCheck {

    public static final String BASE_URL = "https://unsplash.it/1920/1080?image=";
    private long[] photos = new long[]{
            227,
            226,
            11,
            25,
            33,
            10,
            16,
            17,
            44,
            71,
            95,
            132};

    private static ImageCheck instance;

    private ImageCheck(){
    }

    public static ImageCheck getInstance() {
        if (instance == null) {
            instance = new ImageCheck();
        }
        return instance;
    }

    public String getImage(int month, long id){
        String res = null;
        File sdPath = Environment.getExternalStorageDirectory();
        File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + "image_cache");
        if (!sdPathDr.exists()) {
            sdPathDr.mkdirs();
        }
        File image = new File(sdPathDr, getImageName(month, id));
        if (image.exists()) {
            res = image.toString();
        }
        return res;
    }

    public boolean isImage(int month, long id){
        if (isSdPresent()){
            boolean res = false;
            File sdPath = Environment.getExternalStorageDirectory();
            File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + "image_cache");
            if (!sdPathDr.exists()) {
                sdPathDr.mkdirs();
            }
            File image = new File(sdPathDr, getImageName(month, id));
            if (image.exists()) {
                res = true;
            }
            return res;
        } else {
            return false;
        }
    }

    public String getImageUrl(int month, long id){
        if (id != -1) {
            return BASE_URL + id;
        } else {
            return BASE_URL + photos[month];
        }
    }

    public String getImageName(int month, long id){
        if (id != -1) {
            return getFileName(id);
        } else {
            return getFileName(photos[month]);
        }
    }

    private String getFileName(long id) {
        return "photo_" + id + ".jpg";
    }

    public boolean isSdPresent() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}
