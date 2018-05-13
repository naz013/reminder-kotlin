package com.elementary.tasks.navigation.settings.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.elementary.tasks.core.utils.MemoryUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutionException;

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

public class SaveAsync extends AsyncTask<String, Void, Void> {

    private Context mContext;

    public SaveAsync(Context context) {
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(String... strings) {
        String path = strings[0];
        String fileName = path;
        if (path.contains("=")) {
            int index = path.indexOf('=');
            fileName = path.substring(index);
        }
        File directory = MemoryUtil.getImageCacheDir();
        if (directory == null) return null;
        directory.mkdirs();
        File file = new File(directory, fileName + ".jpg");
        try {
            Bitmap bitmap;
            if (!path.contains("=")) {
                bitmap = Glide.with(mContext).asBitmap().load(path).apply(RequestOptions.overrideOf(1280, 76)).submit().get();
            } else {
                bitmap = Glide.with(mContext).asBitmap().load(path).submit().get();
            }
            try {
                if (file.createNewFile()) {
                    FileOutputStream stream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    stream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
