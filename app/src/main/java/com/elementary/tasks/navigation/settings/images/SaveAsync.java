package com.elementary.tasks.navigation.settings.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.elementary.tasks.core.utils.MemoryUtil;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
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
                bitmap = Picasso.with(mContext).load(path).resize(1280, 768).get();
            } else {
                bitmap = Picasso.with(mContext).load(path).get();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
