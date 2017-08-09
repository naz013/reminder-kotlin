package com.elementary.tasks.core.calendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;

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

public class LoadAsync extends AsyncTask<Void, Void, Void> {

    private Context context;
    private int month;
    private long id;

    public LoadAsync(Context context, int month, long id) {
        this.context = context;
        this.month = month;
        this.id = id;
    }

    @Override
    protected Void doInBackground(Void... params) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (!ImageCheck.getInstance().isImage(month, id) && mWifi.isConnected()) {
            try {
                Bitmap bitmap = Picasso.with(context)
                        .load(ImageCheck.getInstance().getImageUrl(month, id))
                        .get();
                File sdPath = Environment.getExternalStorageDirectory();
                File sdPathDr = new File(sdPath.toString() + "/JustReminder/" + "image_cache");
                if (!sdPathDr.exists()) {
                    sdPathDr.mkdirs();
                }
                File image = new File(sdPathDr, ImageCheck.getInstance().getImageName(month, id));
                try {
                    if (image.createNewFile()) {
                        FileOutputStream stream = new FileOutputStream(image);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        stream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
