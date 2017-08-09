package com.elementary.tasks.core.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

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

public final class PicassoTool {

    private static final String TAG = "PicassoTool";

    private Picasso picasso;
    private LruCache lruCache;
    private static PicassoTool instance;

    private PicassoTool() {
    }

    private PicassoTool(Context context) {
        LogUtil.d(TAG, "PicassoTool: ");
        lruCache = new LruCache(context);
        picasso = new Picasso.Builder(context)
                .defaultBitmapConfig(Bitmap.Config.RGB_565)
                .memoryCache(lruCache)
                .build();
    }

    public static PicassoTool getInstance(Context context) {
        if (instance == null) {
            instance = new PicassoTool(context);
        } else if (instance.getPicasso() == null) {
            instance = new PicassoTool(context);
        }
        return instance;
    }

    public Picasso getPicasso() {
        return picasso;
    }

    public void invalidateCache(String url) {
        picasso.invalidate(url);
    }

    public void clearCache() {
        lruCache.clear();
    }
}
