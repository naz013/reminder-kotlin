package com.elementary.tasks.notes.editor;

import androidx.annotation.Nullable;

import com.elementary.tasks.notes.NoteImage;

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

class ImageSingleton {

    @Nullable
    private NoteImage mItem;
    private static ImageSingleton instance;

    private ImageSingleton() {

    }

    static ImageSingleton getInstance() {
        if (instance == null) {
            instance = new ImageSingleton();
        }
        return instance;
    }

    public void setItem(@Nullable NoteImage mItem) {
        this.mItem = mItem;
    }

    @Nullable
    public NoteImage getItem() {
        return mItem;
    }

    public void setImage(byte[] bitmapAsByteArray) {
        if (mItem != null) {
            mItem.setImage(bitmapAsByteArray);
        } else {
            mItem = new NoteImage(bitmapAsByteArray);
        }
    }
}
