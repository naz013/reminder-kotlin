package com.elementary.tasks.notes;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Arrays;

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

public class NoteImage implements Serializable {

    @SerializedName("image")
    @Nullable
    private byte[] image;

    public NoteImage() {
    }

    public NoteImage(RealmImage image) {
        this.image = image.getImage();
    }

    public NoteImage(EditableRealmImage image) {
        this.image = image.getImage();
    }

    public NoteImage(byte[] image) {
        this.image = new byte[image.length];
        System.arraycopy(image, 0, this.image, 0, this.image.length);
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = new byte[image.length];
        System.arraycopy(image, 0, this.image, 0, this.image.length);
    }

    @Override
    public String toString() {
        return "NoteImage{" +
                "image=" + Arrays.toString(image) +
                '}';
    }
}
